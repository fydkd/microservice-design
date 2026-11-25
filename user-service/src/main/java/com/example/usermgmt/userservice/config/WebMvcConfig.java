package com.example.usermgmt.userservice.config;

import com.example.usermgmt.common.accesslog.AccessLogInterceptor;
import com.example.usermgmt.userservice.context.CurrentUserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserInterceptor currentUserInterceptor;
    private final ObjectProvider<AccessLogInterceptor> accessLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(currentUserInterceptor).addPathPatterns("/**");
        accessLogInterceptor.ifAvailable(interceptor -> registry.addInterceptor(interceptor).addPathPatterns("/**"));
    }
}
