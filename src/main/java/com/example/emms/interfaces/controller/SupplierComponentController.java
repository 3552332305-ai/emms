package com.example.emms.interfaces.controller;

import com.example.emms.domain.model.ComponentType;
import com.example.emms.domain.model.Supplier;
import com.example.emms.domain.model.User;
import com.example.emms.domain.repository.ComponentTypeRepository;
import com.example.emms.domain.repository.SupplierRepository;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/supplier/components")
@RequiredArgsConstructor
@Tag(name = "供应商元器件管理")
@SuppressWarnings("null")
public class SupplierComponentController {

    private final ComponentTypeRepository componentTypeRepository;
    private final SupplierRepository supplierRepository;

    // 获取当前供应商的元器件
    @GetMapping
    @Operation(summary = "获取我的元器件")
    public ApiResponse<List<ComponentType>> getMyComponents(@RequestAttribute("currentUser") User currentUser) {
        try {
            Supplier supplier = getCurrentSupplier(currentUser);
            long supplierId = getSupplierId(supplier);
        List<ComponentType> components = componentTypeRepository.findBySupplierId(supplierId);
        return ApiResponse.success(components);
        } catch (IllegalStateException ex) {
            return ApiResponse.error(ex.getMessage());
        }
    }

    // 供应商添加元器件（待审核状态）
    @PostMapping
    @Operation(summary = "添加元器件")
    public ApiResponse<ComponentType> addComponent(@RequestAttribute("currentUser") User currentUser,
                                                  @RequestBody ComponentType componentType) {
        try {
            Supplier supplier = getCurrentSupplier(currentUser);
            componentType.setSupplier(supplier);
        componentType.setStatus("PENDING"); // 待审核状态
            if (componentType.getUnitPrice() == null) {
                componentType.setUnitPrice(0.0);
            }
            ComponentType saved = componentTypeRepository.save(componentType);
            return ApiResponse.success(saved);
        } catch (IllegalStateException ex) {
            return ApiResponse.error(ex.getMessage());
        }
    }

    // 供应商更新自己的元器件
    @PutMapping("/{id}")
    @Operation(summary = "更新元器件")
    public ApiResponse<ComponentType> updateComponent(@RequestAttribute("currentUser") User currentUser,
                                                     @PathVariable Long id,
                                                     @RequestBody ComponentType componentType) {
        try {
            Supplier supplier = getCurrentSupplier(currentUser);
            long supplierId = getSupplierId(supplier);
        Optional<ComponentType> existing = componentTypeRepository.findById(id);
            if (existing.isEmpty()) {
                return ApiResponse.error("元器件不存在");
            }
            ComponentType existingComponent = existing.get();
            Supplier existingSupplier = existingComponent.getSupplier();
            long existingSupplierId = getSupplierId(existingSupplier);
            if (existingSupplierId != supplierId) {
                return ApiResponse.error("无权限修改此元器件");
        }

        componentType.setId(id);
            componentType.setSupplier(existingSupplier);
        componentType.setStatus("PENDING"); // 更新后重新等待审核
            ComponentType updated = componentTypeRepository.save(componentType);
            return ApiResponse.success(updated);
        } catch (IllegalStateException ex) {
            return ApiResponse.error(ex.getMessage());
        }
    }

    // 供应商删除自己的元器件
    @DeleteMapping("/{id}")
    @Operation(summary = "删除元器件")
    public ApiResponse<String> deleteComponent(@RequestAttribute("currentUser") User currentUser,
                                              @PathVariable Long id) {
        try {
            Supplier supplier = getCurrentSupplier(currentUser);
            long supplierId = getSupplierId(supplier);
        Optional<ComponentType> component = componentTypeRepository.findById(id);
            if (component.isEmpty()) {
                return ApiResponse.error("元器件不存在");
            }
            ComponentType existingComponent = component.get();
            Supplier existingSupplier = existingComponent.getSupplier();
            long existingSupplierId = getSupplierId(existingSupplier);
            if (existingSupplierId != supplierId) {
                return ApiResponse.error("无权限删除此元器件");
        }

        componentTypeRepository.deleteById(id);
        return ApiResponse.success("删除成功");
        } catch (IllegalStateException ex) {
            return ApiResponse.error(ex.getMessage());
        }
    }

    private Supplier getCurrentSupplier(User currentUser) {
        if (currentUser == null || currentUser.getRole() != User.UserRole.SUPPLIER) {
            throw new IllegalStateException("无权限");
        }
        Long supplierId = currentUser.getSupplierId();
        if (supplierId == null) {
            throw new IllegalStateException("账号未绑定供应商");
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalStateException("供应商不存在"));
    }

    private long getSupplierId(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalStateException("供应商信息不完整");
        }
        Long supplierId = supplier.getId();
        if (supplierId == null) {
            throw new IllegalStateException("供应商信息不完整");
        }
        return supplierId;
    }
}