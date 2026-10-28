package com.shortlink.redirect.event;

import java.time.LocalDateTime;

/**
 * 一次点击的埋点数据。
 */
public class ClickEvent {

    private final String code;

    private final Long userId;

    private final LocalDateTime clickAt;

    private final String ip;

    private final String province;

    private final String city;

    private final String device;

    private final String referer;

    private final String ua;

    public ClickEvent(String code, Long userId, LocalDateTime clickAt, String ip, String province,
                      String city, String device, String referer, String ua) {
        this.code = code;
        this.userId = userId;
        this.clickAt = clickAt;
        this.ip = ip;
        this.province = province;
        this.city = city;
        this.device = device;
        this.referer = referer;
        this.ua = ua;
    }

    public String getCode() {
        return code;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getClickAt() {
        return clickAt;
    }

    public String getIp() {
        return ip;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDevice() {
        return device;
    }

    public String getReferer() {
        return referer;
    }

    public String getUa() {
        return ua;
    }
}
