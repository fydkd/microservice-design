package com.example.usermgmt.logservice.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@RequiredArgsConstructor
public class ClickHouseConfig {

    private final LogServiceProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "log-service.clickhouse", name = "enabled", havingValue = "true")
    public DataSource clickHouseDataSource() {
        LogServiceProperties.ClickHouse clickHouse = properties.getClickhouse();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(clickHouse.getDriverClassName());
        dataSource.setUrl(clickHouse.getUrl());
        dataSource.setUsername(clickHouse.getUsername());
        dataSource.setPassword(clickHouse.getPassword());
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "log-service.clickhouse", name = "enabled", havingValue = "true")
    public JdbcTemplate clickHouseJdbcTemplate(@Qualifier("clickHouseDataSource") DataSource clickHouseDataSource) {
        return new JdbcTemplate(clickHouseDataSource);
    }

    @Bean
    @ConditionalOnProperty(prefix = "log-service.clickhouse", name = "enabled", havingValue = "true")
    public DataSourceInitializer clickHouseSchemaInitializer(
        @Qualifier("clickHouseDataSource") DataSource clickHouseDataSource,
        @Value("classpath:collector/access_log_all.sql") Resource schemaScript
    ) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(schemaScript);
        populator.setSeparator(";");
        populator.setContinueOnError(false);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(clickHouseDataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
