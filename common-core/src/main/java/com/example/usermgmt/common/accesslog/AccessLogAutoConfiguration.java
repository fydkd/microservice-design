package com.example.usermgmt.common.accesslog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerInterceptor;

@AutoConfiguration
@ConditionalOnClass(HandlerInterceptor.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(AccessLogProperties.class)
public class AccessLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "access-log", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AccessLogInterceptor accessLogInterceptor(
        ObjectMapper objectMapper,
        AccessLogProperties properties,
        Environment environment
    ) {
        return new AccessLogInterceptor(objectMapper, properties, environment);
    }
}
