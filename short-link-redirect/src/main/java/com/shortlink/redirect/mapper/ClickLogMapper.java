package com.shortlink.redirect.mapper;

import com.shortlink.redirect.event.ClickEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClickLogMapper {

    @Insert("<script>"
            + "INSERT INTO click_log (code, click_at, ip, province, city, device, referer, ua) VALUES "
            + "<foreach collection='list' item='item' separator=','>"
            + "(#{item.code}, #{item.clickAt}, #{item.ip}, #{item.province}, #{item.city}, "
            + "#{item.device}, #{item.referer}, #{item.ua})"
            + "</foreach>"
            + "</script>")
    int insertBatch(@Param("list") List<ClickEvent> events);
}
