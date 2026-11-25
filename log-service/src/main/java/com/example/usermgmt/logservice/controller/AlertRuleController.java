package com.example.usermgmt.logservice.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.api.PageResponse;
import com.example.usermgmt.logservice.dto.AlertRuleRequest;
import com.example.usermgmt.logservice.dto.AlertRuleResponse;
import com.example.usermgmt.logservice.dto.AlertTriggerRecord;
import com.example.usermgmt.logservice.service.AlertRuleService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/alerts")
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    @PostMapping("/rules")
    public ApiResponse<AlertRuleResponse> create(@Valid @RequestBody AlertRuleRequest request) {
        return ApiResponse.success("Alert rule created", alertRuleService.createAlertRule(request));
    }

    @PutMapping("/rules/{ruleId}")
    public ApiResponse<AlertRuleResponse> update(@PathVariable("ruleId") String ruleId, @Valid @RequestBody AlertRuleRequest request) {
        return ApiResponse.success("Alert rule updated", alertRuleService.updateAlertRule(ruleId, request));
    }

    @GetMapping("/rules")
    public ApiResponse<PageResponse<AlertRuleResponse>> list(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "metricType", required = false) String metricType,
        @RequestParam(value = "service", required = false) String service,
        @RequestParam(value = "enabled", required = false) Boolean enabled
    ) {
        return ApiResponse.success(alertRuleService.listAlertRules(page, pageSize, metricType, service, enabled));
    }

    @GetMapping("/rules/{ruleId}")
    public ApiResponse<AlertRuleResponse> detail(@PathVariable("ruleId") String ruleId) {
        return ApiResponse.success(alertRuleService.getAlertRule(ruleId));
    }

    @DeleteMapping("/rules/{ruleId}")
    public ApiResponse<Void> delete(@PathVariable("ruleId") String ruleId) {
        alertRuleService.deleteAlertRule(ruleId);
        return ApiResponse.success("Alert rule deleted", null);
    }

    @GetMapping("/triggers")
    public ApiResponse<PageResponse<AlertTriggerRecord>> listTriggers(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "ruleId", required = false) String ruleId,
        @RequestParam(value = "metricType", required = false) String metricType
    ) {
        return ApiResponse.success(alertRuleService.listTriggerRecords(page, pageSize, ruleId, metricType));
    }
}
