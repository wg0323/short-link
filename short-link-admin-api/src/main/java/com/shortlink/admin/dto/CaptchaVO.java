package com.shortlink.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "图形验证码")
public class CaptchaVO {

    @Schema(description = "验证码 ID，登录时回传给 captchaId")
    private final String captchaId;

    /** 可直接放进 img 标签的 data URI。 */
    @Schema(description = "base64 图片，格式 data:image/png;base64,...，可直接放进 img 标签")
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
