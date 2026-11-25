package com.example.usermgmt.notification.config;

import com.example.usermgmt.common.accesslog.AccessLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectProvider<AccessLogInterceptor> accessLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        accessLogInterceptor.ifAvailable(interceptor -> registry.addInterceptor(interceptor).addPathPatterns("/**"));
    }
}
