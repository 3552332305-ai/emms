package com.example.emms.interfaces.controller;

import com.example.emms.domain.model.Supplier;
import com.example.emms.domain.model.User;
import com.example.emms.domain.repository.SupplierRepository;
import com.example.emms.domain.service.SessionAuthService;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier/profile")
@RequiredArgsConstructor
@Tag(name = "供应商个人信息")
public class SupplierProfileController {

    private final SessionAuthService sessionAuthService;
    private final SupplierRepository supplierRepository;

    @GetMapping
    @Operation(summary = "获取当前供应商信息")
    public ApiResponse<Supplier> getProfile(@RequestHeader("Session-Id") String sessionId) {
        var userOpt = sessionAuthService.getUserBySession(sessionId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("会话已过期");
        }

        User user = userOpt.get();
        if (user.getRole() != User.UserRole.SUPPLIER) {
            return ApiResponse.error("无权限");
        }

        Long supplierId = user.getSupplierId();
        if (supplierId == null) {
            return ApiResponse.error("账号未绑定供应商");
        }

        return supplierRepository.findById(supplierId)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error("供应商不存在"));
    }
}

