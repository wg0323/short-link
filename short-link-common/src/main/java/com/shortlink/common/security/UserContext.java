package com.shortlink.common.security;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;

/**
 * 登录用户上下文，按请求线程存放用户 ID。
 *
 * <p>由 JWT 拦截器写入，业务层通过 {@link #require()} 取当前用户；
 * 请求结束时必须调用 {@link #clear()}，否则线程复用会串号。</p>
 */
public final class UserContext {

    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        HOLDER.set(userId);
    }

    public static Long get() {
        return HOLDER.get();
    }

    public static Long require() {
        Long userId = HOLDER.get();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
