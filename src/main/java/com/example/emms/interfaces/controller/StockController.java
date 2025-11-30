package com.example.emms.interfaces.controller;


import com.example.emms.application.service.ComponentApplicationService;
import com.example.emms.interfaces.dto.ApiResponse;
import com.example.emms.interfaces.dto.StockInRequestDTO;
import com.example.emms.interfaces.dto.StockOutRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Tag(name = "库存管理")
public class StockController {

    private final ComponentApplicationService componentApplicationService;

    @PostMapping("/in")
    @Operation(summary = "元器件入库")
    public ApiResponse<String> stockIn(@RequestBody StockInRequestDTO request) {
        try {
            componentApplicationService.addComponentToStock(
                request.getComponentTypeId(),
                request.getQuantity(),
                request.getPurchaseDate(),  // 采购日期
                request.getPurchaser()      // 采购人
            );
            return ApiResponse.success("入库成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/out")
    @Operation(summary = "取用元器件")
    public ApiResponse<String> stockOut(@RequestBody StockOutRequestDTO request) {
        try {
            componentApplicationService.takeComponent(
                request.getModel(),
                request.getQuantity(),
                request.getTakenBy()
            );
            return ApiResponse.success("取用成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/quantity/{model}")
    @Operation(summary = "查询元器件库存")
    public ApiResponse<Integer> getStockQuantity(@PathVariable String model) {
        try {
            Integer quantity = componentApplicationService.getStockByModel(model);
            return ApiResponse.success(quantity);
        } catch (Exception e) {
            return ApiResponse.error("查询失败");
        }
    }
}