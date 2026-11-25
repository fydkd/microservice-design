package com.example.usermgmt.topbiz.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.topbiz.client.NotificationServiceClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/captcha")
public class CaptchaProxyController {

    private final NotificationServiceClient notificationServiceClient;

    @PostMapping("/send")
    public ApiResponse<Object> send(@RequestBody Map<String, Object> request) {
        return notificationServiceClient.sendCaptcha(request);
    }
}
