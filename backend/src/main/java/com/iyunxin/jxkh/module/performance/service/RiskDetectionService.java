package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.performance.domain.*;
import com.iyunxin.jxkh.module.performance.repository.IndicatorInstanceRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 风险检测服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskDetectionService {
    
    private final PerformancePlanRepository planRepository;
    private final IndicatorInstanceRepository indicatorInstanceRepository;
    
    /**
     * 定时任务：每天凌晨 2 点检测风险
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledRiskDetection() {
        log.info("开始执行定时风险检测任务");
        
        try {
            // TODO: 添加 findByStatus 方法到 Repository
            // List<PerformancePlan> inProgressPlans = planRepository.findByStatus(PlanStatus.IN_PROGRESS);
            
            log.info("定时风险检测完成（当前为 Mock 模式）");
            
        } catch (Exception e) {
            log.error("定时风险检测任务执行失败", e);
        }
    }
    
    /**
     * 评估计划风险
     */
    public RiskAssessment assessPlanRisk(Long planId) {
        try {
            PerformancePlan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "计划不存在"));
            
            // TODO: 从 Cycle 中获取开始和结束日期
            // PerformanceCycle cycle = cycleRepository.findById(plan.getCycleId())
            //         .orElseThrow(() -> new BusinessException("CYCLE_NOT_FOUND", "周期不存在"));
            
            // Mock 数据
            BigDecimal expectedProgress = BigDecimal.valueOf(70);
            BigDecimal actualProgress = BigDecimal.valueOf(65);
            BigDecimal progressDelay = expectedProgress.subtract(actualProgress);
            
            return RiskAssessment.builder()
                    .planId(planId)
                    .riskLevel(RiskLevel.LOW)
                    .expectedProgress(expectedProgress)
                    .actualProgress(actualProgress)
                    .progressDelay(progressDelay)
                    .staleIndicatorCount(0)
                    .staleIndicators(List.of())
                    .build();
        } catch (Exception e) {
            log.error("风险评估失败: planId={}", planId, e);
            // 返回默认低风险
            return RiskAssessment.builder()
                    .planId(planId)
                    .riskLevel(RiskLevel.LOW)
                    .expectedProgress(BigDecimal.ZERO)
                    .actualProgress(BigDecimal.ZERO)
                    .progressDelay(BigDecimal.ZERO)
                    .staleIndicatorCount(0)
                    .staleIndicators(List.of())
                    .build();
        }
    }
    
    /**
     * 计算实际进度
     */
    private BigDecimal calculateActualProgress(List<IndicatorInstance> instances) {
        if (instances.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalProgress = instances.stream()
                .map(instance -> instance.getProgress() != null ? instance.getProgress() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalProgress.divide(BigDecimal.valueOf(instances.size()), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 查找未更新的指标（超过7天）
     */
    private List<IndicatorInstance> findStaleIndicators(List<IndicatorInstance> instances) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        
        return instances.stream()
                .filter(instance -> instance.getUpdatedAt() != null && 
                        instance.getUpdatedAt().toLocalDate().isBefore(sevenDaysAgo))
                .toList();
    }
    
    /**
     * 判断风险等级
     */
    private RiskLevel determineRiskLevel(BigDecimal progressDelay, int staleCount) {
        double delay = progressDelay.doubleValue();
        
        if (delay > 30 || staleCount >= 3) {
            return RiskLevel.HIGH;
        } else if (delay > 10 || staleCount >= 1) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }
    
    /**
     * 批量检测计划风险
     */
    public List<RiskAssessment> batchAssessRisks(List<Long> planIds) {
        return planIds.stream()
                .map(this::assessPlanRisk)
                .toList();
    }
    
    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险");
        
        private final String description;
        
        RiskLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 风险评估结果
     */
    @Data
    @lombok.Builder
    public static class RiskAssessment {
        private Long planId;
        private RiskLevel riskLevel;
        private BigDecimal expectedProgress;
        private BigDecimal actualProgress;
        private BigDecimal progressDelay;
        private int staleIndicatorCount;
        private List<IndicatorInstance> staleIndicators;
    }
}
