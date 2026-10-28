package com.shortlink.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.admin.dto.AdminAuthResult;
import com.shortlink.admin.dto.AdminLoginRequest;
import com.shortlink.admin.entity.AdminUser;
import com.shortlink.admin.mapper.AdminUserMapper;
import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 后台登录：图形验证码 + 账号密码 + 独立 JWT 密钥。
 *
 * <p>后台令牌与用户端令牌使用不同 secret，互相不可用。</p>
 */
@Service
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final CaptchaService captchaService;

    public AdminAuthService(AdminUserMapper adminUserMapper, PasswordEncoder passwordEncoder,
                            JwtService jwtService, CaptchaService captchaService) {
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.captchaService = captchaService;
    }

    public AdminAuthResult login(AdminLoginRequest request) {
        captchaService.verify(request.getCaptchaId(), request.getCaptchaCode());

        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.getUsername().trim())
                .last("limit 1"));
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BizException(ResultCode.PASSWORD_ERROR, "账号或密码错误");
        }
        if (Integer.valueOf(0).equals(admin.getStatus())) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED, "该管理员账号已被禁用");
        }

        AdminUser update = new AdminUser();
        update.setId(admin.getId());
        update.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateById(update);

        String token = jwtService.createToken(admin.getId(), "admin");
        return new AdminAuthResult(token, jwtService.getExpireSeconds(), admin.getUsername(), admin.getNickname());
    }
}
