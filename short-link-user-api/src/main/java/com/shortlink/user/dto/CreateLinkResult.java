package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class CreateLinkResult {

    private final Long id;

    private final String code;

    private final String shortUrl;

    /** true 表示命中了幂等规则，返回的是已存在的短链。 */
    private final boolean duplicated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    public CreateLinkResult(Long id, String code, String shortUrl, boolean duplicated, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.shortUrl = shortUrl;
        this.duplicated = duplicated;
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

    public boolean isDuplicated() {
        return duplicated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
