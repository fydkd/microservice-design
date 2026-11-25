package com.example.usermgmt.topbiz.flow.plugin;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.client.NotificationServiceClient;
import com.example.usermgmt.topbiz.controller.AuthController;
import com.example.usermgmt.topbiz.flow.FlowAction;
import com.example.usermgmt.topbiz.flow.FlowContext;
import com.example.usermgmt.topbiz.flow.FlowContextKeys;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerifyRegisterCaptchaAction implements FlowAction {

    private final NotificationServiceClient notificationServiceClient;

    @Override
    public String code() {
        return "verify_register_captcha";
    }

    @Override
    public void execute(FlowContext context) {
        AuthController.RegisterRequest request = context.get(FlowContextKeys.REGISTER_REQUEST, AuthController.RegisterRequest.class);
        Map<String, Object> payload = new HashMap<>();
        payload.put("captchaType", "email".equalsIgnoreCase(AuthController.detectAccountType(request.getAccount())) ? "email" : "sms");
        payload.put("account", request.getAccount());
        payload.put("scene", "register");
        payload.put("code", request.getCode());
        ApiResponse<Object> response = notificationServiceClient.verifyCaptcha(payload);
        if (response == null || response.getCode() == null || response.getCode() != 200) {
            throw new BusinessException(40004, "验证码错误或已过期");
        }
    }
}
