package com.shortlink.common.security;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.util.Base62;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 短码策略：字符集、长度、系统保留词。
 *
 * <p>短链域名下所有一级路径都会落到跳转服务，因此 {@code /login}、{@code /api} 这类系统路由
 * 不能被用户自定义短码占用，否则会把平台自己的页面顶掉。</p>
 */
public final class CodePolicy {

    private static final Set<String> RESERVED = new HashSet<>(Arrays.asList(
            "api", "admin", "login", "logout", "register", "signup", "signin",
            "actuator", "static", "assets", "error", "health", "status", "metrics",
            "p", "s", "r", "c", "u", "i", "go", "t",
            "favicon.ico", "robots.txt", "sitemap.xml", "manifest.json",
            "swagger", "swagger-ui", "doc.html", "api-docs", "v3",
            "dashboard", "console", "user", "users", "link", "links",
            "help", "about", "terms", "privacy", "contact", "pricing",
            "open", "oauth", "callback", "webhook"
    ));

    private CodePolicy() {
    }

    /**
     * 校验用户自定义短码，不合法时抛出业务异常。
     */
    public static void validateCustomCode(String code) {
        if (!Base62.isValidCode(code)) {
            throw new BizException(ResultCode.CODE_ILLEGAL, "短码只能包含字母和数字，长度 5-8 位");
        }
        if (RESERVED.contains(code.toLowerCase())) {
            throw new BizException(ResultCode.CODE_ILLEGAL, "该短码为系统保留词，请换一个");
        }
    }

    public static boolean isReserved(String code) {
        return code != null && RESERVED.contains(code.toLowerCase());
    }
}
