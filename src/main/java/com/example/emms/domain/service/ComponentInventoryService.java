package com.example.emms.domain.service;

import com.example.emms.domain.repository.StockInRecordRepository;
import com.example.emms.domain.repository.StockOutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComponentInventoryService {

    private final StockInRecordRepository stockInRecordRepository;
    private final StockOutRecordRepository stockOutRecordRepository;

    public int getCurrentInventory(Long componentTypeId) {
        Integer totalIn = stockInRecordRepository.sumQuantityByComponentTypeId(componentTypeId);
        Integer totalOut = stockOutRecordRepository.sumQuantityByComponentTypeId(componentTypeId);

        totalIn = totalIn == null ? 0 : totalIn;
        totalOut = totalOut == null ? 0 : totalOut;

        return totalIn - totalOut;
    }
}
