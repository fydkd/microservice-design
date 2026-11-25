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
@RequestMapping("/api/v1/admin/notifications")
public class NotificationAdminController {

    private static final String NOTIFICATION_MANAGEMENT_MENU = "menu:notificationManagement";

    private final NotificationServiceClient notificationServiceClient;

    @GetMapping("/channels/status")
    public ApiResponse<Object> channelStatus() {
        requireNotificationPermission();
        return notificationServiceClient.channelStatus();
    }

    @PostMapping("/channels/{channel}")
    public ApiResponse<Object> configureChannel(@PathVariable("channel") String channel, @RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.configureChannel(channel, request);
    }

    @PutMapping("/channels/{channel}/switch")
    public ApiResponse<Object> switchChannel(@PathVariable("channel") String channel, @RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.switchChannel(channel, request);
    }

    @PostMapping("/templates")
    public ApiResponse<Object> createTemplate(@RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.createTemplate(request);
    }

    @GetMapping("/templates")
    public ApiResponse<Object> listTemplates(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "20") Integer size,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "channel", required = false) String channel,
        @RequestParam(value = "status", required = false) String status
    ) {
        requireNotificationPermission();
        return notificationServiceClient.listTemplates(page, size, businessCode, channel, status);
    }

    @GetMapping("/templates/{templateId}")
    public ApiResponse<Object> getTemplate(@PathVariable("templateId") String templateId) {
        requireNotificationPermission();
        return notificationServiceClient.getTemplate(templateId);
    }

    @PutMapping("/templates/{templateId}")
    public ApiResponse<Object> updateTemplate(@PathVariable("templateId") String templateId, @RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.updateTemplate(templateId, request);
    }

    @DeleteMapping("/templates/{templateId}")
    public ApiResponse<Object> deleteTemplate(@PathVariable("templateId") String templateId) {
        requireNotificationPermission();
        return notificationServiceClient.deleteTemplate(templateId);
    }

    @PostMapping("/templates/{templateId}/publish")
    public ApiResponse<Object> publishTemplate(@PathVariable("templateId") String templateId, @RequestBody(required = false) Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.publishTemplate(templateId, request == null ? Map.of() : request);
    }

    @PostMapping("/templates/preview")
    public ApiResponse<Object> previewTemplate(@RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.previewTemplate(request);
    }

    @GetMapping("/templates/{templateId}/versions")
    public ApiResponse<Object> listTemplateVersions(@PathVariable("templateId") String templateId) {
        requireNotificationPermission();
        return notificationServiceClient.listTemplateVersions(templateId);
    }

    @GetMapping("/templates/{templateId}/versions/{version}")
    public ApiResponse<Object> getTemplateVersion(@PathVariable("templateId") String templateId, @PathVariable("version") Integer version) {
        requireNotificationPermission();
        return notificationServiceClient.getTemplateVersion(templateId, version);
    }

    @PostMapping("/messages/send")
    public ApiResponse<Object> sendReusableMessage(@RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.sendReusableMessage(request);
    }

    @PostMapping("/messages/batch")
    public ApiResponse<Object> sendReusableMessageBatch(@RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.sendReusableMessageBatch(request);
    }

    @GetMapping("/messages")
    public ApiResponse<Object> listReusableMessages(
        @RequestParam(value = "recipient_type", required = false) String recipientType,
        @RequestParam(value = "recipient", required = false) String recipient,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "start_date", required = false) String startDate,
        @RequestParam(value = "end_date", required = false) String endDate,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        requireNotificationPermission();
        return notificationServiceClient.listReusableMessages(recipientType, recipient, businessCode, status, startDate, endDate, page, size);
    }

    @GetMapping("/messages/{messageId}")
    public ApiResponse<Object> getReusableMessage(@PathVariable("messageId") String messageId) {
        requireNotificationPermission();
        return notificationServiceClient.getReusableMessage(messageId);
    }

    @GetMapping("/messages/batch/{batchId}")
    public ApiResponse<Object> getReusableMessageBatch(@PathVariable("batchId") String batchId) {
        requireNotificationPermission();
        return notificationServiceClient.getReusableMessageBatch(batchId);
    }

    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<Object> cancelReusableMessage(@PathVariable("messageId") String messageId) {
        requireNotificationPermission();
        return notificationServiceClient.cancelReusableMessage(messageId);
    }

    @PostMapping("/policies")
    public ApiResponse<Object> createPolicy(@RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.createPolicy(request);
    }

    @GetMapping("/policies")
    public ApiResponse<Object> listPolicies(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "20") Integer size,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "channel_type", required = false) String channelType,
        @RequestParam(value = "status", required = false) String status
    ) {
        requireNotificationPermission();
        return notificationServiceClient.listPolicies(page, size, businessCode, channelType, status);
    }

    @GetMapping("/policies/{policyId}")
    public ApiResponse<Object> getPolicy(@PathVariable("policyId") String policyId) {
        requireNotificationPermission();
        return notificationServiceClient.getPolicy(policyId);
    }

    @PutMapping("/policies/{policyId}")
    public ApiResponse<Object> updatePolicy(@PathVariable("policyId") String policyId, @RequestBody Map<String, Object> request) {
        requireNotificationPermission();
        return notificationServiceClient.updatePolicy(policyId, request);
    }

    @DeleteMapping("/policies/{policyId}")
    public ApiResponse<Object> deletePolicy(@PathVariable("policyId") String policyId) {
        requireNotificationPermission();
        return notificationServiceClient.deletePolicy(policyId);
    }

    private void requireNotificationPermission() {
        SessionUser current = SessionContext.get();
        if (current == null) {
            throw new BusinessException(401, "not logged in or session expired");
        }
        if (current.getMenus() == null || !current.getMenus().contains(NOTIFICATION_MANAGEMENT_MENU)) {
            throw new BusinessException(40301, "missing permission: " + NOTIFICATION_MANAGEMENT_MENU);
        }
    }
}
