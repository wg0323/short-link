package com.shortlink.user.service;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.exception.BizException;
import com.shortlink.user.entity.UserQuota;
import com.shortlink.user.mapper.UserQuotaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 用户配额：初始化、生成额度校验。
 *
 * <p>权威值在 {@code user_quota}，实时计数在 Redis，判断与自增用 Lua 保证原子。
 * 免费额度先取固定值，接入支付后改为读 {@code quota_package} + 下单快照。</p>
 */
@Service
public class QuotaService {

    private static final Logger log = LoggerFactory.getLogger(QuotaService.class);

    private static final String FREE_PACKAGE = "free";

    private static final long FREE_DAILY_GENERATE = 100L;

    /**
     * 返回 0 放行，1 日限超了，2 总量超了。
     * 阈值传空串表示该项不限，对应计数仍然累加，方便以后加限制时历史数据是准的。
     */
    private static final String CONSUME_SCRIPT =
            "local dailyLimit = ARGV[1]\n"
                    + "local totalLimit = ARGV[2]\n"
                    + "if dailyLimit ~= '' then\n"
                    + "  local daily = tonumber(redis.call('GET', KEYS[1]) or '0')\n"
                    + "  if daily >= tonumber(dailyLimit) then return 1 end\n"
                    + "end\n"
                    + "if totalLimit ~= '' then\n"
                    + "  local total = tonumber(redis.call('GET', KEYS[2]) or '0')\n"
                    + "  if total >= tonumber(totalLimit) then return 2 end\n"
                    + "end\n"
                    + "redis.call('INCR', KEYS[1])\n"
                    + "redis.call('EXPIRE', KEYS[1], ARGV[3])\n"
                    + "redis.call('INCR', KEYS[2])\n"
                    + "return 0";

    /** 退还一次额度，计数不会被减成负数。 */
    private static final String REFUND_SCRIPT =
            "for i = 1, #KEYS do\n"
                    + "  local value = tonumber(redis.call('GET', KEYS[i]) or '0')\n"
                    + "  if value > 0 then redis.call('DECR', KEYS[i]) end\n"
                    + "end\n"
                    + "return 0";

    private final UserQuotaMapper userQuotaMapper;

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> consumeScript;

    private final DefaultRedisScript<Long> refundScript;

    public QuotaService(UserQuotaMapper userQuotaMapper, StringRedisTemplate redisTemplate) {
        this.userQuotaMapper = userQuotaMapper;
        this.redisTemplate = redisTemplate;
        this.consumeScript = new DefaultRedisScript<>(CONSUME_SCRIPT, Long.class);
        this.refundScript = new DefaultRedisScript<>(REFUND_SCRIPT, Long.class);
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

    /**
     * 消费一次新建额度，额度不足直接抛业务异常。
     */
    public void consumeGenerate(long userId) {
        UserQuota quota = userQuotaMapper.selectById(userId);
        if (quota == null) {
            // 配额是附加能力，记录缺失时放行，不能把正常业务挡住
            log.warn("用户配额记录缺失，放行 userId={}", userId);
            return;
        }
        Long result = redisTemplate.execute(consumeScript, generateKeys(userId),
                limitArg(quota.getDailyGenerate()),
                limitArg(quota.getTotalGenerate()),
                String.valueOf(secondsToNextDay()));
        if (result == null || result == 0L) {
            return;
        }
        if (result == 1L) {
            throw new BizException(ResultCode.QUOTA_DAILY_EXCEEDED);
        }
        throw new BizException(ResultCode.QUOTA_TOTAL_EXCEEDED);
    }

    /**
     * 退还一次新建额度：短码冲突或写库失败时调用，避免额度白扣。
     */
    public void refundGenerate(long userId) {
        try {
            redisTemplate.execute(refundScript, generateKeys(userId));
        } catch (Exception e) {
            log.warn("退还新建额度失败 userId={}", userId, e);
        }
    }

    private List<String> generateKeys(long userId) {
        return Arrays.asList(
                RedisKeys.quotaGenerateDaily(userId, LocalDate.now().toString()),
                RedisKeys.quotaGenerateTotal(userId));
    }

    private String limitArg(Long limit) {
        return limit == null ? "" : String.valueOf(limit);
    }

    /** 日键保留到次日 01:00，避免刚过零点就把计数清零造成的边界抖动。 */
    private long secondsToNextDay() {
        LocalDateTime next = LocalDate.now().plusDays(1).atTime(1, 0);
        return Math.max(3600L, Duration.between(LocalDateTime.now(), next).getSeconds());
    }
}
