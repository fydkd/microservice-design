package com.example.usermgmt.topbiz.dto;

import lombok.Data;

@Data
public class FeishuUserInfo {

    private String openId;
    private String unionId;
    private String userId;
    private String name;
    private String avatarUrl;
    private String avatarThumb;
    private String avatarMiddle;
    private String avatarBig;
    private String email;
    private String enterpriseEmail;
    private String mobile;
}
