package com.example.usermgmt.topbiz.flow.plugin;

import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.topbiz.flow.FlowAction;
import com.example.usermgmt.topbiz.flow.FlowContext;
import com.example.usermgmt.topbiz.flow.FlowContextKeys;
import com.example.usermgmt.topbiz.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClearUserSessionsAction implements FlowAction {

    private final SessionService sessionService;

    @Override
    public String code() {
        return "clear_user_sessions";
    }

    @Override
    public void execute(FlowContext context) {
        SessionUser currentUser = context.get(FlowContextKeys.CURRENT_USER, SessionUser.class);
        sessionService.removeAllUserSessions(currentUser.getUserId());
    }
}
