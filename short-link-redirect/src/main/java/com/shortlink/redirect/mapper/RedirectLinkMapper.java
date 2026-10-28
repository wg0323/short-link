package com.shortlink.redirect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.redirect.entity.RedirectLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RedirectLinkMapper extends BaseMapper<RedirectLink> {

    @Select("SELECT id, code, user_id, origin_url, password_hash, start_at, end_at, expire_at, status, "
            + "quota_blocked FROM short_link WHERE code = #{code} LIMIT 1")
    RedirectLink selectByCode(@Param("code") String code);

    /**
     * 分批拉取短码（带 id 用于游标翻页），启动时用于构建布隆过滤器。
     */
    @Select("SELECT id, code FROM short_link WHERE id > #{lastId} ORDER BY id LIMIT #{limit}")
    List<RedirectLink> selectCodesAfter(@Param("lastId") long lastId, @Param("limit") int limit);

    /**
     * 把某个用户的在线短链全部置为配额暂停。
     */
    @Update("UPDATE short_link SET quota_blocked = 1 "
            + "WHERE user_id = #{userId} AND status = 1 AND quota_blocked = 0")
    int blockByUser(@Param("userId") long userId);

    /**
     * 取已暂停的短码，用于清掉它们的 Redis 缓存。
     */
    @Select("SELECT code FROM short_link WHERE user_id = #{userId} AND quota_blocked = 1 LIMIT #{limit}")
    List<String> selectBlockedCodes(@Param("userId") long userId, @Param("limit") int limit);
}
