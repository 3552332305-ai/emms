package com.example.emms.interfaces.controller;

import com.example.emms.application.service.ComponentApplicationService;
import com.example.emms.domain.model.ComponentType;
import com.example.emms.interfaces.dto.ApiResponse;
import com.example.emms.interfaces.dto.ComponentTypeDTO;
import com.example.emms.interfaces.mapper.ComponentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/component-types")
@RequiredArgsConstructor
@Tag(name = "元器件类型管理")
public class ComponentTypeController {

    private final ComponentApplicationService componentApplicationService;
    private final ComponentMapper componentMapper;

    @GetMapping
    @Operation(summary = "获取所有元器件类型")
    public ApiResponse<List<ComponentTypeDTO>> getAllComponentTypes() {
        List<ComponentType> types = componentApplicationService.getAllComponentTypes();
        List<ComponentTypeDTO> dtos = types.stream()
                .map(componentMapper::toDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }
}