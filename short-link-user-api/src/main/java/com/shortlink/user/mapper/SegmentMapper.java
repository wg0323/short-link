package com.shortlink.user.mapper;

import com.shortlink.user.entity.Segment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 号段表访问。
 *
 * <p>取号必须在同一个事务内完成「锁定行 + 推进 max_id」，
 * 否则并发实例会拿到重叠的号段、生成重复短码。</p>
 */
@Mapper
public interface SegmentMapper {

    @Select("SELECT biz_tag, max_id, step FROM segment WHERE biz_tag = #{bizTag} FOR UPDATE")
    Segment selectForUpdate(@Param("bizTag") String bizTag);

    @Update("UPDATE segment SET max_id = max_id + #{step} WHERE biz_tag = #{bizTag}")
    int increase(@Param("bizTag") String bizTag, @Param("step") int step);
}
