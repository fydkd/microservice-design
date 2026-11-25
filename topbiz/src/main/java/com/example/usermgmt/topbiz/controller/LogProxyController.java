package com.example.usermgmt.topbiz.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.client.LogServiceClient;
import com.example.usermgmt.topbiz.context.SessionContext;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/logs")
public class LogProxyController {

    private static final String LOG_MANAGEMENT_MENU = "menu:logManagement";

    private final LogServiceClient logServiceClient;

    @PostMapping("/query")
    public ApiResponse<Object> queryAccessLogs(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.queryAccessLogs(request);
    }

    @PostMapping("/metrics/request-rate")
    public ApiResponse<Object> requestRate(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.requestRate(request);
    }

    @PostMapping("/metrics/timeout-rate")
    public ApiResponse<Object> timeoutRate(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.timeoutRate(request);
    }

    @PostMapping("/metrics/error-rate")
    public ApiResponse<Object> errorRate(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.errorRate(request);
    }

    @PostMapping("/metrics/latency")
    public ApiResponse<Object> latency(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.latency(request);
    }

    @PostMapping("/metrics/downstream-error-rate")
    public ApiResponse<Object> downstreamErrorRate(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.downstreamErrorRate(request);
    }

    @PostMapping("/metrics/downstream-latency")
    public ApiResponse<Object> downstreamLatency(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.downstreamLatency(request);
    }

    @PostMapping("/metrics/ip-request-rate")
    public ApiResponse<Object> ipRequestRate(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.ipRequestRate(request);
    }

    @PostMapping("/metrics/user-request-rate")
    public ApiResponse<Object> userRequestRate(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.userRequestRate(request);
    }

    @PostMapping("/alerts/rules")
    public ApiResponse<Object> createAlertRule(@RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.createAlertRule(request);
    }

    @PutMapping("/alerts/rules/{ruleId}")
    public ApiResponse<Object> updateAlertRule(@PathVariable("ruleId") String ruleId, @RequestBody Map<String, Object> request) {
        requireLogPermission();
        return logServiceClient.updateAlertRule(ruleId, request);
    }

    @GetMapping("/alerts/rules")
    public ApiResponse<Object> listAlertRules(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "metricType", required = false) String metricType,
        @RequestParam(value = "service", required = false) String service,
        @RequestParam(value = "enabled", required = false) Boolean enabled
    ) {
        requireLogPermission();
        return logServiceClient.listAlertRules(page, pageSize, metricType, service, enabled);
    }

    @GetMapping("/alerts/rules/{ruleId}")
    public ApiResponse<Object> getAlertRule(@PathVariable("ruleId") String ruleId) {
        requireLogPermission();
        return logServiceClient.getAlertRule(ruleId);
    }

    @DeleteMapping("/alerts/rules/{ruleId}")
    public ApiResponse<Object> deleteAlertRule(@PathVariable("ruleId") String ruleId) {
        requireLogPermission();
        return logServiceClient.deleteAlertRule(ruleId);
    }

    @GetMapping("/alerts/triggers")
    public ApiResponse<Object> listAlertTriggers(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "ruleId", required = false) String ruleId,
        @RequestParam(value = "metricType", required = false) String metricType
    ) {
        requireLogPermission();
        return logServiceClient.listAlertTriggers(page, pageSize, ruleId, metricType);
    }

    private void requireLogPermission() {
        SessionUser current = SessionContext.get();
        if (current == null) {
            throw new BusinessException(401, "not logged in or session expired");
        }
        if (current.getMenus() == null || !current.getMenus().contains(LOG_MANAGEMENT_MENU)) {
            throw new BusinessException(40301, "missing permission: " + LOG_MANAGEMENT_MENU);
        }
    }
}
