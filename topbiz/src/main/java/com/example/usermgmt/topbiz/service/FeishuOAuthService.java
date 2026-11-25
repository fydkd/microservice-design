package com.example.usermgmt.topbiz.service;

import com.example.usermgmt.common.auth.AuthFeishuLoginRequest;
import com.example.usermgmt.common.auth.AuthenticatedUser;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.client.UserServiceClient;
import com.example.usermgmt.topbiz.config.FeishuOAuthProperties;
import com.example.usermgmt.topbiz.controller.AuthController;
import com.example.usermgmt.topbiz.dto.FeishuUserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class FeishuOAuthService {

    private static final String STATE_KEY_PREFIX = "topbiz:oauth:feishu:state:";

    private final FeishuOAuthProperties feishuOAuthProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;
    private final SessionService sessionService;

    public Map<String, String> buildAuthorizeUrl(String redirectUri) {
        if (!feishuOAuthProperties.isEnabled()) {
            throw new BusinessException(42001, "飞书登录未开启");
        }
        String state = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(STATE_KEY_PREFIX + state, redirectUri, Duration.ofMinutes(10));
        String url = feishuOAuthProperties.getAuthorizeUrl()
            + "?app_id=" + encode(feishuOAuthProperties.getAppId())
            + "&redirect_uri=" + encode(redirectUri)
            + "&response_type=code"
            + "&state=" + encode(state);
        Map<String, String> result = new HashMap<>();
        result.put("authorizeUrl", url);
        result.put("state", state);
        return result;
    }

    public AuthController.LoginResponse callback(String code, String state, DeviceLoginContext deviceLoginContext) {
        String redirect = stringRedisTemplate.opsForValue().get(STATE_KEY_PREFIX + state);
        if (redirect == null) {
            throw new BusinessException(42103, "state 参数无效");
        }
        stringRedisTemplate.delete(STATE_KEY_PREFIX + state);

        RestTemplate restTemplate = new RestTemplate();
        String appAccessToken = getAppAccessToken(restTemplate);
        String userAccessToken = getUserAccessToken(restTemplate, appAccessToken, code);
        FeishuUserInfo userInfo = getUserInfo(restTemplate, userAccessToken);

        AuthFeishuLoginRequest request = new AuthFeishuLoginRequest();
        request.setOpenId(userInfo.getOpenId());
        request.setUnionId(userInfo.getUnionId());
        request.setUserId(userInfo.getUserId());
        request.setMobile(userInfo.getMobile());
        request.setEmail(userInfo.getEmail());
        request.setEnterpriseEmail(userInfo.getEnterpriseEmail());
        request.setName(userInfo.getName());
        request.setAvatarUrl(userInfo.getAvatarUrl());

        var response = userServiceClient.feishuLogin(request);
        if (response == null || response.getCode() == null || response.getCode() != 200 || response.getData() == null) {
            throw new BusinessException(42102, "获取第三方用户信息失败");
        }
        AuthenticatedUser user = response.getData();
        String sessionId = sessionService.createSession(user, deviceLoginContext);
        return AuthController.LoginResponse.from(user, sessionId);
    }

    private String getAppAccessToken(RestTemplate restTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = new HashMap<>();
        body.put("app_id", feishuOAuthProperties.getAppId());
        body.put("app_secret", feishuOAuthProperties.getAppSecret());
        JsonNode data = post(restTemplate, feishuOAuthProperties.getAppAccessTokenUrl(), headers, body);
        return data.path("app_access_token").asText();
    }

    private String getUserAccessToken(RestTemplate restTemplate, String appAccessToken, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(appAccessToken);
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        JsonNode data = post(restTemplate, feishuOAuthProperties.getUserAccessTokenUrl(), headers, body).path("data");
        return data.path("access_token").asText();
    }

    private FeishuUserInfo getUserInfo(RestTemplate restTemplate, String userAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userAccessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(feishuOAuthProperties.getUserInfoUrl(), HttpMethod.GET, entity, String.class);
        try {
            JsonNode root = objectMapper.readTree(response.getBody()).path("data");
            FeishuUserInfo userInfo = new FeishuUserInfo();
            userInfo.setOpenId(root.path("open_id").asText());
            userInfo.setUnionId(root.path("union_id").asText());
            userInfo.setUserId(root.path("user_id").asText());
            userInfo.setName(root.path("name").asText());
            userInfo.setAvatarUrl(root.path("avatar_url").asText());
            userInfo.setAvatarThumb(root.path("avatar_thumb").asText());
            userInfo.setAvatarMiddle(root.path("avatar_middle").asText());
            userInfo.setAvatarBig(root.path("avatar_big").asText());
            userInfo.setEmail(root.path("email").asText());
            userInfo.setEnterpriseEmail(root.path("enterprise_email").asText());
            userInfo.setMobile(root.path("mobile").asText());
            return userInfo;
        } catch (Exception exception) {
            throw new BusinessException(42102, "获取第三方用户信息失败");
        }
    }

    private JsonNode post(RestTemplate restTemplate, String url, HttpHeaders headers, Object body) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (!"0".equals(root.path("code").asText())) {
                throw new BusinessException(42101, "授权码无效或已过期");
            }
            return root;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(42102, "获取第三方用户信息失败");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
