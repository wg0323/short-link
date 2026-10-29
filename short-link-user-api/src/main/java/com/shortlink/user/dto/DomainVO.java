package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "可用的短链域名")
public class DomainVO {

    @Schema(description = "域名 ID", example = "1")
    private final Long id;

    @Schema(description = "域名", example = "localhost:8081")
    private final String domain;

    /** 是否为平台默认域名。 */
    @Schema(description = "是否为平台默认域名；生成短链不传域名时用它")
    private final boolean defaultFlag;

    public DomainVO(Long id, String domain, boolean defaultFlag) {
        this.id = id;
        this.domain = domain;
        this.defaultFlag = defaultFlag;
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
}
