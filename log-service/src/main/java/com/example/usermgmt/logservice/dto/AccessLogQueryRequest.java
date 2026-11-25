package com.example.usermgmt.logservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccessLogQueryRequest {

    @NotBlank(message = "start_time is required")
    @JsonAlias("start_time")
    private String startTime;

    @NotBlank(message = "end_time is required")
    @JsonAlias("end_time")
    private String endTime;

    @JsonAlias("service")
    private String microservice;

    @JsonAlias({"api", "api_endpoint"})
    private String apiEndpoint;

    @JsonAlias("http_method")
    private String httpMethod;

    @JsonAlias("status_code")
    private Integer statusCode;

    @JsonAlias("client_ip")
    private String clientIp;

    @JsonAlias("trace_id")
    private String traceId;

    @JsonAlias("user_id")
    private String userId;

    private Integer page;

    @JsonAlias({"page_size", "pageSize", "size"})
    private Integer pageSize;
}
