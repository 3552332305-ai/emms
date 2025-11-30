package com.example.emms.application.service;

import com.example.emms.domain.model.*;
import com.example.emms.domain.repository.*;
import com.example.emms.domain.service.ComponentStockInService;
import com.example.emms.domain.service.ComponentTakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComponentApplicationService {

    private final ComponentTypeRepository componentTypeRepository;
    private final ComponentStockInService componentStockInService;
    private final ComponentTakingService componentTakingService;
     private final StockOutRecordRepository stockOutRecordRepository;

    @Transactional
    public void addComponentToStock(Long componentTypeId, Integer quantity, LocalDateTime purchaseDate, String purchaser) {
        componentStockInService.addComponentToStock(componentTypeId, quantity, purchaseDate, purchaser);
    }

    @Transactional
    public void takeComponent(String model, Integer quantity, String takenBy) {
        componentTakingService.takeComponent(model, quantity, takenBy);
    }

    public List<ComponentType> getAllComponentTypes() {
        return componentTypeRepository.findAll();
    }

    // 获取库存充足的元器件类型
    public List<ComponentType> getAvailableComponentTypes() {
        return componentTypeRepository.findByStatus("APPROVED");
    }

    // 根据型号查询库存 - 添加这个缺失的方法
    public Integer getStockByModel(String model) {
        ComponentType type = getComponentTypeByModel(model);
        return type != null ? (type.getStockQuantity() != null ? type.getStockQuantity() : 0) : 0;
    }

    // 添加其他有用的方法
    public ComponentType getComponentTypeByModel(String model) {
        // 如果存在多个相同型号，优先返回已审核通过的
        List<ComponentType> types = componentTypeRepository.findAllByModel(model);
        
        if (types.isEmpty()) {
            return null;
        }
        
        // 返回第一个（已按状态排序，APPROVED 优先）
        return types.get(0);
    }

    @Transactional
    public void approveTakeRecord(Long recordId, String approvedBy) {
    componentTakingService.approveTakeRecord(recordId, approvedBy);
    }

    @Transactional
    public void rejectTakeRecord(Long recordId, String approvedBy) {
        componentTakingService.rejectTakeRecord(recordId, approvedBy);
    }

    // 获取待审核的领用记录
    public List<StockOutRecord> getPendingTakeRecords() {
        return stockOutRecordRepository.findByStatus("PENDING");
    }
}