package com.shortlink.redirect.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.redirect.entity.RedirectLink;
import com.shortlink.redirect.mapper.RedirectLinkMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 两级缓存：Caffeine 本地 + Redis 远程。
 *
 * <p>Caffeine 的 {@code get(key, loader)} 只放行一个线程回源，天然避免缓存击穿；
 * Redis TTL 叠加随机抖动，避免大批 key 同时过期造成雪崩。</p>
 */
@Service
public class LinkCacheService {

    private static final Logger log = LoggerFactory.getLogger(LinkCacheService.class);

    private final Cache<String, RedirectLink> localCache;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final RedirectLinkMapper linkMapper;

    private final long redisSeconds;

    public LinkCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                            RedirectLinkMapper linkMapper,
                            @Value("${shortlink.redirect.local-cache-size:10000}") long localSize,
                            @Value("${shortlink.redirect.local-cache-seconds:60}") long localSeconds,
                            @Value("${shortlink.redirect.redis-cache-seconds:1800}") long redisSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.linkMapper = linkMapper;
        this.redisSeconds = redisSeconds;
        this.localCache = Caffeine.newBuilder()
                .maximumSize(localSize)
                .expireAfterWrite(localSeconds, TimeUnit.SECONDS)
                .build();
    }

    public RedirectLink get(String code) {
        return localCache.get(code, this::load);
    }

    /**
     * 收到失效广播后清理本地缓存，Redis 侧由发布方删除。
     */
    public void invalidate(String code) {
        localCache.invalidate(code);
    }

    private RedirectLink load(String code) {
        String cached = redisTemplate.opsForValue().get(RedisKeys.link(code));
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, RedirectLink.class);
            } catch (Exception e) {
                log.warn("短链缓存反序列化失败 code={}", code, e);
            }
        }
        RedirectLink link = linkMapper.selectByCode(code);
        if (link != null) {
            cacheToRedis(code, link);
        }
        return link;
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
