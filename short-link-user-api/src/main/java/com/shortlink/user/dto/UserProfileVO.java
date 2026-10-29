package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "个人信息")
public class UserProfileVO {

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
