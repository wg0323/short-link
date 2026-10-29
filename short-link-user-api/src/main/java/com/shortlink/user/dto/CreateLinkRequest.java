package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "生成短链请求")
public class CreateLinkRequest {

    @Schema(description = "长链接，仅支持 http / https，最长 2048 字符", example = "https://example.com/a/b")
    @NotBlank(message = "长链接不能为空")
    @Size(max = 2048, message = "长链接长度不能超过 2048")
    private String originUrl;

    /** 为空时使用平台默认域名。 */
    @Schema(description = "短链域名 ID，为空则使用平台默认域名")
    private Long domainId;

    @Schema(description = "备注名，最长 100 个字符")
    @Size(max = 100, message = "备注名最长 100 个字符")
    private String remark;

    /** 自定义短码，为空则由发号器生成。 */
    @Schema(description = "自定义短码，为空则由发号器生成，最长 8 位字母数字", example = "promo2026")
    @Pattern(regexp = "^[0-9a-zA-Z]{0,8}$", message = "短码只能包含字母和数字，最长 8 位")
    private String code;

    @Schema(description = "访问密码，4-32 位；设置后需先验密码才能跳转")
    @Size(min = 4, max = 32, message = "访问密码长度需为 4-32 位")
    private String password;

    /** 生效开始时间，为空表示立即生效。 */
    @Schema(description = "生效开始时间，为空表示立即生效", example = "2026-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;

    /** 生效结束时间。 */
    @Schema(description = "生效结束时间，超过后不再跳转", example = "2026-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;

    /** 到期时间。 */
    @Schema(description = "到期时间，过期后链接作废", example = "2026-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireAt;

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
