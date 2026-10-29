package com.shortlink.redirect.stat;

import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.util.DigestUtils;
import com.shortlink.redirect.event.ClickEvent;
import com.shortlink.redirect.mapper.ClickLogMapper;
import com.shortlink.redirect.mapper.RedirectLinkMapper;
import com.shortlink.redirect.support.QuotaLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 把一批点击同时写进 Redis 计数与 MySQL 明细。
 *
 * <p>Redis 侧用 pipeline 一次提交，避免每条点击都产生四次往返；
 * 明细写库失败不影响计数，下一批继续。</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClickStatWriter {

    private final StringRedisTemplate redisTemplate;

    private final ClickLogMapper clickLogMapper;

    private final RedirectLinkMapper linkMapper;

    private final QuotaLimitService quotaLimitService;

    public void write(List<ClickEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        countInRedis(events);
        checkQuota(events);
        insertDetail(events);
        updateClickCount(events);
    }

    /**
     * 累计点击跨过套餐上限时暂停该用户的短链，异常不影响统计写入。
     */
    private void checkQuota(List<ClickEvent> events) {
        try {
            Set<Long> userIds = events.stream()
                    .map(ClickEvent::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!userIds.isEmpty()) {
                quotaLimitService.check(userIds);
            }
        } catch (Exception e) {
            log.warn("配额检查失败，条数={}", events.size(), e);
        }
    }

    private void countInRedis(List<ClickEvent> events) {
        Map<String, List<ClickEvent>> byDate = new HashMap<>();
        for (ClickEvent event : events) {
            byDate.computeIfAbsent(event.getClickAt().toLocalDate().toString(),
                    key -> new ArrayList<>()).add(event);
        }

        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                byDate.forEach((date, list) -> pipeline(connection, date, list));
                return null;
            });
        } catch (Exception e) {
            log.warn("统计计数写入 Redis 失败，条数={}", events.size(), e);
        }
    }

    private void pipeline(RedisConnection connection, String date, List<ClickEvent> list) {
        for (ClickEvent event : list) {
            String code = event.getCode();
            connection.incr(key(RedisKeys.statPv(code, date)));
            connection.pfAdd(key(RedisKeys.statUv(code, date)), visitorId(event));
            if (event.getIp() != null && !event.getIp().isEmpty()) {
                connection.pfAdd(key(RedisKeys.statIp(code, date)), bytes(event.getIp()));
            }
            String province = event.getProvince() == null || event.getProvince().isEmpty()
                    ? "未知" : event.getProvince();
            connection.hIncrBy(key(RedisKeys.statRegion(code, date)), bytes(province), 1);
            // 配额计数：只累计不拦截，跨阈值后的处理在下一阶段接入
            if (event.getUserId() != null) {
                connection.incrBy(key(RedisKeys.quotaClickTotal(event.getUserId())), 1L);
            }
            // 维护当天的短码集合，供次日聚合任务遍历（避免用 KEYS 扫描）
            connection.sAdd(key(RedisKeys.statCodes(date)), bytes(code));
        }
    }

    private void insertDetail(List<ClickEvent> events) {
        try {
            clickLogMapper.insertBatch(events);
        } catch (Exception e) {
            log.warn("访问明细写库失败，条数={}", events.size(), e);
        }
    }

    /**
     * 回写 short_link.click_count：列表页展示的是这个冗余字段，不随明细自动变化。
     * 同一批里同一短码只发一条 UPDATE，避免按条点击逐条更新。
     */
    private void updateClickCount(List<ClickEvent> events) {
        Map<String, Long> delta = new HashMap<>();
        for (ClickEvent event : events) {
            delta.merge(event.getCode(), 1L, Long::sum);
        }
        try {
            delta.forEach(linkMapper::increaseClickCount);
        } catch (Exception e) {
            log.warn("短链累计点击更新失败，条数={}", events.size(), e);
        }
    }

    /**
     * UV 用「IP + User-Agent」的摘要做访客标识：同一出口 IP 的不同设备算不同访客。
     */
    private byte[] visitorId(ClickEvent event) {
        return bytes(DigestUtils.sha256Hex(
                (event.getIp() == null ? "" : event.getIp()) + "|"
                        + (event.getUa() == null ? "" : event.getUa())));
    }

    private byte[] key(String key) {
        return bytes(key);
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
