package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "生成短链结果")
public class CreateLinkResult {

    @Schema(description = "短链 ID", example = "1")
    private final Long id;

    @Schema(description = "短码", example = "Ab3dE9f")
    private final String code;

    @Schema(description = "完整短链地址", example = "http://localhost:8081/Ab3dE9f")
    private final String shortUrl;

    /** true 表示命中了幂等规则，返回的是已存在的短链。 */
    @Schema(description = "true 表示命中了幂等规则，返回的是已存在的短链")
    private final boolean duplicated;

    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
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
