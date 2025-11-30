package com.example.emms.interfaces.dto;

import lombok.Data;

@Data
public class ComponentTypeUpdateDTO {
    private String name;
    private String model;
    private Double unitPrice;
    private String image;
    private String parameters;

    // 不包含 supplier 字段，避免关联问题
}