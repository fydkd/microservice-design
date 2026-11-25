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
@RequestMapping("/api/v1/admin/templates")
public class TemplateProxyController {

    private final NotificationServiceClient notificationServiceClient;

    @PostMapping
    public ApiResponse<Object> create(@RequestBody Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.createTemplate(request);
    }

    @GetMapping("/{templateId}")
    public ApiResponse<Object> detail(@PathVariable("templateId") String templateId) {
        requireAdmin();
        return notificationServiceClient.getTemplate(templateId);
    }

    @PutMapping("/{templateId}")
    public ApiResponse<Object> update(@PathVariable("templateId") String templateId, @RequestBody Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.updateTemplate(templateId, request);
    }

    @DeleteMapping("/{templateId}")
    public ApiResponse<Object> delete(@PathVariable("templateId") String templateId) {
        requireAdmin();
        return notificationServiceClient.deleteTemplate(templateId);
    }

    @PostMapping("/{templateId}/publish")
    public ApiResponse<Object> publish(@PathVariable("templateId") String templateId, @RequestBody(required = false) Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.publishTemplate(templateId, request);
    }

    @GetMapping
    public ApiResponse<Object> list(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "20") Integer size,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "channel", required = false) String channel,
        @RequestParam(value = "status", required = false) String status
    ) {
        requireAdmin();
        return notificationServiceClient.listTemplates(page, size, businessCode, channel, status);
    }

    @PostMapping("/preview")
    public ApiResponse<Object> preview(@RequestBody Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.previewTemplate(request);
    }

    @GetMapping("/{templateId}/versions")
    public ApiResponse<Object> versions(@PathVariable("templateId") String templateId) {
        requireAdmin();
        return notificationServiceClient.listTemplateVersions(templateId);
    }

    @GetMapping("/{templateId}/versions/{version}")
    public ApiResponse<Object> versionDetail(@PathVariable("templateId") String templateId, @PathVariable("version") Integer version) {
        requireAdmin();
        return notificationServiceClient.getTemplateVersion(templateId, version);
    }

    @PostMapping("/{templateId}/rollback")
    public ApiResponse<Object> rollback(@PathVariable("templateId") String templateId, @RequestBody Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.rollbackTemplate(templateId, request);
    }

    private void requireAdmin() {
        SessionUser current = SessionContext.get();
        if (current == null) {
            throw new BusinessException(401, "未登录或会话已失效");
        }
        if (!"admin".equalsIgnoreCase(current.getRole())) {
            throw new BusinessException(40301, "无权限访问模板管理");
        }
    }
}