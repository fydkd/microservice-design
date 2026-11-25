package com.example.usermgmt.topbiz.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.client.NotificationServiceClient;
import com.example.usermgmt.topbiz.client.UserServiceClient;
import com.example.usermgmt.topbiz.context.SessionContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final NotificationServiceClient notificationServiceClient;
    private final UserServiceClient userServiceClient;

    @GetMapping
    public ApiResponse<Object> listMessages(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "readStatus", required = false) Boolean readStatus
    ) {
        SessionUser current = requireCurrentUser();
        return notificationServiceClient.listSiteMessages(current.getUserId(), page, pageSize, readStatus);
    }

    @PostMapping("/read")
    public ApiResponse<Object> markRead(@RequestBody Map<String, Object> request) {
        SessionUser current = requireCurrentUser();
        return notificationServiceClient.markSiteMessagesRead(current.getUserId(), request);
    }

    @PostMapping("/send")
    public ApiResponse<Object> send(@RequestBody Map<String, Object> request) {
        SessionUser current = requireCurrentUser();
        String channel = normalizeChannel(asString(request.get("channel")));
        if (hasText(asString(request.get("scheduleAt"))) || request.get("strategy") != null || !"in_app".equals(channel)) {
            return sendReusableNotification(request, channel);
        }
        Map<String, Object> payload = new HashMap<>(request);
        payload.put("creatorId", current.getUserId());
        return notificationServiceClient.sendSiteMessage(payload);
    }

    @GetMapping("/templates")
    public ApiResponse<Object> listMessageTemplates(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "50") Integer size,
        @RequestParam(value = "business_code", required = false) String businessCode,
        @RequestParam(value = "channel", defaultValue = "in_app") String channel
    ) {
        requireCurrentUser();
        return notificationServiceClient.listTemplates(page, size, businessCode, normalizeChannel(channel), "active");
    }

    @PostMapping("/templates/preview")
    public ApiResponse<Object> previewMessageTemplate(@RequestBody Map<String, Object> request) {
        requireCurrentUser();
        return notificationServiceClient.previewTemplate(request);
    }

    private SessionUser requireCurrentUser() {
        SessionUser current = SessionContext.get();
        if (current == null) {
            throw new BusinessException(401, "未登录或会话已失效");
        }
        return current;
    }

    private ApiResponse<Object> sendReusableNotification(Map<String, Object> request, String channel) {
        String businessCode = firstNonBlank(
            asString(request.get("businessCode")),
            asString(request.get("category")),
            "manual_notice"
        );
        String templateId = asString(request.get("templateId"));
        String templateCode = asString(request.get("templateCode"));
        if ("in_app".equals(channel) && !hasText(templateId) && !hasText(templateCode)) {
            templateCode = "IN_APP_PLAIN_MESSAGE";
        }
        if (!hasText(templateId) && !hasText(templateCode)) {
            throw new BusinessException(400, "邮件、短信和推送发送必须选择模板");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        putIfPresent(payload, "requestId", request.get("requestId"));
        payload.put("businessCode", businessCode);
        putIfPresent(payload, "templateId", templateId);
        putIfPresent(payload, "templateCode", templateCode);
        payload.put("channel", channel);
        putIfPresent(payload, "scheduleAt", request.get("scheduleAt"));
        putIfPresent(payload, "strategy", request.get("strategy"));
        payload.put("params", buildReusableParams(request));
        payload.put("recipients", buildRecipients(request, channel));
        return notificationServiceClient.sendReusableMessageBatch(payload);
    }

    private Map<String, Object> buildReusableParams(Map<String, Object> request) {
        Map<String, Object> params = new LinkedHashMap<>();
        Object rawParams = request.get("params");
        if (rawParams instanceof Map<?, ?> rawMap) {
            rawMap.forEach((key, value) -> {
                if (key != null) {
                    params.put(String.valueOf(key), value);
                }
            });
        }
        if (!params.containsKey("title") && hasText(asString(request.get("title")))) {
            params.put("title", asString(request.get("title")));
        }
        if (!params.containsKey("body") && hasText(asString(request.get("content")))) {
            params.put("body", asString(request.get("content")));
        }
        return params;
    }

    private List<Map<String, Object>> buildRecipients(Map<String, Object> request, String channel) {
        List<String> userIds = normalizeUserIds(request.get("userIds"));
        if (!userIds.isEmpty()) {
            return buildRecipientsFromUserIds(userIds, channel);
        }

        Object rawRecipients = request.get("recipients");
        List<Map<String, Object>> recipients = new ArrayList<>();
        if (rawRecipients instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    recipients.add(resolveRecipient(map, channel));
                }
            }
        } else if (request.get("recipient") instanceof Map<?, ?> map) {
            recipients.add(resolveRecipient(map, channel));
        }
        recipients.removeIf(item -> !hasText(asString(item.get("type"))) || !hasText(asString(item.get("value"))));
        if (recipients.isEmpty()) {
            throw new BusinessException(400, "请选择有效接收人");
        }
        return recipients;
    }

    private List<Map<String, Object>> buildRecipientsFromUserIds(List<String> userIds, String channel) {
        List<Map<String, Object>> recipients = new ArrayList<>();
        for (String userId : userIds) {
            recipients.add(resolveUserRecipient(userId, channel));
        }
        return recipients;
    }

    private Map<String, Object> resolveRecipient(Map<?, ?> raw, String channel) {
        String type = normalizeRecipientType(asString(raw.get("type")));
        String value = asString(raw.get("value"));
        if (!hasText(type) || !hasText(value)) {
            return Map.of();
        }
        if ("user_id".equals(type)) {
            return resolveUserRecipient(value.trim(), channel);
        }
        String expectedType = expectedRecipientType(channel);
        if (hasText(expectedType) && !expectedType.equals(type)) {
            throw new BusinessException(400, channel + " 发送不支持接收类型: " + type);
        }
        if (isMaskedValue(value)) {
            throw new BusinessException(400, "接收人联系方式是脱敏值，请按用户重新选择接收人");
        }
        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("type", type);
        recipient.put("value", value.trim());
        return recipient;
    }

    private Map<String, Object> resolveUserRecipient(String userId, String channel) {
        if (!hasText(userId)) {
            return Map.of();
        }
        String normalizedChannel = normalizeChannel(channel);
        if ("email".equals(normalizedChannel)) {
            return buildRecipient("email", resolveUserContact(userId, "email", "邮箱"));
        }
        if ("sms".equals(normalizedChannel)) {
            return buildRecipient("phone", resolveUserContact(userId, "phone", "手机号"));
        }
        return buildRecipient("user_id", userId.trim());
    }

    private String resolveUserContact(String userId, String field, String label) {
        ApiResponse<Object> response = userServiceClient.getUserDetail(userId.trim());
        if (response == null || response.getCode() == null || response.getCode() != 200) {
            throw new BusinessException(400, "获取接收人信息失败: " + userId);
        }
        if (!(response.getData() instanceof Map<?, ?> user)) {
            throw new BusinessException(400, "接收人信息格式不正确: " + userId);
        }
        String value = asString(user.get(field));
        if (!hasText(value)) {
            throw new BusinessException(400, "用户 " + userId + " 没有配置" + label);
        }
        if (isMaskedValue(value)) {
            throw new BusinessException(400, "用户 " + userId + " 的" + label + "不是完整值");
        }
        return value.trim();
    }

    private Map<String, Object> buildRecipient(String type, String value) {
        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("type", type);
        recipient.put("value", value);
        return recipient;
    }

    private List<String> normalizeUserIds(Object rawUserIds) {
        Set<String> ids = new LinkedHashSet<>();
        if (rawUserIds instanceof List<?> list) {
            for (Object item : list) {
                String userId = asString(item);
                if (hasText(userId)) {
                    ids.add(userId.trim());
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private String normalizeRecipientType(String type) {
        if (!hasText(type)) {
            return null;
        }
        String normalized = type.trim().toLowerCase();
        return switch (normalized) {
            case "user", "uid", "user-id", "userid", "user_id" -> "user_id";
            case "mail", "email" -> "email";
            case "mobile", "phone", "sms" -> "phone";
            default -> normalized;
        };
    }

    private String expectedRecipientType(String channel) {
        return switch (normalizeChannel(channel)) {
            case "email" -> "email";
            case "sms" -> "phone";
            default -> null;
        };
    }

    private boolean isMaskedValue(String value) {
        return hasText(value) && value.contains("*");
    }

    private String normalizeChannel(String channel) {
        if (!hasText(channel)) {
            return "in_app";
        }
        String normalized = channel.trim().toLowerCase();
        return switch (normalized) {
            case "site_message", "site-message", "in_app", "in-app", "inapp" -> "in_app";
            case "email" -> "email";
            case "sms" -> "sms";
            case "push" -> "push";
            case "feishu", "lark" -> "feishu";
            default -> throw new BusinessException(400, "不支持的发送方式: " + channel);
        };
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (hasText(first)) {
            return first.trim();
        }
        if (hasText(second)) {
            return second.trim();
        }
        return fallback;
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value instanceof String text) {
            if (hasText(text)) {
                target.put(key, text.trim());
            }
            return;
        }
        if (value != null) {
            target.put(key, value);
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
