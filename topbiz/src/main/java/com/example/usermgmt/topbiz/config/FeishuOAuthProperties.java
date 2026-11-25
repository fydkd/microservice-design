package com.example.usermgmt.topbiz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "topbiz.oauth.feishu")
public class FeishuOAuthProperties {

    private boolean enabled = true;
    private String appId = "";
    private String appSecret = "";
    private String authorizeUrl = "https://open.feishu.cn/open-apis/authen/v1/authorize";
    private String appAccessTokenUrl = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal";
    private String userAccessTokenUrl = "https://open.feishu.cn/open-apis/authen/v1/access_token";
    private String userInfoUrl = "https://open.feishu.cn/open-apis/authen/v1/user_info";
}
