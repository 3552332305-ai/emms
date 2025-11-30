package com.example.emms.interfaces.controller;

import com.example.emms.domain.model.User;
import com.example.emms.domain.repository.UserRepository;
import com.example.emms.domain.service.SessionAuthService;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final SessionAuthService sessionAuthService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Optional<String> sessionIdOpt = sessionAuthService.authenticate(request.getUsername(), request.getPassword());

        if (sessionIdOpt.isPresent()) {
            var user = sessionAuthService.getUserBySession(sessionIdOpt.get()).get();

            Map<String, Object> result = new HashMap<>();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("supplierId", user.getSupplierId());

            result.put("user", userInfo);
            result.put("sessionId", sessionIdOpt.get());

            return ApiResponse.success(result);
        }

        return ApiResponse.error("用户名或密码错误");
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ApiResponse<String> logout(@RequestHeader("Session-Id") String sessionId) {
        sessionAuthService.logout(sessionId);
        return ApiResponse.success("登出成功");
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<Map<String, Object>> getCurrentUser(@RequestHeader("Session-Id") String sessionId) {
        Optional<User> userOpt = sessionAuthService.getUserBySession(sessionId);

        if (userOpt.isPresent()) {
            var user = userOpt.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("supplierId", user.getSupplierId());

            return ApiResponse.success(userInfo);
        }

        return ApiResponse.error("用户未登录");
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ApiResponse.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(User.UserRole.USER);
        // 只保存非空的邮箱和手机号
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }
        userRepository.save(user);

        return ApiResponse.success("注册成功");
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
    }
}