package com.example.emms.interfaces.controller;

import com.example.emms.domain.model.StockOutRecord;
import com.example.emms.domain.repository.StockOutRecordRepository;
import com.example.emms.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "使用历史查询")
public class UsageHistoryController {

    private final StockOutRecordRepository stockOutRecordRepository;

    @GetMapping("/usage")
    @Operation(summary = "查询元器件的取用记录")
    public ApiResponse<List<StockOutRecord>> getUsageHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<StockOutRecord> records = stockOutRecordRepository.findAll(PageRequest.of(page, size));
        return ApiResponse.success(records.getContent());
    }

    @GetMapping("/usage/search")
    @Operation(summary = "按取用人查询记录")
    public ApiResponse<List<StockOutRecord>> getUsageHistoryByUser(@RequestParam String takenBy) {
        List<StockOutRecord> records = stockOutRecordRepository.findByTakenBy(takenBy);
        return ApiResponse.success(records);
    }
}
