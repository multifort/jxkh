package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.performance.domain.Indicator;
import com.iyunxin.jxkh.module.performance.service.IndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 指标控制器
 */
@Tag(name = "指标管理", description = "指标的增删改查接口")
@RestController
@RequestMapping("/api/v1/indicators")
@RequiredArgsConstructor
public class IndicatorController {
    
    private final IndicatorService indicatorService;
    
    @Operation(summary = "分页查询指标列表")
    @GetMapping
    public ApiResponse<Page<Indicator>> listIndicators(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "类型") @RequestParam(required = false) String type,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Indicator> indicators = indicatorService.listIndicators(keyword, categoryId, type, status, pageable);
        return ApiResponse.success(indicators);
    }
    
    @Operation(summary = "根据ID获取指标")
    @GetMapping("/{id}")
    public ApiResponse<Indicator> getIndicator(
            @Parameter(description = "指标ID") @PathVariable Long id) {
        
        Indicator indicator = indicatorService.getIndicator(id);
        return ApiResponse.success(indicator);
    }
    
    @Operation(summary = "创建指标")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Indicator> createIndicator(
            @RequestBody Indicator indicator) {
        
        Indicator created = indicatorService.createIndicator(indicator);
        return ApiResponse.success(created);
    }
    
    @Operation(summary = "更新指标")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Indicator> updateIndicator(
            @Parameter(description = "指标ID") @PathVariable Long id,
            @RequestBody Indicator indicator) {
        
        Indicator updated = indicatorService.updateIndicator(id, indicator);
        return ApiResponse.success(updated);
    }
    
    @Operation(summary = "删除指标")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> deleteIndicator(
            @Parameter(description = "指标ID") @PathVariable Long id) {
        
        indicatorService.deleteIndicator(id);
        return ApiResponse.success(null);
    }
    
    @Operation(summary = "启用/禁用指标")
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Indicator> toggleStatus(
            @Parameter(description = "指标ID") @PathVariable Long id) {
        
        Indicator indicator = indicatorService.toggleStatus(id);
        return ApiResponse.success(indicator);
    }
}
