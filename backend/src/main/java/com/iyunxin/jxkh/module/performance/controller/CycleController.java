package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.performance.domain.CycleStatus;
import com.iyunxin.jxkh.module.performance.domain.PerformanceCycle;
import com.iyunxin.jxkh.module.performance.service.CycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 绩效周期管理控制器
 */
@Tag(name = "绩效周期管理", description = "绩效周期的CRUD操作和状态管理")
@RestController
@RequestMapping("/api/v1/cycles")
@RequiredArgsConstructor
public class CycleController {

    private final CycleService cycleService;

    @Operation(summary = "分页查询周期列表")
    @GetMapping
    public ApiResponse<Page<PerformanceCycle>> getCycles(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选") @RequestParam(required = false) CycleStatus status,
            @Parameter(description = "组织ID") @RequestParam(required = false) Long orgId) {
        
        Page<PerformanceCycle> cycles = cycleService.getCycles(page, size, keyword, status, orgId);
        return ApiResponse.success(cycles);
    }

    @Operation(summary = "根据ID查询周期详情")
    @GetMapping("/{id}")
    public ApiResponse<PerformanceCycle> getCycleById(
            @Parameter(description = "周期ID") @PathVariable Long id) {
        
        PerformanceCycle cycle = cycleService.getCycleById(id);
        return ApiResponse.success(cycle);
    }

    @Operation(summary = "创建周期")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PerformanceCycle> createCycle(
            @RequestBody PerformanceCycle cycle) {
        
        PerformanceCycle created = cycleService.createCycle(cycle);
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新周期")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PerformanceCycle> updateCycle(
            @Parameter(description = "周期ID") @PathVariable Long id,
            @RequestBody PerformanceCycle cycle) {
        
        PerformanceCycle updated = cycleService.updateCycle(id, cycle);
        return ApiResponse.success(updated);
    }

    @Operation(summary = "删除周期")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> deleteCycle(
            @Parameter(description = "周期ID") @PathVariable Long id) {
        
        cycleService.deleteCycle(id);
        return ApiResponse.success();
    }

    @Operation(summary = "启动周期")
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PerformanceCycle> startCycle(
            @Parameter(description = "周期ID") @PathVariable Long id) {
        
        PerformanceCycle cycle = cycleService.startCycle(id);
        return ApiResponse.success(cycle);
    }

    @Operation(summary = "结束周期")
    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<PerformanceCycle> endCycle(
            @Parameter(description = "周期ID") @PathVariable Long id) {
        
        PerformanceCycle cycle = cycleService.endCycle(id);
        return ApiResponse.success(cycle);
    }
}
