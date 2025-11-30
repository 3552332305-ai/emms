package com.example.emms.interfaces.dto;

import lombok.Data;

@Data
public class InventoryStatisticsDTO {
    private String componentType;
    private String model;
    private Integer totalInventory;
    private Integer usedCount;
    private Integer currentStock;

    // 构造函数
    public InventoryStatisticsDTO(String componentType, String model, Integer totalInventory, Integer usedCount) {
        this.componentType = componentType;
        this.model = model;
        this.totalInventory = totalInventory;
        this.usedCount = usedCount;
        this.currentStock = totalInventory - usedCount;
    }
}