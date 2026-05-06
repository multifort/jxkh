package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.performance.service.RiskScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器 - 用于手动触发定时任务
 * 注意：此接口仅用于开发和测试环境，生产环境禁用
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Tag(name = "测试接口", description = "仅用于开发和测试环境")
@Profile({"dev", "test"}) // 仅在开发和测试环境启用
public class TestController {
    
    private final RiskScheduleService riskScheduleService;
    
    /**
     * 手动触发风险检测任务
     * 仅限管理员角色访问
     */
    @PostMapping("/trigger-risk-detection")
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
    @Operation(summary = "触发风险检测", description = "手动执行每日风险检测任务（仅开发和测试环境，需要管理员权限）")
    public ApiResponse<String> triggerRiskDetection() {
        log.info("管理员手动触发风险检测任务");
        riskScheduleService.dailyRiskDetection();
        return ApiResponse.success("风险检测任务已执行，请查看日志和通知");
    }
}
