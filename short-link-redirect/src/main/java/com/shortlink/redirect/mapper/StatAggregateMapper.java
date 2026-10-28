package com.shortlink.redirect.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface StatAggregateMapper {

    @Insert("INSERT INTO link_stat_daily (code, stat_date, pv, uv, ip_count) "
            + "VALUES (#{code}, #{date}, #{pv}, #{uv}, #{ipCount}) "
            + "ON DUPLICATE KEY UPDATE pv = VALUES(pv), uv = VALUES(uv), ip_count = VALUES(ip_count)")
    int upsertDaily(@Param("code") String code, @Param("date") LocalDate date,
                    @Param("pv") long pv, @Param("uv") long uv, @Param("ipCount") long ipCount);

    @Insert("INSERT INTO link_stat_region_daily (code, stat_date, province, city, pv, uv, ip_count) "
            + "VALUES (#{code}, #{date}, #{province}, #{city}, #{pv}, #{uv}, #{ipCount}) "
            + "ON DUPLICATE KEY UPDATE pv = VALUES(pv), uv = VALUES(uv), ip_count = VALUES(ip_count)")
    int upsertRegion(@Param("code") String code, @Param("date") LocalDate date,
                     @Param("province") String province, @Param("city") String city,
                     @Param("pv") long pv, @Param("uv") long uv, @Param("ipCount") long ipCount);
}
