package com.example.usermgmt.topbiz.config;

import com.example.usermgmt.topbiz.shiro.SessionRealm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfig {

    @Bean
    public SecurityManager securityManager(SessionRealm sessionRealm) {
        DefaultSecurityManager securityManager = new DefaultSecurityManager(sessionRealm);
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }
}
