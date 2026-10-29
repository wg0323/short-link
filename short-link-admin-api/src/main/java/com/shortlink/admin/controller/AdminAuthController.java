package com.shortlink.admin.controller;

import com.shortlink.admin.dto.AdminAuthResult;
import com.shortlink.admin.dto.AdminLoginRequest;
import com.shortlink.admin.dto.CaptchaVO;
import com.shortlink.admin.service.AdminAuthService;
import com.shortlink.admin.service.CaptchaService;
import com.shortlink.common.api.Result;
import com.shortlink.common.ratelimit.RateLimit;
import com.shortlink.common.ratelimit.RateLimitType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "后台认证", description = "图形验证码与管理员登录")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final CaptchaService captchaService;

    private final AdminAuthService adminAuthService;

    @Operation(summary = "获取图形验证码", description = "返回验证码 ID 与 base64 图片，5 分钟过期，校验后立即作废")
    @GetMapping("/captcha")
    @RateLimit(type = RateLimitType.IP, limit = 30, windowSeconds = 60, name = "admin_captcha")
    public Result<CaptchaVO> captcha() {
        return Result.ok(captchaService.generate());
    }

    @Operation(summary = "管理员登录", description = "先校验图形验证码，再校验账号密码；后台令牌与用户端令牌互不通用")
    @PostMapping("/login")
    @RateLimit(type = RateLimitType.IP, limit = 10, windowSeconds = 600, name = "admin_login")
    public Result<AdminAuthResult> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.ok(adminAuthService.login(request));
    }
}
