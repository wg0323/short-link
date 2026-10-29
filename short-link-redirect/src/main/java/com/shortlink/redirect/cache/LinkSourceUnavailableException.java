package com.shortlink.redirect.cache;

/**
 * 缓存与数据库同时不可用（回源已被熔断），控制层据此渲染 503 提示页。
 */
public class LinkSourceUnavailableException extends RuntimeException {

    public LinkSourceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
