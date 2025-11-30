package com.example.emms.interfaces.controller;

import com.example.emms.domain.model.ComponentType;
import com.example.emms.domain.repository.ComponentTypeRepository;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/audit")  // 改为前端期望的路径
@RequiredArgsConstructor
@Tag(name = "管理员审核")
public class AdminApprovalController {

    private final ComponentTypeRepository componentTypeRepository;

    // 获取待审核的元器件
    @GetMapping("/pending")
    @Operation(summary = "获取待审核元器件")
    public ApiResponse<List<ComponentType>> getPendingComponents() {
        List<ComponentType> pending = componentTypeRepository.findByStatus("PENDING");
        return ApiResponse.success(pending);
    }

    // 审核通过
    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过")
    public ApiResponse<ComponentType> approveComponent(@PathVariable Long id) {
        Optional<ComponentType> component = componentTypeRepository.findById(id);
        if (component.isEmpty()) {
            return ApiResponse.error("元器件不存在");
        }

        ComponentType comp = component.get();
        comp.setStatus("APPROVED");
        return ApiResponse.success(componentTypeRepository.save(comp));
    }

    // 审核拒绝
    @PutMapping("/{id}/reject")
    @Operation(summary = "审核拒绝")
    public ApiResponse<ComponentType> rejectComponent(@PathVariable Long id) {
        Optional<ComponentType> component = componentTypeRepository.findById(id);
        if (component.isEmpty()) {
            return ApiResponse.error("元器件不存在");
        }

        ComponentType comp = component.get();
        comp.setStatus("REJECTED");
        return ApiResponse.success(componentTypeRepository.save(comp));
    }
}
