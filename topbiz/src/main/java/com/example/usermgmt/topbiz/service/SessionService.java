package com.example.usermgmt.topbiz.service;

import com.example.usermgmt.common.auth.AuthRegisterRequest;
import com.example.usermgmt.common.auth.AuthenticatedUser;
import com.example.usermgmt.common.auth.SessionUser;
import java.util.List;

public interface SessionService {

    AuthenticatedUser loginByPassword(String account, String password, String loginIp);

    AuthenticatedUser loginByCode(String account, String loginIp);

    AuthenticatedUser register(AuthRegisterRequest request);

    String createSession(AuthenticatedUser user, DeviceLoginContext deviceLoginContext);

    SessionUser getSession(String sessionId);

    SessionUser touchSession(String sessionId);

    List<SessionUser> listUserSessions(String userId, String currentSessionId);

    void removeSession(String sessionId);

    void removeUserSession(String userId, String sessionId);

    void removeAllUserSessions(String userId);
}
