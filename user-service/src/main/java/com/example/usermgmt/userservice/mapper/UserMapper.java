package com.example.usermgmt.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.usermgmt.userservice.entity.UserEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("""
        SELECT * FROM `user`
        WHERE phone = #{account}
           OR mail = #{account}
           OR name = #{account}
        LIMIT 1
        """)
    UserEntity selectByAccount(@Param("account") String account);

    @Select("""
        SELECT *
        FROM `user`
        WHERE JSON_UNQUOTE(JSON_EXTRACT(extra_data, '$.feishuOpenId')) = #{openId}
        LIMIT 1
        """)
    UserEntity selectByFeishuOpenId(@Param("openId") String openId);

    @Select("""
        <script>
        SELECT COUNT(1)
        FROM `user`
        WHERE 1 = 1
        <if test="keyword != null and keyword != ''">
          AND (name LIKE CONCAT('%', #{keyword}, '%')
            OR phone LIKE CONCAT('%', #{keyword}, '%')
            OR mail LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <choose>
          <when test="status != null">
            AND status = #{status}
          </when>
          <otherwise>
            AND status &lt;&gt; #{excludedStatus}
          </otherwise>
        </choose>
        <if test="role != null and role != ''">
          AND JSON_UNQUOTE(JSON_EXTRACT(extra_data, '$.role')) = #{role}
        </if>
        </script>
        """)
    Long countUsers(
        @Param("keyword") String keyword,
        @Param("status") Integer status,
        @Param("role") String role,
        @Param("excludedStatus") Integer excludedStatus
    );

    @Select("""
        <script>
        SELECT *
        FROM `user`
        WHERE 1 = 1
        <if test="keyword != null and keyword != ''">
          AND (name LIKE CONCAT('%', #{keyword}, '%')
            OR phone LIKE CONCAT('%', #{keyword}, '%')
            OR mail LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <choose>
          <when test="status != null">
            AND status = #{status}
          </when>
          <otherwise>
            AND status &lt;&gt; #{excludedStatus}
          </otherwise>
        </choose>
        <if test="role != null and role != ''">
          AND JSON_UNQUOTE(JSON_EXTRACT(extra_data, '$.role')) = #{role}
        </if>
        ORDER BY create_time DESC
        LIMIT #{offset}, #{pageSize}
        </script>
        """)
    List<UserEntity> selectUsers(
        @Param("keyword") String keyword,
        @Param("status") Integer status,
        @Param("role") String role,
        @Param("excludedStatus") Integer excludedStatus,
        @Param("offset") long offset,
        @Param("pageSize") int pageSize
    );

    @Select("""
        <script>
        SELECT COUNT(1)
        FROM `user`
        WHERE phone = #{phone}
        <if test="excludeId != null and excludeId != ''">
          AND id &lt;&gt; #{excludeId}
        </if>
        </script>
        """)
    Integer countByPhone(@Param("phone") String phone, @Param("excludeId") String excludeId);

    @Select("""
        <script>
        SELECT COUNT(1)
        FROM `user`
        WHERE mail = #{mail}
        <if test="excludeId != null and excludeId != ''">
          AND id &lt;&gt; #{excludeId}
        </if>
        </script>
        """)
    Integer countByMail(@Param("mail") String mail, @Param("excludeId") String excludeId);
}
