package com.shortlink.redirect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.redirect.entity.RedirectLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RedirectLinkMapper extends BaseMapper<RedirectLink> {

    @Select("SELECT id, code, origin_url, password_hash, start_at, end_at, expire_at, status "
            + "FROM short_link WHERE code = #{code} LIMIT 1")
    RedirectLink selectByCode(@Param("code") String code);

    /**
     * 分批拉取短码（带 id 用于游标翻页），启动时用于构建布隆过滤器。
     */
    @Select("SELECT id, code FROM short_link WHERE id > #{lastId} ORDER BY id LIMIT #{limit}")
    List<RedirectLink> selectCodesAfter(@Param("lastId") long lastId, @Param("limit") int limit);
}
