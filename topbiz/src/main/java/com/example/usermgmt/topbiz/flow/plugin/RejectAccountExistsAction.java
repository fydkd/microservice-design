package com.example.usermgmt.topbiz.flow.plugin;

import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.controller.AuthController;
import com.example.usermgmt.topbiz.flow.FlowAction;
import com.example.usermgmt.topbiz.flow.FlowContext;
import com.example.usermgmt.topbiz.flow.FlowContextKeys;
import org.springframework.stereotype.Component;

@Component
public class RejectAccountExistsAction implements FlowAction {

    @Override
    public String code() {
        return "reject_account_exists";
    }

    @Override
    public void execute(FlowContext context) {
        AuthController.RegisterRequest request = context.get(FlowContextKeys.REGISTER_REQUEST, AuthController.RegisterRequest.class);
        if ("email".equalsIgnoreCase(AuthController.detectAccountType(request.getAccount()))) {
            throw new BusinessException(41001, "邮箱已注册");
        }
        throw new BusinessException(40001, "手机号已注册");
    }
}
