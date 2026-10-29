package com.shortlink.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "修改短链请求，字段传 null 表示不修改")
public class UpdateLinkRequest {

    @Schema(description = "备注名，最长 100 个字符")
    @Size(max = 100, message = "备注名最长 100 个字符")
    private String remark;

    /** 传空字符串表示清除访问密码，传 null 表示不修改。 */
    @Schema(description = "访问密码，传空字符串表示清除密码，传 null 表示不修改")
    @Size(max = 32, message = "访问密码最长 32 位")
    private String password;

    @Schema(description = "生效开始时间", example = "2026-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;

    @Schema(description = "生效结束时间", example = "2026-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;

    @Schema(description = "到期时间", example = "2026-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireAt;

    /** 1 启用 0 禁用。 */
    @Schema(description = "1 启用，0 禁用", example = "1")
    @Min(0)
    @Max(1)
    private Integer status;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
