package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "登录 / 注册 / 刷新返回的凭证")
public class AuthResponse {

    @Schema(description = "访问令牌，放在 Authorization: Bearer 后面")
    private String accessToken;

    @Schema(description = "刷新令牌，用于换取新的访问令牌")
    private String refreshToken;

    /** access token 有效期，单位秒。 */
    @Schema(description = "访问令牌有效期，单位秒", example = "7200")
    private long expiresIn;

    @Schema(description = "当前登录用户信息")
    private Profile user;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, long expiresIn, Profile user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Profile getUser() {
        return user;
    }

    public void setUser(Profile user) {
        this.user = user;
    }

    @Schema(description = "登录用户基本信息")
    public static class Profile {

        @Schema(description = "用户 ID", example = "1")
        private Long id;

        @Schema(description = "手机号", example = "13800000000")
        private String phone;

        @Schema(description = "昵称", example = "小明")
        private String nickname;

        @Schema(description = "头像访问地址，未上传时为空串")
        private String avatar;

        @Schema(description = "注册日期", example = "2026-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime createdAt;

        public Profile() {
        }

        public Profile(Long id, String phone, String nickname, String avatar, LocalDateTime createdAt) {
            this.id = id;
            this.phone = phone;
            this.nickname = nickname;
            this.avatar = avatar;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
