package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.performance.service.ScoreCalculationEngine;
import com.iyunxin.jxkh.module.performance.service.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评分控制器
 */
@Tag(name = "评分管理", description = "绩效评分相关接口")
@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final ScoreCalculationEngine scoreCalculationEngine;

    /**
     * 提交评分
     */
    @Operation(summary = "提交评分", description = "员工自评或上级评分")
    @PostMapping
    public ApiResponse<Long> submitScore(
            @RequestBody ScoreService.ScoreSubmitRequest request,
            Authentication authentication) {
        Long evaluatorId = getCurrentUserId(authentication);
        Long scoreId = scoreService.submitScore(request, evaluatorId);
        return ApiResponse.success(scoreId);
    }

    /**
     * 查询待评分计划列表
     */
    @Operation(summary = "查询待评分计划", description = "获取需要评分的计划ID列表")
    @GetMapping("/pending")
    public ApiResponse<List<Long>> getPendingPlans(
            @Parameter(description = "评分类型：SELF-自评，MANAGER-上级评")
            @RequestParam String type,
            Authentication authentication) {
        Long evaluatorId = getCurrentUserId(authentication);
        List<Long> planIds = scoreService.getPendingPlans(evaluatorId, 
                com.iyunxin.jxkh.module.performance.domain.ScoreType.valueOf(type));
        return ApiResponse.success(planIds);
    }

    /**
     * 查询计划的评分详情
     */
    @Operation(summary = "查询评分详情", description = "获取某个计划的所有评分记录")
    @GetMapping("/plan/{planId}")
    public ApiResponse<List<com.iyunxin.jxkh.module.performance.domain.Score>> getScoresByPlan(
            @PathVariable Long planId) {
        List<com.iyunxin.jxkh.module.performance.domain.Score> scores = scoreService.getScoresByPlanId(planId);
        return ApiResponse.success(scores);
    }

    /**
     * 查询评分进度
     */
    @Operation(summary = "查询评分进度", description = "统计计划的自评和上级评完成情况")
    @GetMapping("/progress/{planId}")
    public ApiResponse<ScoreService.ScoreProgressDTO> getScoreProgress(@PathVariable Long planId) {
        ScoreService.ScoreProgressDTO progress = scoreService.getScoreProgress(planId);
        return ApiResponse.success(progress);
    }

    /**
     * 手动触发分数计算（管理员）
     */
    @Operation(summary = "计算分数", description = "手动触发计划的分数计算")
    @PostMapping("/calculate")
    public ApiResponse<Void> calculateScore(@RequestParam Long planId) {
        scoreCalculationEngine.calculatePlanScore(planId);
        return ApiResponse.success();
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        // TODO: 从 JWT Token 中解析用户ID
        // 临时返回固定值，实际应该从 SecurityContext 中获取
        return 1L;
    }
}
