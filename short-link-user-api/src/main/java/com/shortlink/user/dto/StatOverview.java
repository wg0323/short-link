package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 概览卡片数据：区间汇总 + 链接总数。
 */
@Schema(description = "概览卡片数据")
public class StatOverview {

    @Schema(description = "访问量 PV", example = "1280")
    private final long pv;

    @Schema(description = "独立访客 UV", example = "320")
    private final long uv;

    @Schema(description = "独立 IP 数", example = "280")
    private final long ipCount;

    @Schema(description = "短链总数", example = "12")
    private final long linkCount;

    public StatOverview(long pv, long uv, long ipCount, long linkCount) {
        this.pv = pv;
        this.uv = uv;
        this.ipCount = ipCount;
        this.linkCount = linkCount;
    }

    public long getPv() {
        return pv;
    }

    public long getUv() {
        return uv;
    }

    public long getIpCount() {
        return ipCount;
    }

    public long getLinkCount() {
        return linkCount;
    }
}
