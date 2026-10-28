package com.shortlink.common.ratelimit;

/**
 * 限流维度。
 */
public enum RateLimitType {

    /** 按客户端 IP 限流。 */
    IP,

    /** 按当前登录用户限流。 */
    USER,

    /** 全局限流。 */
    GLOBAL
}
