package com.shortlink.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortlink.admin.dto.AdminUserVO;
import com.shortlink.admin.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    @Select("<script>"
            + "SELECT u.id, u.phone, u.nickname, u.status, u.created_at, u.last_login_at, "
            + "(SELECT COUNT(*) FROM short_link l WHERE l.user_id = u.id AND l.status != 2) AS link_count "
            + "FROM `user` u "
            + "<where>"
            + "<if test='keyword != null and keyword != \"\"'>"
            + " AND (u.phone LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))"
            + "</if>"
            + "<if test='status != null'> AND u.status = #{status}</if>"
            + "</where>"
            + " ORDER BY u.id DESC LIMIT #{offset}, #{size}"
            + "</script>")
    List<AdminUserVO> pageUsers(@Param("keyword") String keyword, @Param("status") Integer status,
                                @Param("offset") int offset, @Param("size") int size);

    @Select("<script>"
            + "SELECT COUNT(*) FROM `user` u "
            + "<where>"
            + "<if test='keyword != null and keyword != \"\"'>"
            + " AND (u.phone LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))"
            + "</if>"
            + "<if test='status != null'> AND u.status = #{status}</if>"
            + "</where>"
            + "</script>")
    long countUsers(@Param("keyword") String keyword, @Param("status") Integer status);
}
