package com.shortlink.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortlink.common.api.PageVO;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.util.IpUtils;
import com.shortlink.user.dto.ClickLogVO;
import com.shortlink.user.dto.StatOverview;
import com.shortlink.user.dto.StatRegionItem;
import com.shortlink.user.dto.StatTrendPoint;
import com.shortlink.user.entity.ShortLink;
import com.shortlink.user.mapper.ShortLinkMapper;
import com.shortlink.user.mapper.StatQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计查询。
 *
 * <p>「今天」走 Redis 实时计数（跳转端写入），「昨天及更早」走 MySQL 汇总表；
 * 跨越今天的时间区间会把两段结果拼起来，用户看到的永远是完整数据。</p>
 */
@Service
@RequiredArgsConstructor
public class StatService {

    private final StatQueryMapper statQueryMapper;

    private final ShortLinkMapper shortLinkMapper;

    private final StringRedisTemplate redisTemplate;

    public StatOverview overview(long userId, String code, String range, LocalDate start, LocalDate end) {
        LocalDate[] window = resolveRange(range, start, end);
        LocalDate today = LocalDate.now();
        long pv = 0;
        long uv = 0;
        long ipCount = 0;

        LocalDate histEnd = window[1].isAfter(today.minusDays(1)) ? today.minusDays(1) : window[1];
        if (!histEnd.isBefore(window[0])) {
            StatTrendPoint hist = statQueryMapper.sumRange(userId, code, window[0], histEnd);
            if (hist != null) {
                pv += hist.getPv();
                uv += hist.getUv();
                ipCount += hist.getIpCount();
            }
        }
        if (coversToday(window)) {
            TodayStat todayStat = loadToday(userId, code);
            pv += todayStat.pv;
            uv += todayStat.uv;
            ipCount += todayStat.ip;
        }
        return new StatOverview(pv, uv, ipCount, countLinks(userId));
    }

    public List<StatTrendPoint> trend(long userId, String code, String range, LocalDate start, LocalDate end) {
        LocalDate[] window = resolveRange(range, start, end);
        LocalDate today = LocalDate.now();

        List<StatTrendPoint> points = new ArrayList<>();
        LocalDate histEnd = window[1].isAfter(today.minusDays(1)) ? today.minusDays(1) : window[1];
        if (!histEnd.isBefore(window[0])) {
            points.addAll(statQueryMapper.groupByDate(userId, code, window[0], histEnd));
        }
        if (coversToday(window)) {
            TodayStat todayStat = loadToday(userId, code);
            StatTrendPoint point = new StatTrendPoint();
            point.setDate(today);
            point.setPv(todayStat.pv);
            point.setUv(todayStat.uv);
            point.setIpCount(todayStat.ip);
            points.add(point);
        }
        return points;
    }

    public List<StatRegionItem> region(long userId, String code, String range, LocalDate start, LocalDate end) {
        LocalDate[] window = resolveRange(range, start, end);
        LocalDate today = LocalDate.now();

        Map<String, Long> merged = new HashMap<>();
        LocalDate histEnd = window[1].isAfter(today.minusDays(1)) ? today.minusDays(1) : window[1];
        if (!histEnd.isBefore(window[0])) {
            for (StatRegionItem item : statQueryMapper.regionRank(userId, code, window[0], histEnd, 50)) {
                merged.merge(item.getProvince(), item.getPv(), Long::sum);
            }
        }
        if (coversToday(window)) {
            loadTodayRegion(userId, code).forEach((province, pv) -> merged.merge(province, pv, Long::sum));
        }

        return merged.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(entry -> {
                    StatRegionItem item = new StatRegionItem();
                    item.setProvince(entry.getKey());
                    item.setPv(entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    public PageVO<ClickLogVO> clickPage(long userId, String code, String range,
                                        LocalDate start, LocalDate end, int page, int size) {
        LocalDate[] window = resolveRange(range, start, end);
        LocalDateTime begin = window[0].atStartOfDay();
        LocalDateTime finish = window[1].plusDays(1).atStartOfDay();
        int currentPage = Math.max(page, 1);
        int pageSize = size < 1 ? 20 : Math.min(size, 100);

        long total = statQueryMapper.countClicks(userId, code, begin, finish);
        List<ClickLogVO> rows = statQueryMapper.pageClicks(userId, code, begin, finish,
                (currentPage - 1) * pageSize, pageSize);
        rows.forEach(row -> row.setIp(IpUtils.mask(row.getIp())));
        return new PageVO<>(total, currentPage, pageSize, rows);
    }

    private boolean coversToday(LocalDate[] window) {
        LocalDate today = LocalDate.now();
        return !window[0].isAfter(today) && !window[1].isBefore(today);
    }

    private LocalDate[] resolveRange(String range, LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        if ("today".equalsIgnoreCase(range)) {
            return new LocalDate[]{today, today};
        }
        if ("yesterday".equalsIgnoreCase(range)) {
            LocalDate yesterday = today.minusDays(1);
            return new LocalDate[]{yesterday, yesterday};
        }
        if ("custom".equalsIgnoreCase(range) && start != null && end != null && !end.isBefore(start)) {
            return new LocalDate[]{start, end};
        }
        return new LocalDate[]{today.minusDays(29), today};
    }

    private TodayStat loadToday(long userId, String code) {
        List<String> codes = codesOf(userId, code);
        if (codes.isEmpty()) {
            return new TodayStat();
        }
        String day = LocalDate.now().toString();
        long pv = 0;
        String[] uvKeys = new String[codes.size()];
        String[] ipKeys = new String[codes.size()];
        for (int i = 0; i < codes.size(); i++) {
            String c = codes.get(i);
            pv += parseLong(redisTemplate.opsForValue().get(RedisKeys.statPv(c, day)));
            uvKeys[i] = RedisKeys.statUv(c, day);
            ipKeys[i] = RedisKeys.statIp(c, day);
        }
        TodayStat stat = new TodayStat();
        stat.pv = pv;
        Long uv = redisTemplate.opsForHyperLogLog().size(uvKeys);
        Long ip = redisTemplate.opsForHyperLogLog().size(ipKeys);
        stat.uv = uv == null ? 0 : uv;
        stat.ip = ip == null ? 0 : ip;
        return stat;
    }

    private Map<String, Long> loadTodayRegion(long userId, String code) {
        Map<String, Long> merged = new HashMap<>();
        String day = LocalDate.now().toString();
        for (String c : codesOf(userId, code)) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(RedisKeys.statRegion(c, day));
            entries.forEach((province, pv) ->
                    merged.merge(String.valueOf(province), parseLong(pv), Long::sum));
        }
        return merged;
    }

    private List<String> codesOf(long userId, String code) {
        LambdaQueryWrapper<ShortLink> wrapper = new LambdaQueryWrapper<ShortLink>()
                .eq(ShortLink::getUserId, userId)
                .ne(ShortLink::getStatus, ShortLink.STATUS_DELETED);
        if (StringUtils.hasText(code)) {
            wrapper.eq(ShortLink::getCode, code);
        }
        return shortLinkMapper.selectList(wrapper).stream()
                .map(ShortLink::getCode)
                .collect(Collectors.toList());
    }

    private long countLinks(long userId) {
        Long count = shortLinkMapper.selectCount(new LambdaQueryWrapper<ShortLink>()
                .eq(ShortLink::getUserId, userId)
                .ne(ShortLink::getStatus, ShortLink.STATUS_DELETED));
        return count == null ? 0 : count;
    }

    private long parseLong(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static class TodayStat {

        private long pv;

        private long uv;

        private long ip;
    }
}
