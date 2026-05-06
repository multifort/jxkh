package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.common.util.SecurityUtils;
import com.iyunxin.jxkh.module.performance.domain.PerformanceRecord;
import com.iyunxin.jxkh.module.performance.domain.RecordType;
import com.iyunxin.jxkh.module.performance.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 绩效记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@Tag(name = "绩效记录管理", description = "绩效记录的增删改查")
public class RecordController {
    
    private final RecordService recordService;
    
    /**
     * 创建记录
     */
    @PostMapping
    @Operation(summary = "创建绩效记录", description = "创建周报、月报、里程碑或成果记录")
    public ApiResponse<Long> createRecord(
            @Parameter(description = "记录类型") @RequestParam RecordType type,
            @Parameter(description = "计划ID") @RequestParam Long planId,
            @Parameter(description = "记录内容") @RequestParam String content,
            @Parameter(description = "进度（0-100）") @RequestParam(required = false) Double progress,
            @Parameter(description = "记录日期") @RequestParam(required = false) String recordDate,
            @Parameter(description = "是否启用AI总结") @RequestParam(defaultValue = "false") boolean enableAiSummary) {
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        RecordService.RecordCreateRequest request = new RecordService.RecordCreateRequest();
        request.setPlanId(planId);
        request.setType(type);
        request.setContent(content);
        request.setProgress(progress != null ? java.math.BigDecimal.valueOf(progress) : null);
        request.setRecordDate(recordDate != null ? java.time.LocalDate.parse(recordDate) : null);
        request.setEnableAiSummary(enableAiSummary);
        
        Long recordId = recordService.createRecord(request, currentUserId);
        
        return ApiResponse.success(recordId);
    }
    
    /**
     * 查询计划的记录列表
     */
    @GetMapping
    @Operation(summary = "查询记录列表", description = "分页查询指定计划的记录列表")
    public ApiResponse<Page<PerformanceRecord>> getRecords(
            @Parameter(description = "计划ID") @RequestParam Long planId,
            @Parameter(description = "记录类型") @RequestParam(required = false) RecordType type,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordDate"));
        Page<PerformanceRecord> records = recordService.getRecordsByPlan(planId, type, pageable);
        
        return ApiResponse.success(records);
    }
    
    /**
     * 查询记录详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询记录详情", description = "根据ID查询记录详情")
    public ApiResponse<PerformanceRecord> getRecord(@PathVariable Long id) {
        PerformanceRecord record = recordService.getRecordById(id);
        return ApiResponse.success(record);
    }
    
    /**
     * 更新记录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新记录", description = "更新记录内容、进度等信息")
    public ApiResponse<Void> updateRecord(
            @PathVariable Long id,
            @Parameter(description = "记录内容") @RequestParam(required = false) String content,
            @Parameter(description = "进度（0-100）") @RequestParam(required = false) Double progress,
            @Parameter(description = "记录日期") @RequestParam(required = false) String recordDate) {
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        RecordService.RecordUpdateRequest request = new RecordService.RecordUpdateRequest();
        request.setContent(content);
        request.setProgress(progress != null ? java.math.BigDecimal.valueOf(progress) : null);
        request.setRecordDate(recordDate != null ? java.time.LocalDate.parse(recordDate) : null);
        
        recordService.updateRecord(id, request, currentUserId);
        
        return ApiResponse.success();
    }
    
    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除记录", description = "逻辑删除记录")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        recordService.deleteRecord(id, currentUserId);
        return ApiResponse.success();
    }
    
    /**
     * 删除附件
     */
    @DeleteMapping("/{recordId}/attachments")
    @Operation(summary = "删除附件", description = "删除记录的指定附件")
    public ApiResponse<Void> deleteAttachment(
            @PathVariable Long recordId,
            @Parameter(description = "文件URL") @RequestParam String fileUrl) {
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        recordService.deleteAttachment(recordId, fileUrl, currentUserId);
        
        return ApiResponse.success();
    }
    
    /**
     * 查询计划的风险预警指标
     */
    @GetMapping("/risks")
    @Operation(summary = "查询风险预警", description = "查询指定计划的延期风险指标")
    public ApiResponse<List<com.iyunxin.jxkh.module.performance.domain.IndicatorInstance>> getRiskIndicators(
            @Parameter(description = "计划ID") @RequestParam Long planId) {
        
        List<com.iyunxin.jxkh.module.performance.domain.IndicatorInstance> riskIndicators = 
                recordService.getRiskIndicators(planId);
        
        return ApiResponse.success(riskIndicators);
    }
}
