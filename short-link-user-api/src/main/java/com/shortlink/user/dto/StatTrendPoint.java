package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 单日统计点（也用于区间汇总）。
 */
@Schema(description = "单日统计点")
public class StatTrendPoint {

    @Schema(description = "统计日期", example = "2026-01-01")
    private LocalDate date;

    @Schema(description = "访问量 PV", example = "128")
    private long pv;

    @Schema(description = "独立访客 UV", example = "36")
    private long uv;

    @Schema(description = "独立 IP 数", example = "30")
    private long ipCount;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getPv() {
        return pv;
    }

    public void setPv(long pv) {
        this.pv = pv;
    }

    public long getUv() {
        return uv;
    }

    public void setUv(long uv) {
        this.uv = uv;
    }

    public long getIpCount() {
        return ipCount;
    }

    public void setIpCount(long ipCount) {
        this.ipCount = ipCount;
    }
}
