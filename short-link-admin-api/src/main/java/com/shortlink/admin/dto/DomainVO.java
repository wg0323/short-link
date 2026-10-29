package com.shortlink.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "后台域名列表行")
public class DomainVO {

    @Schema(description = "域名 ID", example = "1")
    private final Long id;

    @Schema(description = "域名", example = "s.example.com")
    private final String domain;

    @Schema(description = "是否平台默认域名")
    private final boolean defaultFlag;

    @Schema(description = "1 启用，0 停用", example = "1")
    private final Integer status;

    @Schema(description = "该域名下已生成的短链数", example = "12")
    private final long linkCount;

    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private final LocalDateTime createdAt;

    public DomainVO(Long id, String domain, boolean defaultFlag, Integer status,
                    long linkCount, LocalDateTime createdAt) {
        this.id = id;
        this.domain = domain;
        this.defaultFlag = defaultFlag;
        this.status = status;
        this.linkCount = linkCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isDefaultFlag() {
        return defaultFlag;
    }

    public Integer getStatus() {
        return status;
    }

    public long getLinkCount() {
        return linkCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
