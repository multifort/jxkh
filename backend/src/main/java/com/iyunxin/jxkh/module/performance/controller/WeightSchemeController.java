package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.performance.domain.WeightScheme;
import com.iyunxin.jxkh.module.performance.domain.WeightSchemeItem;
import com.iyunxin.jxkh.module.performance.service.WeightSchemeService;
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

import java.util.List;

/**
 * 权重方案控制器
 */
@Tag(name = "权重方案管理", description = "权重方案的增删改查接口")
@RestController
@RequestMapping("/api/v1/weight-schemes")
@RequiredArgsConstructor
public class WeightSchemeController {
    
    private final WeightSchemeService schemeService;
    
    @Operation(summary = "分页查询方案列表")
    @GetMapping
    public ApiResponse<Page<WeightScheme>> listSchemes(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "周期ID") @RequestParam(required = false) Long cycleId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WeightScheme> schemes = schemeService.listSchemes(keyword, cycleId, status, pageable);
        return ApiResponse.success(schemes);
    }
    
    @Operation(summary = "根据ID获取方案")
    @GetMapping("/{id}")
    public ApiResponse<WeightScheme> getScheme(
            @Parameter(description = "方案ID") @PathVariable Long id) {
        
        WeightScheme scheme = schemeService.getScheme(id);
        return ApiResponse.success(scheme);
    }
    
    @Operation(summary = "获取方案明细列表")
    @GetMapping("/{id}/items")
    public ApiResponse<List<WeightSchemeItem>> getSchemeItems(
            @Parameter(description = "方案ID") @PathVariable Long id) {
        
        List<WeightSchemeItem> items = schemeService.getSchemeItems(id);
        return ApiResponse.success(items);
    }
    
    @Operation(summary = "创建方案")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<WeightScheme> createScheme(
            @RequestBody WeightScheme scheme) {
        
        WeightScheme created = schemeService.createScheme(scheme);
        return ApiResponse.success(created);
    }
    
    @Operation(summary = "更新方案基本信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<WeightScheme> updateScheme(
            @Parameter(description = "方案ID") @PathVariable Long id,
            @RequestBody WeightScheme scheme) {
        
        WeightScheme updated = schemeService.updateScheme(id, scheme);
        return ApiResponse.success(updated);
    }
    
    @Operation(summary = "保存方案明细")
    @PutMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> saveSchemeItems(
            @Parameter(description = "方案ID") @PathVariable Long id,
            @RequestBody List<WeightSchemeItem> items) {
        
        schemeService.saveSchemeItems(id, items);
        return ApiResponse.success(null);
    }
    
    @Operation(summary = "发布方案")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<WeightScheme> publishScheme(
            @Parameter(description = "方案ID") @PathVariable Long id) {
        
        WeightScheme scheme = schemeService.publishScheme(id);
        return ApiResponse.success(scheme);
    }
    
    @Operation(summary = "归档方案")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<WeightScheme> archiveScheme(
            @Parameter(description = "方案ID") @PathVariable Long id) {
        
        WeightScheme scheme = schemeService.archiveScheme(id);
        return ApiResponse.success(scheme);
    }
    
    @Operation(summary = "删除方案")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> deleteScheme(
            @Parameter(description = "方案ID") @PathVariable Long id) {
        
        schemeService.deleteScheme(id);
        return ApiResponse.success(null);
    }
    
    @Operation(summary = "复制方案")
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<WeightScheme> copyScheme(
            @Parameter(description = "方案ID") @PathVariable Long id) {
        
        WeightScheme copied = schemeService.copyScheme(id);
        return ApiResponse.success(copied);
    }
}
