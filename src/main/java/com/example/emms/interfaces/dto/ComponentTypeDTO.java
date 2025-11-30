package com.example.emms.interfaces.dto;

import lombok.Data;

@Data
public class ComponentTypeDTO {
    private Long id;
    private String name;
    private String model;
    private Long supplierId;
    private String supplierName;
    private Double unitPrice;
    private String image;
    private Integer stockQuantity;
    private String status;
    private String parameters; // 直接映射 Specification 的 parameters

    // 完整的图片URL
    public String getImageUrl() {
        if (image != null && !image.startsWith("http")) {
            return "/api/files/" + image;
        }
        return image;
    }
}
