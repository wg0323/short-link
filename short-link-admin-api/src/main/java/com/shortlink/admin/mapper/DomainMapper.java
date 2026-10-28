package com.shortlink.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.admin.entity.Domain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DomainMapper extends BaseMapper<Domain> {

    @Select("SELECT COUNT(*) FROM short_link WHERE domain_id = #{domainId} AND status != 2")
    long countLinks(@Param("domainId") long domainId);
}
