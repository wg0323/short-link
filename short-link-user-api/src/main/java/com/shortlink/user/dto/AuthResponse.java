package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    /** access token 有效期，单位秒。 */
    private long expiresIn;

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

    public static class Profile {

        private Long id;

        private String phone;

        private String nickname;

        private String avatar;

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
