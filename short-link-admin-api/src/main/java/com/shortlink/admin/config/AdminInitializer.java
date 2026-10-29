package com.shortlink.admin.config;

import com.shortlink.admin.entity.AdminUser;
import com.shortlink.admin.mapper.AdminUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 首次启动时创建默认管理员，避免交付后没有入口。
 *
 * <p>密码通过 BCrypt 现算，所以不需要在 SQL 里放明文或预生成摘要。</p>
 */
@Component
@Slf4j
public class AdminInitializer implements ApplicationRunner {

    private final AdminUserMapper adminUserMapper;

    private final PasswordEncoder passwordEncoder;

    private final String initUsername;

    private final String initPassword;

    public AdminInitializer(AdminUserMapper adminUserMapper, PasswordEncoder passwordEncoder,
                            @Value("${shortlink.admin.init-username:admin}") String initUsername,
                            @Value("${shortlink.admin.init-password:Admin@123456}") String initPassword) {
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.initUsername = initUsername;
        this.initPassword = initPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long count = adminUserMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(initUsername);
        admin.setPasswordHash(passwordEncoder.encode(initPassword));
        admin.setNickname("超级管理员");
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.insert(admin);
        log.warn("已创建默认管理员账号 {}，请登录后尽快修改密码", initUsername);
    }
}
