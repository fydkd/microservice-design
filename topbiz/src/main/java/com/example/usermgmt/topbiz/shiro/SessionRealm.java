package com.example.usermgmt.topbiz.shiro;

import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.topbiz.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionRealm extends AuthenticatingRealm {

    private final SessionService sessionService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof SessionIdToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String sessionId = (String) token.getPrincipal();
        SessionUser sessionUser = sessionService.touchSession(sessionId);
        return new SimpleAuthenticationInfo(sessionUser, sessionId, getName());
    }
}
