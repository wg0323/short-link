package com.shortlink.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.user.entity.UserQuota;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface UserQuotaMapper extends BaseMapper<UserQuota> {

    /**
     * 用 GREATEST 覆盖：Redis 丢过数据时只会保留更大的历史值，不会把已用量改小。
     */
    @Update("UPDATE user_quota SET "
            + "used_generate = GREATEST(used_generate, #{usedGenerate}), "
            + "used_click = GREATEST(used_click, #{usedClick}) "
            + "WHERE user_id = #{userId}")
    int updateUsage(@Param("userId") long userId, @Param("usedGenerate") long usedGenerate,
                    @Param("usedClick") long usedClick);

    @Insert("INSERT INTO user_quota_daily (user_id, stat_date, generate_count) "
            + "VALUES (#{userId}, #{date}, #{generated}) "
            + "ON DUPLICATE KEY UPDATE generate_count = GREATEST(generate_count, #{generated})")
    int upsertDaily(@Param("userId") long userId, @Param("date") LocalDate date,
                    @Param("generated") long generated);
}
