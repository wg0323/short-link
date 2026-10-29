package com.shortlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 访问明细行。IP 在输出时脱敏。
 */
@Schema(description = "访问明细行")
public class ClickLogVO {

    @Schema(description = "访问时间", example = "2026-01-01 12:00:00")
    private LocalDateTime clickAt;

    @Schema(description = "短码", example = "Ab3dE9f")
    private String code;

    @Schema(description = "访问者 IP，已脱敏", example = "113.88.*.*")
    private String ip;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "设备信息，格式「系统 · 浏览器」", example = "Android · Chrome")
    private String device;

    @Schema(description = "来源页地址")
    private String referer;

    public LocalDateTime getClickAt() {
        return clickAt;
    }

    public void setClickAt(LocalDateTime clickAt) {
        this.clickAt = clickAt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
