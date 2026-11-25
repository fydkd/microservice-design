package com.example.usermgmt.topbiz.client;

import com.example.usermgmt.common.api.ApiResponse;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "log-service-client", url = "${topbiz.clients.log-service-url}")
public interface LogServiceClient {

    @PostMapping("/api/v1/logs/query")
    ApiResponse<Object> queryAccessLogs(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/metrics/request_rate")
    ApiResponse<Object> requestRate(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/metrics/request_timeout_rate")
    ApiResponse<Object> timeoutRate(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/metrics/request_error_rate")
    ApiResponse<Object> errorRate(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/metrics/request_latency")
    ApiResponse<Object> latency(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/metrics/downstream_error_rate")
    ApiResponse<Object> downstreamErrorRate(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/metrics/downstream_latency")
    ApiResponse<Object> downstreamLatency(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/metrics/ip_request_rate")
    ApiResponse<Object> ipRequestRate(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/metrics/user_request_rate")
    ApiResponse<Object> userRequestRate(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/alerts/rules")
    ApiResponse<Object> createAlertRule(@RequestBody Map<String, Object> request);

    @PutMapping("/api/v1/alerts/rules/{ruleId}")
    ApiResponse<Object> updateAlertRule(@PathVariable("ruleId") String ruleId, @RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/alerts/rules")
    ApiResponse<Object> listAlertRules(
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "metricType", required = false) String metricType,
        @RequestParam(value = "service", required = false) String service,
        @RequestParam(value = "enabled", required = false) Boolean enabled
    );

    @GetMapping("/api/v1/alerts/rules/{ruleId}")
    ApiResponse<Object> getAlertRule(@PathVariable("ruleId") String ruleId);

    @DeleteMapping("/api/v1/alerts/rules/{ruleId}")
    ApiResponse<Object> deleteAlertRule(@PathVariable("ruleId") String ruleId);

    @GetMapping("/api/v1/alerts/triggers")
    ApiResponse<Object> listAlertTriggers(
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "ruleId", required = false) String ruleId,
        @RequestParam(value = "metricType", required = false) String metricType
    );
}
