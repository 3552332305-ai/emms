package com.example.emms.config;

import com.example.emms.domain.model.User;
import com.example.emms.domain.service.SessionAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SimpleAuthFilter implements Filter {

    private final SessionAuthService sessionAuthService;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 设置 CORS 头
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Session-Id, Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 公开接口不需要认证
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/api/auth/") ||
            path.startsWith("/swagger-ui/") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/api/files/") ||
            path.equals("/api/supplier/profile")) {
            chain.doFilter(request, response);
            return;
        }

        // 检查认证
        String sessionId = httpRequest.getHeader("Session-Id");
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }

        var userOpt = sessionAuthService.getUserBySession(sessionId);
        if (userOpt.isEmpty()) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "会话已过期");
            return;
        }

        var user = userOpt.get();
        httpRequest.setAttribute("currentUser", user);

        // 检查权限
        if (path.startsWith("/api/admin/") && user.getRole() != User.UserRole.ADMIN) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "无权限访问管理员接口");
            return;
        }

        if (path.startsWith("/api/supplier/") && user.getRole() != User.UserRole.SUPPLIER) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "无权限访问供应商接口");
            return;
        }

        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}