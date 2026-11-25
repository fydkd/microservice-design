package com.example.usermgmt.topbiz.flow.plugin;

import com.example.usermgmt.common.auth.AuthRegisterRequest;
import com.example.usermgmt.common.auth.AuthenticatedUser;
import com.example.usermgmt.topbiz.controller.AuthController;
import com.example.usermgmt.topbiz.flow.FlowAction;
import com.example.usermgmt.topbiz.flow.FlowContext;
import com.example.usermgmt.topbiz.flow.FlowContextKeys;
import com.example.usermgmt.topbiz.service.DeviceLoginContext;
import com.example.usermgmt.topbiz.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUserAction implements FlowAction {

    private final SessionService sessionService;

    @Override
    public String code() {
        return "register_user";
    }

    @Override
    public void execute(FlowContext context) {
        AuthController.RegisterRequest request = context.get(FlowContextKeys.REGISTER_REQUEST, AuthController.RegisterRequest.class);
        AuthRegisterRequest registerRequest = new AuthRegisterRequest();
        registerRequest.setAccount(request.getAccount());
        registerRequest.setPassword(request.getPassword());
        registerRequest.setCode(request.getCode());
        registerRequest.setNickname(request.getNickname());
        DeviceLoginContext deviceContext = context.get(FlowContextKeys.DEVICE_CONTEXT, DeviceLoginContext.class);
        if (deviceContext != null) {
            registerRequest.setLoginIp(deviceContext.getLoginIp());
        }
        AuthenticatedUser user = sessionService.register(registerRequest);
        context.put(FlowContextKeys.AUTH_USER, user);
    }
}
