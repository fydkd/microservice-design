package com.example.usermgmt.topbiz.shiro;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.auth.SessionUser;
import com.example.usermgmt.common.exception.BusinessException;
import com.example.usermgmt.topbiz.context.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ShiroAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = List.of(
        "/api/v1/login",
        "/api/v1/register",
        "/api/v1/password/reset",
        "/api/v1/groups/public"
    );

    private final SecurityManager securityManager;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/")
            || EXCLUDED_PATHS.contains(path)
            || path.startsWith("/api/v1/captcha/")
            || path.startsWith("/api/v1/oauth/feishu/")
            || path.startsWith("/api/v1/flows/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (CorsUtils.isPreFlightRequest(request) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String sessionId = request.getHeader("SessionId");
        if (sessionId == null || sessionId.isBlank()) {
            writeUnauthorized(response, 401, "Missing SessionId");
            return;
        }

        Subject subject = new Subject.Builder(securityManager).buildSubject();
        SubjectThreadState threadState = new SubjectThreadState(subject);
        threadState.bind();
        try {
            subject.login(new SessionIdToken(sessionId));
            Object principal = subject.getPrincipal();
            if (!(principal instanceof SessionUser sessionUser)) {
                throw new BusinessException(401, "Session not found or expired");
            }
            SessionContext.set(sessionUser);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException | BusinessException exception) {
            writeUnauthorized(response, 401, exception.getMessage() == null ? "未登录或会话已失效" : exception.getMessage());
        } finally {
            SessionContext.clear();
            threadState.clear();
            ThreadContext.remove();
        }
    }

    private void writeUnauthorized(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(code, message)));
    }
}
