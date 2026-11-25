package com.example.usermgmt.logservice.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@RequiredArgsConstructor
public class AlertStoreConfig {

    private final LogServiceProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "log-service.alert", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DataSource alertDataSource() {
        LogServiceProperties.Alert alert = properties.getAlert();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(alert.getDriverClassName());
        dataSource.setUrl(alert.getJdbcUrl());
        dataSource.setUsername(alert.getUsername());
        dataSource.setPassword(alert.getPassword());
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "log-service.alert", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JdbcTemplate alertJdbcTemplate(@Qualifier("alertDataSource") DataSource alertDataSource) {
        return new JdbcTemplate(alertDataSource);
    }
}
