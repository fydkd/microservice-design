package com.example.usermgmt.topbiz.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.auth.AuthRegisterRequest;
import com.example.usermgmt.common.auth.AuthResetPasswordRequest;
import com.example.usermgmt.common.auth.AuthenticatedUser;
import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.client.NotificationServiceClient;
import com.example.usermgmt.topbiz.client.UserServiceClient;
import com.example.usermgmt.topbiz.config.FlowProperties;
import com.example.usermgmt.topbiz.context.SessionContext;
import com.example.usermgmt.topbiz.flow.FlowContext;
import com.example.usermgmt.topbiz.flow.FlowContextKeys;
import com.example.usermgmt.topbiz.flow.FlowEngine;
import com.example.usermgmt.topbiz.service.DeviceLoginContext;
import com.example.usermgmt.topbiz.service.FeishuOAuthService;
import com.example.usermgmt.topbiz.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final SessionService sessionService;
    private final NotificationServiceClient notificationServiceClient;
    private final UserServiceClient userServiceClient;
    private final FeishuOAuthService feishuOAuthService;
    private final FlowEngine flowEngine;
    private final FlowProperties flowProperties;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        DeviceLoginContext deviceContext = buildDeviceContext(request, httpServletRequest);
        AuthenticatedUser user;
        if ("password".equalsIgnoreCase(request.getLoginType())) {
            user = sessionService.loginByPassword(request.getAccount(), request.getPassword(), deviceContext.getLoginIp());
        } else if ("code".equalsIgnoreCase(request.getLoginType())) {
            verifyCaptcha(resolveCaptchaType(request.getAccount()), request.getAccount(), "login", request.getCode());
            user = sessionService.loginByCode(request.getAccount(), deviceContext.getLoginIp());
        } else {
            throw new BusinessException(40006, "Invalid login type");
        }
        String sessionId = sessionService.createSession(user, deviceContext);
        return ApiResponse.success("Login successful", LoginResponse.from(user, sessionId));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpServletRequest) {
        detectAccountType(request.getAccount());
        FlowContext context = new FlowContext()
            .put(FlowContextKeys.REGISTER_REQUEST, request)
            .put(FlowContextKeys.DEVICE_CONTEXT, buildDeviceContext(request, httpServletRequest));
        flowEngine.execute(flowProperties.getRegisterCode(), context);
        return ApiResponse.success("Registration successful", context.get(FlowContextKeys.LOGIN_RESPONSE, LoginResponse.class));
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        verifyCaptcha(resolveCaptchaType(request.getAccount()), request.getAccount(), "reset_password", request.getCode());
        AuthResetPasswordRequest payload = new AuthResetPasswordRequest();
        payload.setAccount(request.getAccount());
        payload.setNewPassword(request.getNewPassword());
        ApiResponse<Void> response = userServiceClient.resetPassword(payload);
        if (response == null || response.getCode() == null || response.getCode() != 200) {
            throw new BusinessException(response == null || response.getCode() == null ? 500 : response.getCode(), response == null ? "Password reset failed" : response.getMessage());
        }
        return ApiResponse.success("Password reset successful", null);
    }

    @GetMapping("/oauth/feishu/authorize")
    public ApiResponse<Map<String, String>> feishuAuthorize(@RequestParam("redirectUri") String redirectUri) {
        return ApiResponse.success(feishuOAuthService.buildAuthorizeUrl(redirectUri));
    }

    @GetMapping("/oauth/feishu/callback")
    public ApiResponse<LoginResponse> feishuCallback(
        @RequestParam("code") String code,
        @RequestParam("state") String state,
        @RequestParam(required = false) String deviceId,
        @RequestParam(required = false) String deviceType,
        @RequestParam(required = false) String deviceName,
        @RequestParam(required = false) String os,
        @RequestParam(required = false) String browser,
        HttpServletRequest httpServletRequest
    ) {
        OAuthDeviceRequest request = new OAuthDeviceRequest();
        request.setDeviceId(deviceId);
        request.setDeviceType(deviceType);
        request.setDeviceName(deviceName);
        request.setOs(os);
        request.setBrowser(browser);
        return ApiResponse.success("Login successful", feishuOAuthService.callback(code, state, buildDeviceContext(request, httpServletRequest)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("SessionId") String sessionId) {
        sessionService.removeSession(sessionId);
        return ApiResponse.success("Logout successful", null);
    }

    @PostMapping("/cancel")
    public ApiResponse<Void> cancelCurrentAccount() {
        FlowContext context = new FlowContext().put(FlowContextKeys.CURRENT_USER, SessionContext.get());
        flowEngine.execute(flowProperties.getCancelCode(), context);
        return ApiResponse.success("Account cancelled", null);
    }

    @GetMapping("/session/me")
    public ApiResponse<SessionUser> currentSession(@RequestHeader("SessionId") String sessionId) {
        SessionUser sessionUser = sessionService.getSession(sessionId);
        sessionUser.setCurrent(Boolean.TRUE);
        return ApiResponse.success(sessionUser);
    }

    @GetMapping("/devices")
    public ApiResponse<List<SessionUser>> listDevices(@RequestHeader("SessionId") String sessionId) {
        SessionUser current = SessionContext.get();
        return ApiResponse.success(sessionService.listUserSessions(current.getUserId(), sessionId));
    }

    @DeleteMapping("/devices/{sessionId}")
    public ApiResponse<Void> logoutDevice(@PathVariable("sessionId") String sessionId) {
        SessionUser current = SessionContext.get();
        sessionService.removeUserSession(current.getUserId(), sessionId);
        return ApiResponse.success("Device session removed", null);
    }

    private void verifyCaptcha(String captchaType, String account, String scene, String code) {
        Map<String, Object> request = new HashMap<>();
        request.put("captchaType", captchaType);
        request.put("account", account);
        request.put("scene", scene);
        request.put("code", code);
        ApiResponse<Object> response = notificationServiceClient.verifyCaptcha(request);
        if (response == null || response.getCode() == null || response.getCode() != 200) {
            throw new BusinessException(40004, "Verification code is invalid or expired");
        }
    }

    public static String detectAccountType(String account) {
        if (account == null || account.isBlank()) {
            throw new BusinessException(40003, "Please enter a valid phone number or email");
        }
        String normalized = account.trim();
        if (PHONE_PATTERN.matcher(normalized).matches()) {
            return "phone";
        }
        if (EMAIL_PATTERN.matcher(normalized).matches()) {
            return "email";
        }
        throw new BusinessException(40003, "Please enter a valid phone number or email");
    }

    private String resolveCaptchaType(String account) {
        return "email".equals(detectAccountType(account)) ? "email" : "sms";
    }

    private DeviceLoginContext buildDeviceContext(DeviceAwareRequest request, HttpServletRequest httpServletRequest) {
        DeviceLoginContext context = new DeviceLoginContext();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        String deviceType = resolveDeviceType(request.getDeviceType(), userAgent);
        String os = firstNonBlank(request.getOs(), parseOs(userAgent));
        String browser = firstNonBlank(request.getBrowser(), parseBrowser(userAgent));
        context.setDeviceId(null);
        context.setDeviceType(deviceType);
        context.setDeviceName(resolveDeviceName(deviceType, os, browser));
        context.setOs(os);
        context.setBrowser(browser);
        context.setLoginIp(resolveClientIp(httpServletRequest));
        return context;
    }

    private String resolveDeviceType(String deviceType, String userAgent) {
        if (deviceType != null && !deviceType.isBlank()) {
            return normalizeDeviceType(deviceType);
        }
        String ua = userAgent == null ? "" : userAgent.toLowerCase();
        if (ua.contains("iphone") || ua.contains("android") || ua.contains("mobile")) {
            return "mobile";
        }
        return "desktop";
    }

    private String normalizeDeviceType(String deviceType) {
        String normalized = deviceType == null ? "" : deviceType.trim().toLowerCase();
        if (normalized.contains("mobile") || normalized.contains("phone") || normalized.contains("android") || normalized.contains("ios")) {
            return "mobile";
        }
        return "desktop";
    }

    private String resolveDeviceName(String deviceType, String os, String browser) {
        String typeLabel = "mobile".equals(deviceType) ? "Mobile" : "Desktop";
        if ("Unknown".equals(os) && "Unknown".equals(browser)) {
            return typeLabel;
        }
        return typeLabel + " · " + os + " / " + browser;
    }

    private String parseOs(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase();
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os")) {
            return "macOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }

    private String parseBrowser(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase();
        if (ua.contains("edg")) {
            return "Edge";
        }
        if (ua.contains("chrome")) {
            return "Chrome";
        }
        if (ua.contains("firefox")) {
            return "Firefox";
        }
        if (ua.contains("safari")) {
            return "Safari";
        }
        return "Unknown";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    @Data
    public static class LoginRequest implements DeviceAwareRequest {
        @NotBlank
        private String loginType;
        @NotBlank
        private String account;
        private String code;
        private String password;
        private String deviceId;
        private String deviceType;
        private String deviceName;
        private String os;
        private String browser;
    }

    @Data
    public static class RegisterRequest implements DeviceAwareRequest {
        private String account;
        private String registerType;
        private String phone;
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String code;
        private String nickname;
        private String deviceId;
        private String deviceType;
        private String deviceName;
        private String os;
        private String browser;

        public String getAccount() {
            if (account != null && !account.isBlank()) {
                return account.trim();
            }
            if (email != null && !email.isBlank()) {
                return email.trim();
            }
            if (phone != null && !phone.isBlank()) {
                return phone.trim();
            }
            return account;
        }
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank
        private String account;
        @NotBlank
        private String code;
        @NotBlank
        private String newPassword;
    }

    @Data
    public static class OAuthDeviceRequest implements DeviceAwareRequest {
        private String deviceId;
        private String deviceType;
        private String deviceName;
        private String os;
        private String browser;
    }

    public interface DeviceAwareRequest {
        String getDeviceId();
        String getDeviceType();
        String getDeviceName();
        String getOs();
        String getBrowser();
    }

    @Data
    public static class LoginResponse {
        private String userId;
        private String nickname;
        private String avatar;
        private String status;
        private String sessionId;
        private List<String> menus;

        public static LoginResponse from(AuthenticatedUser user, String sessionId) {
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getUserId());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
            response.setStatus(resolveStatus(user.getStatus()));
            response.setSessionId(sessionId);
            response.setMenus(user.getMenus());
            return response;
        }

        private static String resolveStatus(Integer status) {
            if (status == null || status == 1) {
                return "active";
            }
            if (status == 2) {
                return "disabled";
            }
            if (status == 3) {
                return "locked";
            }
            return "cancelled";
        }
    }
}
