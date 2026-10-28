package com.shortlink.user.dto;

import java.time.LocalDate;

/**
 * 单日统计点（也用于区间汇总）。
 */
public class StatTrendPoint {

    private LocalDate date;

    private long pv;

    private long uv;

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
