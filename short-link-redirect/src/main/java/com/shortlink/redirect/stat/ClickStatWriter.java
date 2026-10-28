package com.shortlink.redirect.stat;

import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.util.DigestUtils;
import com.shortlink.redirect.event.ClickEvent;
import com.shortlink.redirect.mapper.ClickLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把一批点击同时写进 Redis 计数与 MySQL 明细。
 *
 * <p>Redis 侧用 pipeline 一次提交，避免每条点击都产生四次往返；
 * 明细写库失败不影响计数，下一批继续。</p>
 */
@Component
public class ClickStatWriter {

    private static final Logger log = LoggerFactory.getLogger(ClickStatWriter.class);

    private final StringRedisTemplate redisTemplate;

    private final ClickLogMapper clickLogMapper;

    public ClickStatWriter(StringRedisTemplate redisTemplate, ClickLogMapper clickLogMapper) {
        this.redisTemplate = redisTemplate;
        this.clickLogMapper = clickLogMapper;
    }

    public void write(List<ClickEvent> events) {
        if (events.isEmpty()) {
            return;
        }
        countInRedis(events);
        insertDetail(events);
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
