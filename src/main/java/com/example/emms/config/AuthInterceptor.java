package com.example.emms.config;

import com.example.emms.domain.model.User;
import com.example.emms.domain.service.SessionAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionAuthService sessionAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许登录和公开接口
        if (request.getRequestURI().startsWith("/api/auth/") ||
            request.getRequestURI().startsWith("/swagger-ui/") ||
            request.getRequestURI().startsWith("/v3/api-docs") ||
            request.getRequestURI().startsWith("/api/files/")) {
            return true;
        }

        String sessionId = request.getHeader("Session-Id");
        if (sessionId == null || sessionId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"未登录\"}");
            return false;
        }

        var userOpt = sessionAuthService.getUserBySession(sessionId);
        if (userOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"会话已过期\"}");
            return false;
        }

        // 简单的角色检查
        var user = userOpt.get();
        String uri = request.getRequestURI();

        if (uri.startsWith("/api/admin/") && user.getRole() != User.UserRole.ADMIN) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"无权限访问管理员接口\"}");
            return false;
        }

        if (uri.startsWith("/api/supplier/") && user.getRole() != User.UserRole.SUPPLIER) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"无权限访问供应商接口\"}");
            return false;
        }

        return true;
    }
}
