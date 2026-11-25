package com.example.usermgmt.logservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MetricQueryRequest {

    @NotBlank(message = "start_time is required")
    @JsonAlias("start_time")
    private String startTime;

    @NotBlank(message = "end_time is required")
    @JsonAlias("end_time")
    private String endTime;

    @NotBlank(message = "window_size is required")
    @JsonAlias("window_size")
    private String windowSize;

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

    @JsonAlias("min_qps")
    private Double minQps;

    @JsonAlias("timeout_threshold_ms")
    private Long timeoutThresholdMs;
}
