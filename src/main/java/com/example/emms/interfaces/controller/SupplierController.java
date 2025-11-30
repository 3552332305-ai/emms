package com.example.emms.interfaces.controller;

import com.example.emms.domain.model.Supplier;
import com.example.emms.domain.repository.SupplierRepository;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
@Tag(name = "供应商管理")
public class SupplierController {

    private final SupplierRepository supplierRepository;

    @GetMapping
    @Operation(summary = "获取所有供应商")
    public ApiResponse<List<Supplier>> getAllSuppliers() {
        return ApiResponse.success(supplierRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取供应商")
    public ApiResponse<Supplier> getSupplierById(@PathVariable Long id) {
        return supplierRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("供应商不存在"));
    }

    @PostMapping
    @Operation(summary = "创建供应商")
    public ApiResponse<Supplier> createSupplier(@RequestBody Supplier supplier) {
        // 检查供应商名称是否已存在
        if (supplierRepository.existsByName(supplier.getName())) {
            return ApiResponse.error("供应商名称已存在");
        }
        return ApiResponse.success(supplierRepository.save(supplier));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新供应商")
    public ApiResponse<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        if (!supplierRepository.existsById(id)) {
            return ApiResponse.error("供应商不存在");
        }
        supplier.setId(id);
        return ApiResponse.success(supplierRepository.save(supplier));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除供应商")
    public ApiResponse<String> deleteSupplier(@PathVariable Long id) {
        if (!supplierRepository.existsById(id)) {
            return ApiResponse.error("供应商不存在");
        }

        // 检查是否有元器件关联此供应商
        // 这里需要添加检查逻辑，如果有关联则不能删除

        supplierRepository.deleteById(id);
        return ApiResponse.success("删除成功");
    }

    @GetMapping("/search")
    @Operation(summary = "搜索供应商")
    public ApiResponse<List<Supplier>> searchSuppliers(@RequestParam String keyword) {
        List<Supplier> suppliers = supplierRepository.findByNameContaining(keyword);
        return ApiResponse.success(suppliers);
    }
}
