package com.shortlink.user.service;

import com.shortlink.user.entity.UserQuota;
import com.shortlink.user.mapper.UserQuotaMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 用户配额：初始化与查询。
 *
 * <p>本阶段只做数据准备，不做拦截。免费额度先取固定值，
 * 接入支付后改为读 {@code quota_package} + 下单快照。</p>
 */
@Service
public class QuotaService {

    private static final String FREE_PACKAGE = "free";

    private static final long FREE_DAILY_GENERATE = 100L;

    private final UserQuotaMapper userQuotaMapper;

    public QuotaService(UserQuotaMapper userQuotaMapper) {
        this.userQuotaMapper = userQuotaMapper;
    }

    /**
     * 新注册用户统一发免费套餐：总量不限、每日 100 条新建。
     */
    public void initForNewUser(long userId) {
        UserQuota quota = new UserQuota();
        quota.setUserId(userId);
        quota.setPackageCode(FREE_PACKAGE);
        quota.setDailyGenerate(FREE_DAILY_GENERATE);
        quota.setUsedGenerate(0L);
        quota.setUsedClick(0L);
        quota.setPeriodStart(LocalDate.now());
        quota.setStatus(1);
        userQuotaMapper.insert(quota);
    }
}
