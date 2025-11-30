package com.example.emms.interfaces.controller;

import com.example.emms.domain.model.ComponentType;
import com.example.emms.domain.repository.ComponentTypeRepository;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.example.emms.domain.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/component-types")
@RequiredArgsConstructor
@Tag(name = "元器件类型管理(管理员)")
public class AdminComponentTypeController {

    private final ComponentTypeRepository componentTypeRepository;
    private final FileStorageService fileStorageService;

    @PostMapping
    @Operation(summary = "创建元器件类型")
    public ApiResponse<ComponentType> createComponentType(@RequestBody ComponentType componentType) {
        if (componentTypeRepository.existsByModel(componentType.getModel())) {
            return ApiResponse.error("型号已存在");
        }
        return ApiResponse.success(componentTypeRepository.save(componentType));
    }

    @PostMapping("/with-image")
    @Operation(summary = "创建元器件类型（带图片上传）")
    public ApiResponse<ComponentType> createComponentTypeWithImage(
            @RequestPart("componentType") ComponentType componentType,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            if (componentTypeRepository.existsByModel(componentType.getModel())) {
                return ApiResponse.error("型号已存在");
            }

            // 处理图片上传
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(imageFile);
                componentType.setImage(fileName);
            }

            return ApiResponse.success(componentTypeRepository.save(componentType));
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新元器件类型")
    public ApiResponse<ComponentType> updateComponentType(@PathVariable Long id, @RequestBody ComponentType componentType) {
        componentType.setId(id);
        return ApiResponse.success(componentTypeRepository.save(componentType));
    }

    @PutMapping("/{id}/with-image")
    @Operation(summary = "更新元器件类型（带图片上传）")
    public ApiResponse<ComponentType> updateComponentTypeWithImage(
            @PathVariable Long id,
            @RequestPart("componentType") ComponentType componentType,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            componentType.setId(id);

            // 处理图片上传
            if (imageFile != null && !imageFile.isEmpty()) {
                // 删除旧图片
                ComponentType existing = componentTypeRepository.findById(id).orElse(null);
                if (existing != null && existing.getImage() != null) {
                    fileStorageService.deleteFile(existing.getImage());
                }

                // 保存新图片
                String fileName = fileStorageService.storeFile(imageFile);
                componentType.setImage(fileName);
            }

            return ApiResponse.success(componentTypeRepository.save(componentType));
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除元器件类型")
    public ApiResponse<String> deleteComponentType(@PathVariable Long id) {
        try {
            // 删除关联的图片
            ComponentType componentType = componentTypeRepository.findById(id).orElse(null);
            if (componentType != null && componentType.getImage() != null) {
                fileStorageService.deleteFile(componentType.getImage());
            }

            componentTypeRepository.deleteById(id);
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "获取所有元器件类型(详细)")
    public ApiResponse<List<ComponentType>> getAllComponentTypes() {
        return ApiResponse.success(componentTypeRepository.findAll());
    }
}