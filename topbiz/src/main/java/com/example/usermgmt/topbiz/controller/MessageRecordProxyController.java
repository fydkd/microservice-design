package com.example.usermgmt.topbiz.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.client.NotificationServiceClient;
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
@RequestMapping("/api/v1/admin/messages")
public class MessageRecordProxyController {

    private final NotificationServiceClient notificationServiceClient;

    @GetMapping
    public ApiResponse<Object> listRecords(
        @RequestParam(value = "recipient_type", required = false) String recipientType,
        @RequestParam(value = "recipient", required = false) String recipient,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "start_date", required = false) String startDate,
        @RequestParam(value = "end_date", required = false) String endDate,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        requireAdmin();
        return notificationServiceClient.listReusableMessages(recipientType, recipient, businessCode, status, startDate, endDate, page, size);
    }

    @GetMapping("/{messageId}")
    public ApiResponse<Object> detail(@PathVariable("messageId") String messageId) {
        requireAdmin();
        return notificationServiceClient.getReusableMessage(messageId);
    }

    @DeleteMapping("/{messageId}")
    public ApiResponse<Object> cancel(@PathVariable("messageId") String messageId) {
        requireAdmin();
        return notificationServiceClient.cancelReusableMessage(messageId);
    }

    @GetMapping("/policies")
    public ApiResponse<Object> listPolicies(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "20") Integer size,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "status", required = false) String status
    ) {
        requireAdmin();
        return notificationServiceClient.listPolicies(page, size, businessCode, null, status);
    }

    @PostMapping("/policies")
    public ApiResponse<Object> createPolicy(@RequestBody Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.createPolicy(request);
    }

    @PutMapping("/policies/{policyId}")
    public ApiResponse<Object> updatePolicy(@PathVariable("policyId") String policyId, @RequestBody Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.updatePolicy(policyId, request);
    }

    private void requireAdmin() {
        SessionUser current = SessionContext.get();
        if (current == null) {
            throw new BusinessException(401, "未登录或会话已失效");
        }
        if (!"admin".equalsIgnoreCase(current.getRole())) {
            throw new BusinessException(40301, "无权限访问消息管理");
        }
    }
}