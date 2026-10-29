package com.shortlink.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理员登录请求")
public class AdminLoginRequest {

    @Schema(description = "管理员账号", example = "admin")
    @NotBlank(message = "请输入账号")
    private String username;

    @Schema(description = "管理员密码")
    @NotBlank(message = "请输入密码")
    private String password;

    @Schema(description = "图形验证码接口返回的 captchaId")
    @NotBlank(message = "请输入图形验证码")
    private String captchaId;

    @Schema(description = "用户填写的图形验证码", example = "A3BC")
    @NotBlank(message = "请输入图形验证码")
    private String captchaCode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}
