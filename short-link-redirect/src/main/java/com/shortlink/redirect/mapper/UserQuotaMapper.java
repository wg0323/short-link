package com.shortlink.redirect.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserQuotaMapper {

    /**
     * 取用户的累计访问上限，null 表示不限。
     */
    @Select("SELECT total_click FROM user_quota WHERE user_id = #{userId}")
    Long selectTotalClick(@Param("userId") long userId);
}
