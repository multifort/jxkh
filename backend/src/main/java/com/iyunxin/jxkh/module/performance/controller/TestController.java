package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.performance.service.RiskScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器 - 用于手动触发定时任务
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Tag(name = "测试接口", description = "仅用于开发和测试")
public class TestController {
    
    private final RiskScheduleService riskScheduleService;
    
    /**
     * 手动触发风险检测任务
     */
    @PostMapping("/trigger-risk-detection")
    @Operation(summary = "触发风险检测", description = "手动执行每日风险检测任务（测试用）")
    public ApiResponse<String> triggerRiskDetection() {
        log.info("手动触发风险检测任务");
        riskScheduleService.dailyRiskDetection();
        return ApiResponse.success("风险检测任务已执行，请查看日志和通知");
    }
}
