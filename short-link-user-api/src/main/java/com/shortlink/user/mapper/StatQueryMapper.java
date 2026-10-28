package com.shortlink.user.mapper;

import com.shortlink.user.dto.StatTrendPoint;
import com.shortlink.user.dto.StatRegionItem;
import com.shortlink.user.dto.ClickLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计查询：历史数据走汇总表，并与短链表关联到用户维度。
 *
 * <p>已删除的短链不计入统计，因此统一带上 {@code l.status <> 2}。</p>
 */
@Mapper
public interface StatQueryMapper {

    @Select("SELECT COALESCE(SUM(d.pv), 0) AS pv, COALESCE(SUM(d.uv), 0) AS uv, "
            + "COALESCE(SUM(d.ip_count), 0) AS ipCount "
            + "FROM link_stat_daily d JOIN short_link l ON l.code = d.code "
            + "WHERE l.user_id = #{userId} AND l.status <> 2 "
            + "AND d.stat_date BETWEEN #{start} AND #{end} "
            + "AND (#{code} IS NULL OR d.code = #{code})")
    StatTrendPoint sumRange(@Param("userId") long userId, @Param("code") String code,
                            @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("SELECT d.stat_date AS date, COALESCE(SUM(d.pv), 0) AS pv, "
            + "COALESCE(SUM(d.uv), 0) AS uv, COALESCE(SUM(d.ip_count), 0) AS ipCount "
            + "FROM link_stat_daily d JOIN short_link l ON l.code = d.code "
            + "WHERE l.user_id = #{userId} AND l.status <> 2 "
            + "AND d.stat_date BETWEEN #{start} AND #{end} "
            + "AND (#{code} IS NULL OR d.code = #{code}) "
            + "GROUP BY d.stat_date ORDER BY d.stat_date")
    List<StatTrendPoint> groupByDate(@Param("userId") long userId, @Param("code") String code,
                                     @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("SELECT r.province AS province, COALESCE(SUM(r.pv), 0) AS pv "
            + "FROM link_stat_region_daily r JOIN short_link l ON l.code = r.code "
            + "WHERE l.user_id = #{userId} AND l.status <> 2 "
            + "AND r.stat_date BETWEEN #{start} AND #{end} "
            + "AND (#{code} IS NULL OR r.code = #{code}) "
            + "GROUP BY r.province ORDER BY pv DESC LIMIT #{limit}")
    List<StatRegionItem> regionRank(@Param("userId") long userId, @Param("code") String code,
                                    @Param("start") LocalDate start, @Param("end") LocalDate end,
                                    @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM click_log c JOIN short_link l ON l.code = c.code "
            + "WHERE l.user_id = #{userId} AND l.status <> 2 "
            + "AND c.click_at >= #{start} AND c.click_at < #{end} "
            + "AND (#{code} IS NULL OR c.code = #{code})")
    long countClicks(@Param("userId") long userId, @Param("code") String code,
                     @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT c.click_at AS clickAt, c.code AS code, c.ip AS ip, c.province AS province, "
            + "c.city AS city, c.device AS device, c.referer AS referer "
            + "FROM click_log c JOIN short_link l ON l.code = c.code "
            + "WHERE l.user_id = #{userId} AND l.status <> 2 "
            + "AND c.click_at >= #{start} AND c.click_at < #{end} "
            + "AND (#{code} IS NULL OR c.code = #{code}) "
            + "ORDER BY c.click_at DESC LIMIT #{offset}, #{size}")
    List<ClickLogVO> pageClicks(@Param("userId") long userId, @Param("code") String code,
                                @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                @Param("offset") int offset, @Param("size") int size);
}
