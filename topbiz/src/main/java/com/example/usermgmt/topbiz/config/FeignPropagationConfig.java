package com.example.usermgmt.topbiz.config;

import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.topbiz.context.SessionContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignPropagationConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            SessionUser sessionUser = SessionContext.get();
            if (sessionUser != null) {
                requestTemplate.header("X-User-Id", sessionUser.getUserId());
                if (sessionUser.getRole() != null) {
                    requestTemplate.header("X-User-Role", sessionUser.getRole());
                }
                if (sessionUser.getMenus() != null && !sessionUser.getMenus().isEmpty()) {
                    var menus = sessionUser.getMenus().stream()
                        .filter(menu -> menu != null && !menu.isBlank())
                        .toList();
                    if (!menus.isEmpty()) {
                        requestTemplate.header("X-User-Menus", String.join(",", menus));
                    }
                }
            }
        };
    }
}
