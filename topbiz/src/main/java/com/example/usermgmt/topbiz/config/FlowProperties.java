package com.example.usermgmt.topbiz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "topbiz.flow")
public class FlowProperties {

    private String registerCode = "register-default";
    private String cancelCode = "cancel-default";
}
