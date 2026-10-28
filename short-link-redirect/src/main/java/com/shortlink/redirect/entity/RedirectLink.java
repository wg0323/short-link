package com.shortlink.redirect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 跳转端只读视图：只取跳转所需的字段，避免把整张表的列都加载进缓存。
 */
@TableName("short_link")
public class RedirectLink {

    public static final int STATUS_NORMAL = 1;

    public static final int STATUS_DISABLED = 0;

    public static final int STATUS_DELETED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    /** 创建者，用于配额计数；旧缓存里没有该字段时为 null。 */
    private Long userId;

    private String originUrl;

    private String passwordHash;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private LocalDateTime expireAt;

    private Integer status;

    /** 1 表示因创建者配额耗尽而暂停。 */
    private Integer quotaBlocked;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getQuotaBlocked() {
        return quotaBlocked;
    }

    public void setQuotaBlocked(Integer quotaBlocked) {
        this.quotaBlocked = quotaBlocked;
    }
}
