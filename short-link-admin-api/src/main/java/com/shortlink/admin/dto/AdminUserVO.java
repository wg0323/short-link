package com.shortlink.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 后台用户列表行：注册用户 + 累计短链数。
 */
@Schema(description = "后台用户列表行")
public class AdminUserVO {

    @Schema(description = "用户 ID", example = "1")
    private Long id;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "1 正常，0 禁用", example = "1")
    private Integer status;

    @Schema(description = "该用户已生成的短链数", example = "12")
    private Long linkCount;

    @Schema(description = "最近登录时间", example = "2026-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @Schema(description = "注册时间", example = "2026-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
