package com.shortlink.user.dto;

/**
 * 概览卡片数据：区间汇总 + 链接总数。
 */
public class StatOverview {

    private final long pv;

    private final long uv;

    private final long ipCount;

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
