package com.shortlink.redirect.cache;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.redirect.entity.RedirectLink;
import com.shortlink.redirect.mapper.RedirectLinkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 两级缓存：Caffeine 本地 + Redis 远程。
 *
 * <p>Caffeine 的 loader 只放行一个线程回源，天然避免缓存击穿；
 * Redis TTL 叠加随机抖动，避免大批 key 同时过期造成雪崩。</p>
 *
 * <p>本地缓存用 {@code refreshAfterWrite} 做异步刷新：到点后读线程照旧拿旧值，
 * 后台线程再去回源；回源失败（例如 MySQL 熔断）时旧值会保留，
 * 因此 Redis 与 MySQL 同时不可用时，L1 里的旧值仍能撑到 expireSeconds。</p>
 */
@Service
@Slf4j
public class LinkCacheService {

    /** Redis 读的 Sentinel 资源名，熔断规则挂到它上面。 */
    private static final String RESOURCE_REDIS_GET = "redis:linkcache";

    /** MySQL 回源的 Sentinel 资源名，熔断期间不再查库。 */
    private static final String RESOURCE_DB_LOAD = "mysql:link";

    private final LoadingCache<String, RedirectLink> localCache;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final RedirectLinkMapper linkMapper;

    private final long redisSeconds;

    public LinkCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                            RedirectLinkMapper linkMapper,
                            @Value("${shortlink.redirect.local-cache-size:10000}") long localSize,
                            @Value("${shortlink.redirect.local-cache-refresh-seconds:60}") long refreshSeconds,
                            @Value("${shortlink.redirect.local-cache-expire-seconds:600}") long expireSeconds,
                            @Value("${shortlink.redirect.redis-cache-seconds:1800}") long redisSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.linkMapper = linkMapper;
        this.redisSeconds = redisSeconds;
        // 到点异步刷新，读线程拿旧值；expireAfterWrite 只做兜底，让旧值扛得住 Redis 长时间不可用
        this.localCache = Caffeine.newBuilder()
                .maximumSize(localSize)
                .refreshAfterWrite(refreshSeconds, TimeUnit.SECONDS)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(this::load);
    }

    public RedirectLink get(String code) {
        // 加载失败不会写缓存，异常原样抛给控制层决定渲染哪一页
        return localCache.get(code);
    }

    /**
     * 收到失效广播后清理本地缓存，Redis 侧由发布方删除。
     */
    public void invalidate(String code) {
        localCache.invalidate(code);
    }

    /**
     * 回源：Redis → MySQL。Redis 熔断或异常只影响本次回源，且不再回写它；
     * MySQL 熔断时抛 {@link LinkSourceUnavailableException}。
     */
    private RedirectLink load(String code) {
        Entry entry = null;
        boolean redisAvailable = true;
        String cached = null;
        try {
            entry = SphU.entry(RESOURCE_REDIS_GET);
            cached = redisTemplate.opsForValue().get(RedisKeys.link(code));
        } catch (BlockException e) {
            redisAvailable = false;
            log.warn("Redis 缓存读已熔断，降级查库 code={}", code);
        } catch (RuntimeException e) {
            // 连接被拒这类快失败不会触发慢调用熔断，同样按不可用处理
            redisAvailable = false;
            log.warn("Redis 缓存读异常，降级查库 code={}", code, e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, RedirectLink.class);
            } catch (Exception e) {
                log.warn("短链缓存反序列化失败 code={}", code, e);
            }
        }
        RedirectLink link = loadFromDb(code);
        if (link != null && redisAvailable) {
            cacheToRedis(code, link);
        }
        return link;
    }

    /**
     * MySQL 回源。熔断打开时直接抛出、不再压库：刷新场景下 Caffeine 会保留旧值，
     * 冷 key 由控制层渲染 503 提示页。
     */
    private RedirectLink loadFromDb(String code) {
        Entry entry = null;
        try {
            entry = SphU.entry(RESOURCE_DB_LOAD, EntryType.OUT);
            return linkMapper.selectByCode(code);
        } catch (BlockException e) {
            throw new LinkSourceUnavailableException("短链回源已熔断 code=" + code, e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void cacheToRedis(String code, RedirectLink link) {
        try {
            long ttl = redisSeconds + ThreadLocalRandom.current().nextInt(300);
            redisTemplate.opsForValue().set(RedisKeys.link(code),
                    objectMapper.writeValueAsString(link), Duration.ofSeconds(ttl));
        } catch (Exception e) {
            log.warn("短链缓存写入失败 code={}", code, e);
        }
    }
}
