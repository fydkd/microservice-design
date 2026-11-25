package com.example.usermgmt.topbiz.flow.plugin;

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
public class CreateSessionAction implements FlowAction {

    private final SessionService sessionService;

    @Override
    public String code() {
        return "create_session";
    }

    @Override
    public void execute(FlowContext context) {
        AuthenticatedUser user = context.get(FlowContextKeys.AUTH_USER, AuthenticatedUser.class);
        DeviceLoginContext deviceContext = context.get(FlowContextKeys.DEVICE_CONTEXT, DeviceLoginContext.class);
        String sessionId = sessionService.createSession(user, deviceContext);
        context.put(FlowContextKeys.LOGIN_RESPONSE, AuthController.LoginResponse.from(user, sessionId));
    }
}
