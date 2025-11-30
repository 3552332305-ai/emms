package com.example.emms.domain.service;

import com.example.emms.domain.model.ComponentType;
import com.example.emms.domain.model.StockOutRecord;
import com.example.emms.domain.repository.ComponentTypeRepository;
import com.example.emms.domain.repository.StockOutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComponentTakingService {

    private final ComponentTypeRepository componentTypeRepository;
    private final StockOutRecordRepository stockOutRecordRepository;

    @Transactional
    public void takeComponent(String model, int quantity, String takenBy) {
        // 根据型号获取元器件类型（优先返回已审核通过的）
        List<ComponentType> types = componentTypeRepository.findAllByModel(model);
        if (types.isEmpty()) {
            throw new IllegalArgumentException("元器件型号不存在");
        }
        ComponentType componentType = types.get(0); // 已按状态排序，APPROVED 优先

        // 检查库存是否充足
        if (!componentType.isStockSufficient(quantity)) {
            throw new IllegalStateException("库存不足，当前库存: " + componentType.getStockQuantity());
        }

        // 创建待审核的取用记录（不立即减少库存）
        StockOutRecord record = new StockOutRecord();
        record.setComponentType(componentType);
        record.setQuantity(quantity);
        record.setTakenBy(takenBy);
        record.setStatus("PENDING"); // 等待审核

        stockOutRecordRepository.save(record);
    }

    // 审核通过取用申请
    @Transactional
    public void approveTakeRecord(Long recordId, String approvedBy) {
        StockOutRecord record = stockOutRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("取用记录不存在"));

        if (!"PENDING".equals(record.getStatus())) {
            throw new IllegalStateException("记录状态不是待审核");
        }

        ComponentType componentType = record.getComponentType();
        if (!componentType.isStockSufficient(record.getQuantity())) {
            throw new IllegalStateException("库存不足，无法批准");
        }

        // 减少库存
        componentType.decreaseStock(record.getQuantity());
        componentTypeRepository.save(componentType);

        // 更新记录状态
        record.setStatus("APPROVED");
        record.setApprovedBy(approvedBy);
        record.setApprovedAt(LocalDateTime.now());

        stockOutRecordRepository.save(record);
    }

    // 拒绝取用申请
    @Transactional
    public void rejectTakeRecord(Long recordId, String approvedBy) {
        StockOutRecord record = stockOutRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("取用记录不存在"));

        if (!"PENDING".equals(record.getStatus())) {
            throw new IllegalStateException("记录状态不是待审核");
        }

        // 更新记录状态
        record.setStatus("REJECTED");
        record.setApprovedBy(approvedBy);
        record.setApprovedAt(LocalDateTime.now());

        stockOutRecordRepository.save(record);
    }
}
