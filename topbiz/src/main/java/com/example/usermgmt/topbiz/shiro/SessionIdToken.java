package com.example.usermgmt.topbiz.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class SessionIdToken implements AuthenticationToken {

    private final String sessionId;

    public SessionIdToken(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public Object getPrincipal() {
        return sessionId;
    }

    @Override
    public Object getCredentials() {
        return sessionId;
    }
}
