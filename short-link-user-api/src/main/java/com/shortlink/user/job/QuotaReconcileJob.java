package com.shortlink.user.job;

import com.shortlink.common.constant.RedisKeys;
import com.shortlink.user.entity.UserQuota;
import com.shortlink.user.mapper.UserQuotaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 配额对账：把 Redis 里的实时用量写回 MySQL 权威值。
 *
 * <p>Redis 只做实时计数，丢了不影响业务；这里每天把累计值快照一次，
 * 顺带把昨日的日计数落到 {@code user_quota_daily}，便于核对和追溯。</p>
 *
 * <p>多实例同时跑不会算错：覆盖用 GREATEST，日计数用 upsert，都是幂等的。</p>
 */
@Component
public class QuotaReconcileJob {

    private static final Logger log = LoggerFactory.getLogger(QuotaReconcileJob.class);

    private final UserQuotaMapper userQuotaMapper;

    private final StringRedisTemplate redisTemplate;

    public QuotaReconcileJob(UserQuotaMapper userQuotaMapper, StringRedisTemplate redisTemplate) {
        this.userQuotaMapper = userQuotaMapper;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "${shortlink.quota.reconcile-cron:0 10 0 * * ?}")
    public void reconcileYesterday() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            int users = reconcile(yesterday);
            log.info("配额对账完成 date={} users={}", yesterday, users);
        } catch (Exception e) {
            log.error("配额对账失败", e);
        }
    }

    /**
     * 对账指定日期。用户量上来后应改成分页或按 dirty 集合遍历。
     */
    public int reconcile(LocalDate date) {
        String day = date.toString();
        List<UserQuota> quotas = userQuotaMapper.selectList(null);
        int touched = 0;
        for (UserQuota quota : quotas) {
            long usedGenerate = readLong(RedisKeys.quotaGenerateTotal(quota.getUserId()));
            long usedClick = readLong(RedisKeys.quotaClickTotal(quota.getUserId()));
            if (usedGenerate > 0 || usedClick > 0) {
                userQuotaMapper.updateUsage(quota.getUserId(), usedGenerate, usedClick);
                touched++;
            }
            long dailyGenerated = readLong(RedisKeys.quotaGenerateDaily(quota.getUserId(), day));
            if (dailyGenerated > 0) {
                userQuotaMapper.upsertDaily(quota.getUserId(), date, dailyGenerated);
            }
        }
        return touched;
    }

    private long readLong(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
