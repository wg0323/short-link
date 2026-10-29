package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 短链列表查询条件。
 */
@Schema(description = "短链列表查询条件")
public class LinkQuery {

    /** 备注名模糊匹配。 */
    @Schema(description = "备注名，模糊匹配")
    private String remark;

    @Schema(description = "短链域名 ID")
    private Long domainId;

    /** normal / expired / disabled / deleted，为空表示全部（不含已删除）。 */
    @Schema(description = "状态：normal / expired / disabled / deleted，为空表示全部（不含已删除）")
    private String status;

    @Schema(description = "创建时间起，格式 yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "创建时间止，格式 yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Schema(description = "页码，从 1 开始", example = "1")
    private int page = 1;

    @Schema(description = "每页条数，最大 100", example = "20")
    private int size = 20;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getPage() {
        return page < 1 ? 1 : page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        if (size < 1) {
            return 20;
        }
        return size > 100 ? 100 : size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
