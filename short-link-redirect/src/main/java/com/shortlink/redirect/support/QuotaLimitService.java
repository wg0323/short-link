package com.shortlink.redirect.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.redirect.mapper.RedirectLinkMapper;
import com.shortlink.redirect.mapper.UserQuotaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 访问额度守护：累计点击跨过套餐上限时，把该用户的在线短链置为配额暂停。
 *
 * <p>跑在埋点消费线程里，只处理「刚跨过阈值」的那一次：用 Redis SETNX 打标保证
 * 一个周期只触发一次，阈值用本地缓存避免每批点击都查库。</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuotaLimitService {

    /** 一次最多清理这么多短链的 Redis 缓存，超出部分靠缓存 TTL 自然失效。 */
    private static final int CODE_LIMIT = 5000;

    private static final int DELETE_BATCH = 500;

    private final StringRedisTemplate redisTemplate;

    private final RedirectLinkMapper linkMapper;

    private final UserQuotaMapper userQuotaMapper;

    private final Cache<Long, Optional<Long>> limitCache = Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .build();

    /**
     * 检查这批点击涉及的创建者，超限的置为暂停。异常一律吞掉，不能影响统计写入。
     */
    public void check(Set<Long> userIds) {
        for (Long userId : userIds) {
            try {
                checkOne(userId);
            } catch (Exception e) {
                log.warn("配额暂停处理失败 userId={}", userId, e);
            }
        }
    }

    private void checkOne(long userId) {
        Optional<Long> limit = limitCache.get(userId, this::loadLimit);
        if (!limit.isPresent()) {
            return;
        }
        String value = redisTemplate.opsForValue().get(RedisKeys.quotaClickTotal(userId));
        long used = value == null ? 0L : Long.parseLong(value);
        if (used < limit.get()) {
            return;
        }
        Boolean first = redisTemplate.opsForValue().setIfAbsent(RedisKeys.quotaBlocked(userId), "1");
        if (!Boolean.TRUE.equals(first)) {
            return;
        }
        block(userId, used, limit.get());
    }

    private Optional<Long> loadLimit(long userId) {
        return Optional.ofNullable(userQuotaMapper.selectTotalClick(userId));
    }

    private void block(long userId, long used, long limit) {
        int updated = linkMapper.blockByUser(userId);
        List<String> codes = linkMapper.selectBlockedCodes(userId, CODE_LIMIT);
        for (int start = 0; start < codes.size(); start += DELETE_BATCH) {
            List<String> batch = codes.subList(start, Math.min(start + DELETE_BATCH, codes.size()));
            redisTemplate.delete(batch.stream().map(RedisKeys::link).collect(Collectors.toList()));
        }
        // Caffeine 本地缓存不主动清，最多延迟一个 TTL（60 秒）就会读到新状态
        log.warn("用户访问额度耗尽，已暂停短链 userId={} used={} limit={} links={} cached={}",
                userId, used, limit, updated, codes.size());
    }
}
