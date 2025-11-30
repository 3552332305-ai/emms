package com.example.emms.interfaces.controller;

import com.example.emms.application.service.ComponentApplicationService;
import com.example.emms.domain.service.ComponentInventoryService;
import com.example.emms.interfaces.dto.ApiResponse;
import com.example.emms.interfaces.dto.InventoryStatisticsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "统计查询")
public class StatisticsController {

    private final ComponentInventoryService inventoryService;
    private final ComponentApplicationService componentApplicationService;

    @GetMapping("/inventory")
    @Operation(summary = "按类型统计库存总量")
    public ApiResponse<List<InventoryStatisticsDTO>> getInventoryStatistics() {
        // 实现库存统计逻辑 - 这里需要实际实现
        return ApiResponse.success(List.of());
    }

    @GetMapping("/inventory/{componentTypeId}")
    @Operation(summary = "查询具体型号的当前库存")
    public ApiResponse<Map<String, Object>> getInventoryByType(@PathVariable Long componentTypeId) {
        int currentInventory = inventoryService.getCurrentInventory(componentTypeId);
        Map<String, Object> result = new HashMap<>();
        result.put("componentTypeId", componentTypeId);
        result.put("currentInventory", currentInventory);
        return ApiResponse.success(result);
    }

    @GetMapping("/inventory/model")
    @Operation(summary = "根据型号查询库存")
    public ApiResponse<Map<String, Object>> getInventoryByModel(@RequestParam String model) {
        try {
            var componentType = componentApplicationService.getComponentTypeByModel(model);
            if (componentType == null) {
                return ApiResponse.error("型号不存在");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("model", model);
            result.put("componentTypeId", componentType.getId());
            result.put("currentInventory", componentType.getStockQuantity() != null ? componentType.getStockQuantity() : 0);
            result.put("status", componentType.getStatus() != null ? componentType.getStatus() : "UNKNOWN");
            result.put("supplierId", componentType.getSupplierId());
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
}