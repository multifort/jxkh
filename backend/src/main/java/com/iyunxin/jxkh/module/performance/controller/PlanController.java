package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.performance.domain.PerformancePlan;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import com.iyunxin.jxkh.module.performance.dto.PlanDetailDTO;
import com.iyunxin.jxkh.module.performance.dto.PlanListDTO;
import com.iyunxin.jxkh.module.performance.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 绩效计划管理控制器
 */
@Tag(name = "绩效计划管理", description = "绩效计划的创建、查询和更新")
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @Operation(summary = "创建绩效计划")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ApiResponse<Long> createPlan(
            @RequestBody PlanService.PlanCreateRequest request) {
        
        Long planId = planService.createPlan(request);
        return ApiResponse.success(planId);
    }

    @Operation(summary = "根据ID查询计划详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ApiResponse<PlanDetailDTO> getPlanById(
            @Parameter(description = "计划ID") @PathVariable Long id) {
        
        PlanDetailDTO plan = planService.getPlanById(id);
        return ApiResponse.success(plan);
    }

    @Operation(summary = "分页查询计划列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ApiResponse<Page<PlanListDTO>> listPlans(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "周期ID") @RequestParam(required = false) Long cycleId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) PlanStatus status) {
        
        Page<PlanListDTO> plans = planService.listPlans(page, size, cycleId, status);
        return ApiResponse.success(plans);
    }

    @Operation(summary = "更新计划草稿")
    @PutMapping("/{id}/draft")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ApiResponse<Void> updatePlanDraft(
            @Parameter(description = "计划ID") @PathVariable Long id,
            @RequestBody PlanService.PlanUpdateRequest request) {
        
        planService.updatePlanDraft(id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "提交计划审批")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ApiResponse<Void> submitPlan(
            @Parameter(description = "计划ID") @PathVariable Long id) {
        
        planService.submitPlan(id);
        return ApiResponse.success();
    }

    @Operation(summary = "审批计划")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ApiResponse<Void> approvePlan(
            @Parameter(description = "计划ID") @PathVariable Long id,
            @Parameter(description = "是否通过") @RequestParam Boolean approved,
            @Parameter(description = "审批意见") @RequestParam(required = false) String comment) {
        
        planService.approvePlan(id, approved, comment);
        return ApiResponse.success();
    }
}
