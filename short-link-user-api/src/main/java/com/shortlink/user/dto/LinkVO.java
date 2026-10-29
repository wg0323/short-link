package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 短链列表 / 详情视图。
 */
@Schema(description = "短链列表 / 详情")
public class LinkVO {

    @Schema(description = "短链 ID", example = "1")
    private final Long id;

    @Schema(description = "短码", example = "Ab3dE9f")
    private final String code;

    @Schema(description = "完整短链地址", example = "http://localhost:8081/Ab3dE9f")
    private final String shortUrl;

    @Schema(description = "原始长链接", example = "https://example.com/a/b")
    private final String originUrl;

    @Schema(description = "备注名")
    private final String remark;

    @Schema(description = "短链域名 ID", example = "1")
    private final Long domainId;

    @Schema(description = "短链域名", example = "localhost:8081")
    private final String domain;

    /** normal / expired / disabled / deleted。 */
    @Schema(description = "状态：normal / expired / disabled / deleted", example = "normal")
    private final String status;

    @Schema(description = "是否设置了访问密码")
    private final boolean hasPassword;

    @Schema(description = "累计点击量", example = "128")
    private final Long clickCount;

    @Schema(description = "生效开始时间", example = "2026-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime startAt;

    @Schema(description = "生效结束时间", example = "2026-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime endAt;

    @Schema(description = "到期时间", example = "2026-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime expireAt;

    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
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
