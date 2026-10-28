package com.shortlink.admin.service;

import com.shortlink.admin.dto.AdminUserVO;
import com.shortlink.admin.entity.PlatformUser;
import com.shortlink.admin.mapper.AdminUserMapper;
import com.shortlink.admin.mapper.PlatformUserMapper;
import com.shortlink.admin.mapper.QuotaAdminMapper;
import com.shortlink.common.api.PageVO;
import com.shortlink.common.api.ResultCode;
import com.shortlink.common.cache.LinkCachePublisher;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 注册用户管理：查询、禁用/启用、重置密码。
 *
 * <p>禁用会写一个 Redis 标记，用户端拦截器每次请求检查它，做到「禁用立即生效」；
 * 一期不物理删除用户，避免留下孤儿短链与统计数据。</p>
 */
@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final AdminUserMapper adminUserMapper;

    private final PlatformUserMapper platformUserMapper;

    private final QuotaAdminMapper quotaAdminMapper;

    private final LinkCachePublisher linkCachePublisher;

    private final PasswordEncoder passwordEncoder;

    private final StringRedisTemplate redisTemplate;

    public AdminUserService(AdminUserMapper adminUserMapper, PlatformUserMapper platformUserMapper,
                            QuotaAdminMapper quotaAdminMapper, LinkCachePublisher linkCachePublisher,
                            PasswordEncoder passwordEncoder, StringRedisTemplate redisTemplate) {
        this.adminUserMapper = adminUserMapper;
        this.platformUserMapper = platformUserMapper;
        this.quotaAdminMapper = quotaAdminMapper;
        this.linkCachePublisher = linkCachePublisher;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    public PageVO<AdminUserVO> page(String keyword, Integer status, int page, int size) {
        int currentPage = Math.max(page, 1);
        int pageSize = size < 1 ? 20 : Math.min(size, 100);
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

        long total = adminUserMapper.countUsers(kw, status);
        List<AdminUserVO> records = adminUserMapper.pageUsers(kw, status,
                (currentPage - 1) * pageSize, pageSize);
        return new PageVO<>(total, currentPage, pageSize, records);
    }

    public void updateStatus(long userId, int status) {
        if (status != 0 && status != 1) {
            throw new BizException(ResultCode.PARAM_ERROR, "状态值不合法");
        }
        PlatformUser update = new PlatformUser();
        update.setId(userId);
        update.setStatus(status);
        update.setUpdatedAt(LocalDateTime.now());
        platformUserMapper.updateById(update);

        String key = RedisKeys.disabledUser(userId);
        if (status == 0) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofDays(3650));
        } else {
            redisTemplate.delete(key);
        }
    }

    public void resetPassword(long userId, String password) {
        PlatformUser update = new PlatformUser();
        update.setId(userId);
        update.setPasswordHash(passwordEncoder.encode(password));
        update.setUpdatedAt(LocalDateTime.now());
        platformUserMapper.updateById(update);
        // 让该用户其它端的刷新令牌失效，必须用新密码重新登录
        redisTemplate.delete(RedisKeys.refreshToken(userId));
    }

    /**
     * 重置配额：清零累计用量、解除短链暂停，并清掉对应缓存。
     *
     * <p>套餐上限与计费周期不动——这里只是手工补一份额度，不是换套餐。</p>
     */
    public void resetQuota(long userId) {
        List<String> blockedCodes = quotaAdminMapper.selectBlockedCodes(userId);
        quotaAdminMapper.resetUsage(userId);
        int unblocked = quotaAdminMapper.unblockLinks(userId);

        redisTemplate.delete(RedisKeys.quotaClickTotal(userId));
        redisTemplate.delete(RedisKeys.quotaGenerateTotal(userId));
        redisTemplate.delete(RedisKeys.quotaBlocked(userId));
        redisTemplate.delete(RedisKeys.quotaGenerateDaily(userId, LocalDate.now().toString()));

        // 短链缓存里还存着 quota_blocked=1，不 evict 的话最长要等半小时才恢复
        for (String code : blockedCodes) {
            linkCachePublisher.evict(code);
        }
        log.info("重置用户配额 userId={} unblocked={} cached={}", userId, unblocked, blockedCodes.size());
    }
}
