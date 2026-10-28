package com.shortlink.common.cache;

import com.shortlink.common.constant.RedisKeys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 短链缓存失效：先删 Redis，再广播给所有跳转实例清理本地缓存。
 *
 * <p>只删 Redis 是不够的 —— 每个跳转实例还各自持有 Caffeine 本地缓存，
 * 不广播的话最长要等本地 TTL 到期才会生效。</p>
 */
@Component
public class LinkCachePublisher {

    private final StringRedisTemplate redisTemplate;

    public LinkCachePublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void evict(String code) {
        redisTemplate.delete(RedisKeys.link(code));
        redisTemplate.convertAndSend(RedisKeys.LINK_EVICT_CHANNEL, code);
    }

    /**
     * 新建短链后广播，让所有跳转实例把短码加入布隆过滤器；
     * 否则新链接会被布隆误判为“不存在”而直接 404。
     */
    public void publishAdd(String code) {
        redisTemplate.convertAndSend(RedisKeys.LINK_ADD_CHANNEL, code);
    }
}
