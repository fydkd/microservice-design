package com.example.usermgmt.topbiz.client;

import com.example.usermgmt.common.api.ApiResponse;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service-client", url = "${topbiz.clients.notification-service-url}")
public interface NotificationServiceClient {

    @PostMapping("/internal/captcha/send")
    ApiResponse<Object> sendCaptcha(@RequestBody Map<String, Object> request);

    @PostMapping("/internal/captcha/verify")
    ApiResponse<Object> verifyCaptcha(@RequestBody Map<String, Object> request);

    @PostMapping("/internal/site-messages/send")
    ApiResponse<Object> sendSiteMessage(@RequestBody Map<String, Object> request);

    @GetMapping("/internal/site-messages/user/{userId}")
    ApiResponse<Object> listSiteMessages(
        @PathVariable("userId") String userId,
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "readStatus", required = false) Boolean readStatus
    );

    @PostMapping("/internal/site-messages/user/{userId}/read")
    ApiResponse<Object> markSiteMessagesRead(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/channels/status")
    ApiResponse<Object> channelStatus();

    @PostMapping("/api/v1/channels/{channel}")
    ApiResponse<Object> configureChannel(@PathVariable("channel") String channel, @RequestBody Map<String, Object> request);

    @PutMapping("/api/v1/channels/{channel}/switch")
    ApiResponse<Object> switchChannel(@PathVariable("channel") String channel, @RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/templates")
    ApiResponse<Object> createTemplate(@RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/templates")
    ApiResponse<Object> listTemplates(
        @RequestParam("page") Integer page,
        @RequestParam("size") Integer size,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "channel", required = false) String channel,
        @RequestParam(value = "status", required = false) String status
    );

    @GetMapping("/api/v1/templates/{templateId}")
    ApiResponse<Object> getTemplate(@PathVariable("templateId") String templateId);

    @PutMapping("/api/v1/templates/{templateId}")
    ApiResponse<Object> updateTemplate(@PathVariable("templateId") String templateId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/api/v1/templates/{templateId}")
    ApiResponse<Object> deleteTemplate(@PathVariable("templateId") String templateId);

    @PostMapping("/api/v1/templates/{templateId}/publish")
    ApiResponse<Object> publishTemplate(@PathVariable("templateId") String templateId, @RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/templates/preview")
    ApiResponse<Object> previewTemplate(@RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/templates/{templateId}/versions")
    ApiResponse<Object> listTemplateVersions(@PathVariable("templateId") String templateId);

    @GetMapping("/api/v1/templates/{templateId}/versions/{version}")
    ApiResponse<Object> getTemplateVersion(@PathVariable("templateId") String templateId, @PathVariable("version") Integer version);

    @PostMapping("/api/v1/templates/{templateId}/rollback")
    ApiResponse<Object> rollbackTemplate(@PathVariable("templateId") String templateId, @RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/messages/send")
    ApiResponse<Object> sendReusableMessage(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/messages/batch")
    ApiResponse<Object> sendReusableMessageBatch(@RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/messages")
    ApiResponse<Object> listReusableMessages(
        @RequestParam(value = "recipient_type", required = false) String recipientType,
        @RequestParam(value = "recipient", required = false) String recipient,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "start_date", required = false) String startDate,
        @RequestParam(value = "end_date", required = false) String endDate,
        @RequestParam("page") Integer page,
        @RequestParam("size") Integer size
    );

    @GetMapping("/api/v1/messages/{messageId}")
    ApiResponse<Object> getReusableMessage(@PathVariable("messageId") String messageId);

    @DeleteMapping("/api/v1/messages/{messageId}")
    ApiResponse<Object> cancelReusableMessage(@PathVariable("messageId") String messageId);

    @GetMapping("/api/v1/messages/batch/{batchId}")
    ApiResponse<Object> getReusableMessageBatch(@PathVariable("batchId") String batchId);

    @PostMapping("/api/v1/policies")
    ApiResponse<Object> createPolicy(@RequestBody Map<String, Object> request);

    @GetMapping("/api/v1/policies")
    ApiResponse<Object> listPolicies(
        @RequestParam("page") Integer page,
        @RequestParam("size") Integer size,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "channel_type", required = false) String channelType,
        @RequestParam(value = "status", required = false) String status
    );

    @GetMapping("/api/v1/policies/{policyId}")
    ApiResponse<Object> getPolicy(@PathVariable("policyId") String policyId);

    @PutMapping("/api/v1/policies/{policyId}")
    ApiResponse<Object> updatePolicy(@PathVariable("policyId") String policyId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/api/v1/policies/{policyId}")
    ApiResponse<Object> deletePolicy(@PathVariable("policyId") String policyId);
}
