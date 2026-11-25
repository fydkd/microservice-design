package com.example.usermgmt.topbiz.flow.plugin;

import com.example.usermgmt.topbiz.client.UserServiceClient;
import com.example.usermgmt.topbiz.flow.FlowAction;
import com.example.usermgmt.topbiz.flow.FlowContext;
import com.example.usermgmt.topbiz.flow.FlowContextKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelCurrentUserAction implements FlowAction {

    private final UserServiceClient userServiceClient;

    @Override
    public String code() {
        return "cancel_current_user";
    }

    @Override
    public void execute(FlowContext context) {
        userServiceClient.cancelCurrentUser();
    }
}
