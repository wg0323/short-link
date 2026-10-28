package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 短链列表 / 详情视图。
 */
public class LinkVO {

    private final Long id;

    private final String code;

    private final String shortUrl;

    private final String originUrl;

    private final String remark;

    private final Long domainId;

    private final String domain;

    /** normal / expired / disabled / deleted。 */
    private final String status;

    private final boolean hasPassword;

    private final Long clickCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime endAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime expireAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    public LinkVO(Long id, String code, String shortUrl, String originUrl, String remark, Long domainId,
                  String domain, String status, boolean hasPassword, Long clickCount,
                  LocalDateTime startAt, LocalDateTime endAt, LocalDateTime expireAt, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.shortUrl = shortUrl;
        this.originUrl = originUrl;
        this.remark = remark;
        this.domainId = domainId;
        this.domain = domain;
        this.status = status;
        this.hasPassword = hasPassword;
        this.clickCount = clickCount;
        this.startAt = startAt;
        this.endAt = endAt;
        this.expireAt = expireAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public String getRemark() {
        return remark;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getDomain() {
        return domain;
    }

    public String getStatus() {
        return status;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
