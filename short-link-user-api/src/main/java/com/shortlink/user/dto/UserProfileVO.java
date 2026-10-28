package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class UserProfileVO {

    private Long id;

    private String phone;

    private String nickname;

    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    public UserProfileVO() {
    }

    public UserProfileVO(Long id, String phone, String nickname, String avatar, LocalDateTime createdAt) {
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
