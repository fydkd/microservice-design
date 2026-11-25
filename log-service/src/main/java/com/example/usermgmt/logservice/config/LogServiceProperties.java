package com.example.usermgmt.logservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "log-service")
public class LogServiceProperties {

    private String tableName = "logs.access_log_all";
    private boolean mockEnabled = false;
    private int maxQueryRangeDays = 31;
    private int defaultPageSize = 20;
    private int maxPageSize = 200;
    private long defaultTimeoutThresholdMs = 3000;
    private ClickHouse clickhouse = new ClickHouse();
    private Alert alert = new Alert();

    @Data
    public static class ClickHouse {
        private boolean enabled = true;
        private String url = "jdbc:clickhouse://127.0.0.1:8123/default?compress=0";
        private String username = "default";
        private String password = "";
        private String driverClassName = "com.clickhouse.jdbc.ClickHouseDriver";
    }

    @Data
    public static class Alert {
        private boolean enabled = true;
        private String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/log_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&createDatabaseIfNotExist=true";
        private String username = "root";
        private String password = "123456";
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
        private long evaluationDelayMs = 60000;
        private String notificationServiceUrl = "http://127.0.0.1:8082";
        private long notificationCooldownMinutes = 5;
        private String defaultNotificationChannel = "in_app";
    }
}
