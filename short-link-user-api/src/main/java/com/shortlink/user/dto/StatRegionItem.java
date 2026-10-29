package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "地域分布行")
public class StatRegionItem {

    @Schema(description = "省份，解析不出时为「未知」", example = "广东省")
    private String province;

    @Schema(description = "该省份的访问量", example = "260")
    private long pv;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public long getPv() {
        return pv;
    }

    public void setPv(long pv) {
        this.pv = pv;
    }
}
