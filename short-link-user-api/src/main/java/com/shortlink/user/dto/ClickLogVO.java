package com.shortlink.user.dto;

import java.time.LocalDateTime;

/**
 * 访问明细行。IP 在输出时脱敏。
 */
public class ClickLogVO {

    private LocalDateTime clickAt;

    private String code;

    private String ip;

    private String province;

    private String city;

    private String device;

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
