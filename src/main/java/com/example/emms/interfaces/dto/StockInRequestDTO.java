package com.example.emms.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockInRequestDTO {
    private Long componentTypeId;
    private Integer quantity;
    private LocalDateTime purchaseDate;
    private String purchaser;
}