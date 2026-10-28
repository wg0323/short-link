package com.shortlink.common.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解，基于 Redis 有序集合实现滑动窗口。
 *
 * <pre>
 * &#64;RateLimit(type = RateLimitType.IP, limit = 5, windowSeconds = 60)
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    RateLimitType type() default RateLimitType.IP;

    /** 窗口内允许的最大请求数。 */
    int limit();

    /** 窗口长度，单位秒。 */
    int windowSeconds();

    /** 用于区分同一维度下不同接口的 key，默认取方法全名。 */
    String name() default "";
}
