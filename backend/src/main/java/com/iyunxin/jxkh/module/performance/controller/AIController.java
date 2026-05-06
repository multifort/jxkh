package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.infra.ai.AISummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI 服务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI 服务", description = "AI 智能总结、分析")
public class AIController {
    
    private final AISummaryService aiSummaryService;
    
    /**
     * 生成周报智能总结
     */
    @PostMapping("/weekly-summary")
    @Operation(summary = "生成周报智能总结", description = "使用 AI 对周报内容进行智能分析，提取关键成果、风险点和建议")
    public ApiResponse<AISummaryService.AISummaryResult> generateWeeklySummary(
            @RequestBody WeeklySummaryRequest request) {
        
        AISummaryService.AISummaryResult result = aiSummaryService.generateWeeklySummary(request.getContent());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 周报总结请求
     */
    @Data
    public static class WeeklySummaryRequest {
        @Parameter(description = "周报内容")
        private String content;
    }
}
