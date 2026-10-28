package com.shortlink.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.user.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLink> {
}
