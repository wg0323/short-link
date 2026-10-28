package com.shortlink.admin.dto;

public class AdminAuthResult {

    private final String token;

    private final long expiresIn;

    private final String username;

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
