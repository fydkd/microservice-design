package com.example.usermgmt.topbiz.service;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.auth.AuthCodeLoginRequest;
import com.example.usermgmt.common.auth.AuthPasswordLoginRequest;
import com.example.usermgmt.common.auth.AuthRegisterRequest;
import com.example.usermgmt.common.auth.AuthenticatedUser;
import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.client.UserServiceClient;
import com.example.usermgmt.topbiz.config.TopbizSessionProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSessionService implements SessionService {

    private static final String SESSION_KEY_PREFIX = "topbiz:session:";
    private static final String USER_SESSION_SET_PREFIX = "topbiz:user:sessions:";

    private final UserServiceClient userServiceClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final TopbizSessionProperties sessionProperties;

    @Override
    public AuthenticatedUser loginByPassword(String account, String password, String loginIp) {
        AuthPasswordLoginRequest request = new AuthPasswordLoginRequest();
        request.setAccount(account);
        request.setPassword(password);
        request.setLoginIp(loginIp);
        ApiResponse<AuthenticatedUser> response = userServiceClient.passwordLogin(request);
        return requireSuccess(response, "User service password login failed");
    }

    @Override
    public AuthenticatedUser loginByCode(String account, String loginIp) {
        AuthCodeLoginRequest request = new AuthCodeLoginRequest();
        request.setAccount(account);
        request.setLoginIp(loginIp);
        ApiResponse<AuthenticatedUser> response = userServiceClient.codeLogin(request);
        return requireSuccess(response, "User service code login failed");
    }

    @Override
    public AuthenticatedUser register(AuthRegisterRequest request) {
        ApiResponse<AuthenticatedUser> response = userServiceClient.register(request);
        return requireSuccess(response, "User service register failed");
    }

    @Override
    public String createSession(AuthenticatedUser user, DeviceLoginContext deviceLoginContext) {
        if (sessionProperties.isSingleLoginEnabled()) {
            removeAllUserSessions(user.getUserId());
        }
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        SessionUser sessionUser = new SessionUser();
        sessionUser.setSessionId(sessionId);
        sessionUser.setUserId(user.getUserId());
        sessionUser.setNickname(user.getNickname());
        sessionUser.setAvatar(user.getAvatar());
        sessionUser.setStatus(user.getStatus());
        sessionUser.setRole(user.getRole());
        sessionUser.setMenus(user.getMenus());
        sessionUser.setDeviceId(deviceLoginContext.getDeviceId());
        sessionUser.setDeviceType(deviceLoginContext.getDeviceType());
        sessionUser.setDeviceName(deviceLoginContext.getDeviceName());
        sessionUser.setOs(deviceLoginContext.getOs());
        sessionUser.setBrowser(deviceLoginContext.getBrowser());
        sessionUser.setLoginIp(deviceLoginContext.getLoginIp());
        sessionUser.setLoginAt(now);
        sessionUser.setLastActiveAt(now);
        sessionUser.setCurrent(Boolean.FALSE);

        saveSession(sessionUser);
        stringRedisTemplate.opsForSet().add(userSessionKey(user.getUserId()), sessionId);
        stringRedisTemplate.expire(userSessionKey(user.getUserId()), sessionProperties.getTtl());
        return sessionId;
    }

    @Override
    public SessionUser getSession(String sessionId) {
        String json = stringRedisTemplate.opsForValue().get(sessionKey(sessionId));
        if (json == null) {
            throw new BusinessException(401, "Session not found or expired");
        }
        return readSession(json);
    }

    @Override
    public SessionUser touchSession(String sessionId) {
        SessionUser sessionUser = getSession(sessionId);
        sessionUser.setLastActiveAt(LocalDateTime.now());
        saveSession(sessionUser);
        return sessionUser;
    }

    @Override
    public List<SessionUser> listUserSessions(String userId, String currentSessionId) {
        Set<String> sessionIds = stringRedisTemplate.opsForSet().members(userSessionKey(userId));
        List<SessionUser> sessions = new ArrayList<>();
        if (sessionIds == null || sessionIds.isEmpty()) {
            return sessions;
        }
        for (String sessionId : sessionIds) {
            String json = stringRedisTemplate.opsForValue().get(sessionKey(sessionId));
            if (json == null) {
                stringRedisTemplate.opsForSet().remove(userSessionKey(userId), sessionId);
                continue;
            }
            SessionUser sessionUser = readSession(json);
            sessionUser.setCurrent(sessionId.equals(currentSessionId));
            sessions.add(sessionUser);
        }
        sessions.sort(Comparator.comparing(SessionUser::getLoginAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
        return sessions;
    }

    @Override
    public void removeSession(String sessionId) {
        String json = stringRedisTemplate.opsForValue().get(sessionKey(sessionId));
        if (json != null) {
            SessionUser sessionUser = readSession(json);
            stringRedisTemplate.opsForSet().remove(userSessionKey(sessionUser.getUserId()), sessionId);
        }
        stringRedisTemplate.delete(sessionKey(sessionId));
    }

    @Override
    public void removeUserSession(String userId, String sessionId) {
        SessionUser sessionUser = getSession(sessionId);
        if (!userId.equals(sessionUser.getUserId())) {
            throw new BusinessException(40301, "Cannot operate another user's session");
        }
        removeSession(sessionId);
    }

    @Override
    public void removeAllUserSessions(String userId) {
        Set<String> sessionIds = stringRedisTemplate.opsForSet().members(userSessionKey(userId));
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                stringRedisTemplate.delete(sessionKey(sessionId));
            }
        }
        stringRedisTemplate.delete(userSessionKey(userId));
    }

    private void saveSession(SessionUser sessionUser) {
        try {
            stringRedisTemplate.opsForValue().set(
                sessionKey(sessionUser.getSessionId()),
                objectMapper.writeValueAsString(sessionUser),
                sessionProperties.getTtl()
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(500, "Session serialization failed");
        }
    }

    private SessionUser readSession(String json) {
        try {
            return objectMapper.readValue(json, SessionUser.class);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(500, "Session deserialization failed");
        }
    }

    private String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String userSessionKey(String userId) {
        return USER_SESSION_SET_PREFIX + userId;
    }

    private AuthenticatedUser requireSuccess(ApiResponse<AuthenticatedUser> response, String fallbackMessage) {
        if (response == null) {
            throw new BusinessException(500, fallbackMessage);
        }
        if (response.getCode() == null || response.getCode() != 200 || response.getData() == null) {
            throw new BusinessException(response.getCode() == null ? 500 : response.getCode(), resolveMessage(response.getMessage(), fallbackMessage));
        }
        return response.getData();
    }

    private String resolveMessage(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }
}
