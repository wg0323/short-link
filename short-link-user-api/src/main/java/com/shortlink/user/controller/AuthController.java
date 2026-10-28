package com.shortlink.user.controller;

import com.shortlink.common.api.Result;
import com.shortlink.common.ratelimit.RateLimit;
import com.shortlink.common.ratelimit.RateLimitType;
import com.shortlink.common.security.UserContext;
import com.shortlink.user.dto.AuthResponse;
import com.shortlink.user.dto.LoginRequest;
import com.shortlink.user.dto.RefreshRequest;
import com.shortlink.user.dto.RegisterRequest;
import com.shortlink.user.dto.SendSmsRequest;
import com.shortlink.user.dto.SendSmsResult;
import com.shortlink.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "认证", description = "手机号注册、登录与令牌刷新")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "发送验证码（Mock 通道会直接回传验证码）")
    @PostMapping("/sms/send")
    @RateLimit(type = RateLimitType.IP, limit = 5, windowSeconds = 600, name = "sms_send")
    public Result<SendSmsResult> sendSms(@Valid @RequestBody SendSmsRequest request) {
        return Result.ok(authService.sendSms(request.getPhone()));
    }

    @Operation(summary = "注册并直接登录")
    @PostMapping("/register")
    @RateLimit(type = RateLimitType.IP, limit = 10, windowSeconds = 3600, name = "register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @Operation(summary = "手机号密码登录")
    @PostMapping("/login")
    @RateLimit(type = RateLimitType.IP, limit = 20, windowSeconds = 600, name = "login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @Operation(summary = "用刷新令牌换取新的访问令牌")
    @PostMapping("/refresh")
    public Result<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.ok(authService.refresh(request.getRefreshToken()));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout(UserContext.require());
        return Result.ok();
    }
}
