package com.shortlink.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 配额相关的运维操作：重置用量、解除短链暂停。
 */
@Mapper
public interface QuotaAdminMapper {

    @Update("UPDATE user_quota SET used_generate = 0, used_click = 0, status = 1 "
            + "WHERE user_id = #{userId}")
    int resetUsage(@Param("userId") long userId);

    @Update("UPDATE short_link SET quota_blocked = 0 "
            + "WHERE user_id = #{userId} AND quota_blocked = 1")
    int unblockLinks(@Param("userId") long userId);

    @Select("SELECT code FROM short_link WHERE user_id = #{userId} AND quota_blocked = 1")
    List<String> selectBlockedCodes(@Param("userId") long userId);
}
