package com.example.usermgmt.logservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLogRecord {

    private String timestamp;

    private String microservice;

    @JsonProperty("api_endpoint")
    private String apiEndpoint;

    @JsonProperty("http_method")
    private String httpMethod;

    @JsonProperty("status_code")
    private Integer statusCode;

    @JsonProperty("latency_ms")
    private Long latencyMs;

    @JsonProperty("trace_id")
    private String traceId;

    @JsonProperty("client_ip")
    private String clientIp;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("user_id")
    private String userId;
}
