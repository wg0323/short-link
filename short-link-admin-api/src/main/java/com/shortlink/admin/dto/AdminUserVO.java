package com.shortlink.admin.dto;

import java.time.LocalDateTime;

/**
 * 后台用户列表行：注册用户 + 累计短链数。
 */
public class AdminUserVO {

    private Long id;

    private String phone;

    private String nickname;

    private Integer status;

    private Long linkCount;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getLinkCount() {
        return linkCount;
    }

    public void setLinkCount(Long linkCount) {
        this.linkCount = linkCount;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
