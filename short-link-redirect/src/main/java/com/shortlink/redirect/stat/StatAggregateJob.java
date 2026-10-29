package com.shortlink.redirect.stat;

import com.shortlink.common.constant.RedisKeys;
import com.shortlink.redirect.mapper.StatAggregateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 每日统计聚合：把 Redis 里的当日计数刷进 MySQL 汇总表，然后清掉过期 key。
 *
 * <p>用户端查询「今天」走 Redis 实时数，「昨天及更早」走汇总表，
 * 这样既保证今天的数据随时可见，又不让历史查询扫明细表。</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StatAggregateJob {

    private final StringRedisTemplate redisTemplate;

    private final StatAggregateMapper aggregateMapper;

    @Scheduled(cron = "0 5 0 * * ?")
    public void aggregateYesterday() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            int count = aggregate(yesterday);
            log.info("统计聚合完成 date={} codes={}", yesterday, count);
        } catch (Exception e) {
            log.error("统计聚合失败", e);
        }
    }

    public int aggregate(LocalDate date) {
        String day = date.toString();
        String codesKey = RedisKeys.statCodes(day);
        Set<String> codes = redisTemplate.opsForSet().members(codesKey);
        if (codes == null || codes.isEmpty()) {
            return 0;
        }

        for (String code : codes) {
            long pv = toLong(redisTemplate.opsForValue().get(RedisKeys.statPv(code, day)));
            long uv = hyperLogLogSize(RedisKeys.statUv(code, day));
            long ipCount = hyperLogLogSize(RedisKeys.statIp(code, day));
            aggregateMapper.upsertDaily(code, date, pv, uv, ipCount);

            Map<Object, Object> regions = redisTemplate.opsForHash()
                    .entries(RedisKeys.statRegion(code, day));
            for (Map.Entry<Object, Object> entry : regions.entrySet()) {
                aggregateMapper.upsertRegion(code, date, String.valueOf(entry.getKey()), "",
                        toLong(entry.getValue()), 0L, 0L);
            }
        }

        clearDay(day, codes, codesKey);
        return codes.size();
    }

    private void clearDay(String day, Set<String> codes, String codesKey) {
        List<String> keys = new java.util.ArrayList<>(codes.size() * 4 + 1);
        for (String code : codes) {
            keys.add(RedisKeys.statPv(code, day));
            keys.add(RedisKeys.statUv(code, day));
            keys.add(RedisKeys.statIp(code, day));
            keys.add(RedisKeys.statRegion(code, day));
        }
        keys.add(codesKey);
        redisTemplate.delete(keys);
    }

    private long hyperLogLogSize(String key) {
        Long size = redisTemplate.opsForHyperLogLog().size(key);
        return size == null ? 0L : size;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
