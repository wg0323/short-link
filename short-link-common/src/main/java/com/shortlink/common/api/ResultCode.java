package com.shortlink.common.api;

/**
 * 业务状态码。4xxxx 为业务/参数问题，5xxxx 为系统问题。
 */
public enum ResultCode {

    SUCCESS(0, "成功"),

    PARAM_ERROR(40000, "参数不合法"),
    SMS_CODE_ERROR(40001, "验证码错误或已过期"),
    PHONE_REGISTERED(40002, "该手机号已注册"),
    PASSWORD_ERROR(40003, "手机号或密码错误"),
    URL_ILLEGAL(40004, "长链接不合法"),
    CODE_OCCUPIED(40005, "短码已被占用"),
    CODE_ILLEGAL(40006, "短码不合法"),
    PASSWORD_TOO_WEAK(40007, "密码强度不足"),
    SMS_SEND_TOO_FREQUENT(40008, "验证码发送过于频繁"),

    UNAUTHORIZED(40100, "登录已失效"),
    FORBIDDEN(40300, "无权访问"),
    ACCOUNT_DISABLED(40301, "账号已被禁用"),
    LINK_EXPIRED(40302, "短链已过期"),
    LINK_DISABLED(40303, "短链已被禁用"),
    ACCESS_PASSWORD_ERROR(40304, "访问密码错误"),

    NOT_FOUND(40400, "资源不存在"),

    RATE_LIMITED(42900, "请求过于频繁，请稍后再试"),

    SYSTEM_ERROR(50000, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
