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
public class AlertTriggerRecord {

    private String id;

    @JsonProperty("rule_id")
    private String ruleId;

    @JsonProperty("rule_name")
    private String ruleName;

    @JsonProperty("metric_type")
    private String metricType;

    @JsonProperty("observed_value")
    private Double observedValue;

    private Double threshold;

    private String comparator;

    @JsonProperty("triggered_at")
    private LocalDateTime triggeredAt;

    @JsonProperty("notification_channel")
    private String notificationChannel;

    @JsonProperty("notification_target")
    private String notificationTarget;

    @JsonProperty("notification_status")
    private String notificationStatus;

    @JsonProperty("notification_message")
    private String notificationMessage;

    private String details;
}
