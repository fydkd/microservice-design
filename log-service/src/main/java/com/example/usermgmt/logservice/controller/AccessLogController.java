package com.example.usermgmt.logservice.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.api.PageResponse;
import com.example.usermgmt.logservice.dto.AccessLogQueryRequest;
import com.example.usermgmt.logservice.dto.AccessLogRecord;
import com.example.usermgmt.logservice.service.LogAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs")
public class AccessLogController {

    private final LogAnalysisService logAnalysisService;

    @PostMapping("/query")
    public ApiResponse<PageResponse<AccessLogRecord>> query(@Valid @RequestBody AccessLogQueryRequest request) {
        return ApiResponse.success(logAnalysisService.queryAccessLogs(request));
    }
}
