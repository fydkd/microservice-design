package com.example.usermgmt.logservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertRuleRequest {

    @NotBlank(message = "rule_name is required")
    @JsonAlias("rule_name")
    private String ruleName;

    @NotBlank(message = "metric_type is required")
    @JsonAlias("metric_type")
    private String metricType;

    private String service;

    @JsonAlias({"api", "api_endpoint"})
    private String apiEndpoint;

    @JsonAlias("downstream_service")
    private String downstreamService;

    @JsonAlias("error_type")
    private String errorType;

    private String percentile;

    @JsonAlias("client_ip")
    private String clientIp;

    @JsonAlias("user_id")
    private String userId;

    @JsonAlias("timeout_threshold_ms")
    private Long timeoutThresholdMs;

    @NotBlank(message = "window_size is required")
    @JsonAlias("window_size")
    private String windowSize;

    @NotBlank(message = "comparator is required")
    private String comparator;

    @NotNull(message = "threshold is required")
    private Double threshold;

    @JsonAlias("consecutive_times")
    private Integer consecutiveTimes;

    @JsonAlias("notification_channel")
    private String notificationChannel;

    @JsonAlias("notification_target")
    private String notificationTarget;

    private Boolean enabled;
}
