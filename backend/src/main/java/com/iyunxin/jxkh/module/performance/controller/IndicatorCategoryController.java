package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.performance.domain.IndicatorCategory;
import com.iyunxin.jxkh.module.performance.service.IndicatorCategoryService;
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
 * 指标分类控制器
 */
@Tag(name = "指标分类管理", description = "指标分类的增删改查接口")
@RestController
@RequestMapping("/api/v1/indicator-categories")
@RequiredArgsConstructor
public class IndicatorCategoryController {
    
    private final IndicatorCategoryService categoryService;
    
    @Operation(summary = "分页查询分类列表")
    @GetMapping
    public ApiResponse<Page<IndicatorCategory>> listCategories(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IndicatorCategory> categories = categoryService.listCategories(keyword, parentId, pageable);
        return ApiResponse.success(categories);
    }
    
    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public ApiResponse<List<IndicatorCategory>> getCategoryTree() {
        List<IndicatorCategory> tree = categoryService.getCategoryTree();
        return ApiResponse.success(tree);
    }
    
    @Operation(summary = "根据ID获取分类")
    @GetMapping("/{id}")
    public ApiResponse<IndicatorCategory> getCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        
        IndicatorCategory category = categoryService.getCategory(id);
        return ApiResponse.success(category);
    }
    
    @Operation(summary = "创建分类")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<IndicatorCategory> createCategory(
            @RequestBody IndicatorCategory category) {
        
        IndicatorCategory created = categoryService.createCategory(category);
        return ApiResponse.success(created);
    }
    
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<IndicatorCategory> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @RequestBody IndicatorCategory category) {
        
        IndicatorCategory updated = categoryService.updateCategory(id, category);
        return ApiResponse.success(updated);
    }
    
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        
        categoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }
}
