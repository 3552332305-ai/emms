package com.example.emms.domain.service;

import com.example.emms.domain.model.ComponentType;
import com.example.emms.domain.model.StockInRecord;
import com.example.emms.domain.repository.ComponentTypeRepository;
import com.example.emms.domain.repository.StockInRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ComponentStockInService {

    private final ComponentTypeRepository componentTypeRepository;
    private final StockInRecordRepository stockInRecordRepository;

    @Transactional
    public void addComponentToStock(Long componentTypeId, int quantity, LocalDateTime purchaseDate, String purchaser) {
        ComponentType componentType = componentTypeRepository.findById(componentTypeId)
                .orElseThrow(() -> new IllegalArgumentException("元器件类型不存在"));

        // 增加库存
        componentType.increaseStock(quantity);
        componentTypeRepository.save(componentType);

        // 创建入库记录
        StockInRecord record = new StockInRecord();
        record.setComponentType(componentType);
        record.setQuantity(quantity);
        record.setPurchaseDate(purchaseDate);
        record.setPurchaser(purchaser);

        stockInRecordRepository.save(record);
    }
}
