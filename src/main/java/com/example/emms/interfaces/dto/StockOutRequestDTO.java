package com.example.emms.interfaces.dto;

import lombok.Data;

@Data
public class StockOutRequestDTO {
    private String model;
    private Integer quantity;
    private String takenBy;
}