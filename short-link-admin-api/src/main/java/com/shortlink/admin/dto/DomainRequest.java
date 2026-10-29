package com.shortlink.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = "新增域名请求")
public class DomainRequest {

    @Schema(description = "短链域名，不带 http:// 前缀，可带端口", example = "s.example.com")
    @NotBlank(message = "请输入短链域名")
    @Size(max = 100, message = "域名长度不能超过 100")
    @Pattern(regexp = "^[a-zA-Z0-9.-]+(:\\d{1,5})?$", message = "域名格式不正确，不要带 http:// 前缀")
    private String domain;

    @Schema(description = "是否设为默认域名，会取消原有的默认域名")
    private boolean defaultFlag;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isDefaultFlag() {
        return defaultFlag;
    }

    public void setDefaultFlag(boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
    }
}
