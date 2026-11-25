package com.example.usermgmt.logservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleResponse {

    private String id;

    @JsonProperty("rule_name")
    private String ruleName;

    @JsonProperty("metric_type")
    private String metricType;

    private String service;

    @JsonProperty("api_endpoint")
    private String apiEndpoint;

    @JsonProperty("downstream_service")
    private String downstreamService;

    @JsonProperty("error_type")
    private String errorType;

    private String percentile;

    @JsonProperty("client_ip")
    private String clientIp;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("timeout_threshold_ms")
    private Long timeoutThresholdMs;

    @JsonProperty("window_size")
    private String windowSize;

    private String comparator;

    private Double threshold;

    @JsonProperty("consecutive_times")
    private Integer consecutiveTimes;

    @JsonProperty("notification_channel")
    private String notificationChannel;

    @JsonProperty("notification_target")
    private String notificationTarget;

    private Boolean enabled;

    @JsonProperty("created_time")
    private LocalDateTime createdTime;

    @JsonProperty("updated_time")
    private LocalDateTime updatedTime;

    @JsonProperty("last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    @JsonProperty("last_value")
    private Double lastValue;

    @JsonProperty("current_consecutive_count")
    private Integer currentConsecutiveCount;

    @JsonProperty("trigger_count")
    private Integer triggerCount;

    @JsonProperty("last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    @JsonProperty("last_notification_at")
    private LocalDateTime lastNotificationAt;

    @JsonProperty("last_notification_status")
    private String lastNotificationStatus;

    @JsonProperty("last_notification_message")
    private String lastNotificationMessage;
}
