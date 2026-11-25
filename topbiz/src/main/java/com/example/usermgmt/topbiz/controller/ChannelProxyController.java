package com.example.usermgmt.topbiz.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.client.NotificationServiceClient;
import com.example.usermgmt.topbiz.context.SessionContext;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/channels")
public class ChannelProxyController {

    private final NotificationServiceClient notificationServiceClient;

    @PostMapping("/{channel}")
    public ApiResponse<Object> configure(@PathVariable("channel") String channel, @RequestBody Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.configureChannel(channel, request);
    }

    @GetMapping("/status")
    public ApiResponse<Object> status() {
        requireAdmin();
        return notificationServiceClient.channelStatus();
    }

    @PutMapping("/{channel}/switch")
    public ApiResponse<Object> switchProvider(@PathVariable("channel") String channel, @RequestBody Map<String, Object> request) {
        requireAdmin();
        return notificationServiceClient.switchChannel(channel, request);
    }

    private void requireAdmin() {
        SessionUser current = SessionContext.get();
        if (current == null) {
            throw new BusinessException(401, "未登录或会话已失效");
        }
        if (!"admin".equalsIgnoreCase(current.getRole())) {
            throw new BusinessException(40301, "无权限访问渠道管理");
        }
    }
}