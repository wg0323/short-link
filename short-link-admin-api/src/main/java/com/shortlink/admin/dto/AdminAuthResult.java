package com.shortlink.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理员登录结果")
public class AdminAuthResult {

    @Schema(description = "后台访问令牌，放在 Authorization: Bearer 后面")
    private final String token;

    @Schema(description = "令牌有效期，单位秒", example = "28800")
    private final long expiresIn;

    @Schema(description = "管理员账号", example = "admin")
    private final String username;

    @Schema(description = "管理员昵称", example = "超级管理员")
    private final String nickname;

    public AdminAuthResult(String token, long expiresIn, String username, String nickname) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.username = username;
        this.nickname = nickname;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }
}
