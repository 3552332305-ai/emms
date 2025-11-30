package com.example.emms.interfaces.controller;

import com.example.emms.application.service.ComponentApplicationService;
import com.example.emms.domain.model.ComponentType;
import com.example.emms.interfaces.dto.ApiResponse;
import com.example.emms.interfaces.dto.ComponentTypeDTO;
import com.example.emms.interfaces.mapper.ManualComponentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/component-types")
@RequiredArgsConstructor
@Tag(name = "元器件类型查询(用户)")
public class UserComponentTypeController {

    private final ComponentApplicationService componentApplicationService;
    private final ManualComponentMapper manualComponentMapper;

    @GetMapping
    @Operation(summary = "获取元器件类型列表(下拉框用)")
    public ApiResponse<List<ComponentTypeDTO>> getComponentTypesForSelect() {
        List<ComponentType> types = componentApplicationService.getAvailableComponentTypes();
        List<ComponentTypeDTO> dtos = types.stream()
                .map(manualComponentMapper::toDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索元器件类型")
    public ApiResponse<List<ComponentTypeDTO>> searchComponentTypes(@RequestParam String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<ComponentType> types = componentApplicationService.getAvailableComponentTypes().stream()
                .filter(type -> {
                    if (normalized.isEmpty()) {
                        return true;
                    }
                    return type.getName().toLowerCase(Locale.ROOT).contains(normalized)
                            || type.getModel().toLowerCase(Locale.ROOT).contains(normalized);
                })
                .collect(Collectors.toList());
        List<ComponentTypeDTO> dtos = types.stream()
                .map(manualComponentMapper::toDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }
}