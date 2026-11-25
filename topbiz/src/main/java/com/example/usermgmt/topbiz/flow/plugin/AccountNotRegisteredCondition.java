package com.example.usermgmt.topbiz.flow.plugin;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.topbiz.client.UserServiceClient;
import com.example.usermgmt.topbiz.controller.AuthController;
import com.example.usermgmt.topbiz.flow.FlowCondition;
import com.example.usermgmt.topbiz.flow.FlowContext;
import com.example.usermgmt.topbiz.flow.FlowContextKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountNotRegisteredCondition implements FlowCondition {

    private final UserServiceClient userServiceClient;

    @Override
    public String code() {
        return "account_not_registered";
    }

    @Override
    public boolean evaluate(FlowContext context) {
        AuthController.RegisterRequest request = context.get(FlowContextKeys.REGISTER_REQUEST, AuthController.RegisterRequest.class);
        ApiResponse<Boolean> response = userServiceClient.accountExists(request.getAccount());
        return response != null && response.getData() != null && !response.getData();
    }
}
