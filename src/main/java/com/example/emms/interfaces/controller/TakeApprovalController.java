package com.example.emms.interfaces.controller;

import com.example.emms.application.service.ComponentApplicationService;
import com.example.emms.domain.model.StockOutRecord;
import com.example.emms.domain.repository.StockOutRecordRepository;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/take-approvals")
@RequiredArgsConstructor
@Tag(name = "领用审核管理")
public class TakeApprovalController {

    private final ComponentApplicationService componentApplicationService;
    private final StockOutRecordRepository stockOutRecordRepository;

    // 获取待审核的领用记录
    @GetMapping("/pending")
    @Operation(summary = "获取待审核领用记录")
    public ApiResponse<List<StockOutRecord>> getPendingTakeRecords() {
        List<StockOutRecord> pending = componentApplicationService.getPendingTakeRecords();
        return ApiResponse.success(pending);
    }

    // 审核通过领用申请
    @PutMapping("/{recordId}/approve")
    @Operation(summary = "审核通过领用")
    public ApiResponse<String> approveTakeRecord(@PathVariable Long recordId,
                                                @RequestParam String approvedBy) {
        try {
            componentApplicationService.approveTakeRecord(recordId, approvedBy);
            return ApiResponse.success("审核通过");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 审核拒绝领用申请
    @PutMapping("/{recordId}/reject")
    @Operation(summary = "审核拒绝领用")
    public ApiResponse<String> rejectTakeRecord(@PathVariable Long recordId,
                                               @RequestParam String approvedBy) {
        try {
            componentApplicationService.rejectTakeRecord(recordId, approvedBy);
            return ApiResponse.success("审核拒绝");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 获取所有领用记录
    @GetMapping
    @Operation(summary = "获取所有领用记录")
    public ApiResponse<List<StockOutRecord>> getAllTakeRecords() {
        return ApiResponse.success(stockOutRecordRepository.findAll());
    }
}