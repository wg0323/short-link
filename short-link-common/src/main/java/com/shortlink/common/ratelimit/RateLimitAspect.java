package com.shortlink.common.ratelimit;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.security.UserContext;
import com.shortlink.common.util.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 限流切面：Redis 有序集合 + Lua 脚本实现滑动窗口，保证原子性。
 *
 * <p>滑动窗口会记录窗口内每个请求的时间戳，比固定窗口准确，代价是每个被限流的 key
 * 最多保留 {@code limit} 个成员，量级可控。</p>
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final String LUA =
            "local key = KEYS[1]\n"
                    + "local now = tonumber(ARGV[1])\n"
                    + "local window = tonumber(ARGV[2])\n"
                    + "local limit = tonumber(ARGV[3])\n"
                    + "redis.call('ZREMRANGEBYSCORE', key, 0, now - window)\n"
                    + "local count = redis.call('ZCARD', key)\n"
                    + "if count < limit then\n"
                    + "  redis.call('ZADD', key, now, ARGV[4])\n"
                    + "  redis.call('PEXPIRE', key, window)\n"
                    + "  return 1\n"
                    + "end\n"
                    + "return 0";

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> script;

    public RateLimitAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(LUA, Long.class);
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        long now = System.currentTimeMillis();
        String member = now + "-" + ThreadLocalRandom.current().nextInt(1_000_000);

        Long allowed = redisTemplate.execute(script, Collections.singletonList(key),
                String.valueOf(now),
                String.valueOf(rateLimit.windowSeconds() * 1000L),
                String.valueOf(rateLimit.limit()),
                member);

        if (allowed == null || allowed == 0L) {
            throw new BizException(ResultCode.RATE_LIMITED);
        }
        return joinPoint.proceed();
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        String name = rateLimit.name().isEmpty()
                ? joinPoint.getSignature().toShortString()
                : rateLimit.name();

        String dimension;
        switch (rateLimit.type()) {
            case USER:
                dimension = String.valueOf(UserContext.require());
                break;
            case GLOBAL:
                dimension = "global";
                break;
            case IP:
            default:
                dimension = currentIp();
                break;
        }
        return RedisKeys.rateLimit(name, dimension);
    }

    private String currentIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? "unknown" : IpUtils.getClientIp(attributes.getRequest());
    }
}
