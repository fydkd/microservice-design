package com.example.usermgmt.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName(value = "`user`")
public class UserEntity {

    @TableId(type = IdType.INPUT)
    private String id;
    private String name;
    private String phone;
    private String mail;
    private String password;
    private String salt;
    private Integer gender;
    private String description;
    private String extension;
    private Integer status;

    @TableField("creator_id")
    private String creatorId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("last_update_time")
    private LocalDateTime lastUpdateTime;

    @TableField("last_operator_id")
    private String lastOperatorId;

    @TableField("extra_data")
    private String extraData;
}
