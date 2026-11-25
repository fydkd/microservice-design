package com.example.usermgmt.logservice.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.logservice.dto.MetricQueryRequest;
import com.example.usermgmt.logservice.dto.MetricSeriesResponse;
import com.example.usermgmt.logservice.service.LogAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private final LogAnalysisService logAnalysisService;

    @PostMapping("/request_rate")
    public ApiResponse<MetricSeriesResponse> requestRate(@Valid @RequestBody MetricQueryRequest request) {
        return ApiResponse.success(logAnalysisService.requestRate(request));
    }

    @PostMapping("/request_timeout_rate")
    public ApiResponse<MetricSeriesResponse> timeoutRate(@Valid @RequestBody MetricQueryRequest request) {
        return ApiResponse.success(logAnalysisService.timeoutRate(request));
    }

    @PostMapping("/request_error_rate")
    public ApiResponse<MetricSeriesResponse> errorRate(@Valid @RequestBody MetricQueryRequest request) {
        return ApiResponse.success(logAnalysisService.errorRate(request));
    }

    @PostMapping("/request_latency")
    public ApiResponse<MetricSeriesResponse> latency(@Valid @RequestBody MetricQueryRequest request) {
        return ApiResponse.success(logAnalysisService.latency(request));
    }

    @PostMapping("/downstream_error_rate")
    public ApiResponse<MetricSeriesResponse> downstreamErrorRate(@Valid @RequestBody MetricQueryRequest request) {
        return ApiResponse.success(logAnalysisService.downstreamErrorRate(request));
    }

    @PostMapping("/downstream_latency")
    public ApiResponse<MetricSeriesResponse> downstreamLatency(@Valid @RequestBody MetricQueryRequest request) {
        return ApiResponse.success(logAnalysisService.downstreamLatency(request));
    }

    @PostMapping("/ip_request_rate")
    public ApiResponse<MetricSeriesResponse> ipRequestRate(@Valid @RequestBody MetricQueryRequest request) {
        return ApiResponse.success(logAnalysisService.ipRequestRate(request));
    }

    @PostMapping("/user_request_rate")
    public ApiResponse<MetricSeriesResponse> userRequestRate(@Valid @RequestBody MetricQueryRequest request) {
        return ApiResponse.success(logAnalysisService.userRequestRate(request));
    }
}
