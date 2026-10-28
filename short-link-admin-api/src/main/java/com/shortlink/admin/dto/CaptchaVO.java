package com.shortlink.admin.dto;

public class CaptchaVO {

    private final String captchaId;

    /** 可直接放进 img 标签的 data URI。 */
    private final String image;

    public CaptchaVO(String captchaId, String image) {
        this.captchaId = captchaId;
        this.image = image;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public String getImage() {
        return image;
    }
}
