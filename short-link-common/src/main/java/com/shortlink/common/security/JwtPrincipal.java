package com.shortlink.common.security;

/**
 * 从 JWT 解析出的身份信息：用户端与后台共用一套拦截器，靠 role 区分。
 */
public class JwtPrincipal {

    public static final String ROLE_USER = "user";

    public static final String ROLE_ADMIN = "admin";

    private final long subjectId;

    private final String role;

    public JwtPrincipal(long subjectId, String role) {
        this.subjectId = subjectId;
        this.role = role;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public String getRole() {
        return role;
    }

    public boolean isUser() {
        return ROLE_USER.equals(role);
    }
}
