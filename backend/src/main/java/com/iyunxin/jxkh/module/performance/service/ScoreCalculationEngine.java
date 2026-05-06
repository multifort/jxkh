package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.performance.domain.*;
import com.iyunxin.jxkh.module.performance.repository.IndicatorInstanceRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import com.iyunxin.jxkh.module.performance.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分数计算引擎
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculationEngine {

    private final ScoreRepository scoreRepository;
    private final PerformancePlanRepository planRepository;
    private final IndicatorInstanceRepository indicatorInstanceRepository;

    /**
     * 自评权重（默认30%）
     */
    private static final BigDecimal SELF_WEIGHT = new BigDecimal("0.3");

    /**
     * 上级评权重（默认70%）
     */
    private static final BigDecimal MANAGER_WEIGHT = new BigDecimal("0.7");

    /**
     * 计算计划的最终得分和等级
     */
    @Transactional
    public void calculatePlanScore(Long planId) {
        log.info("开始计算计划 {} 的分数", planId);

        // 1. 验证计划是否存在
        PerformancePlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "绩效计划不存在"));

        // 2. 获取所有指标实例
        List<IndicatorInstance> indicators = indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(planId);
        if (indicators.isEmpty()) {
            throw new BusinessException("NO_INDICATORS", "该计划没有指标实例");
        }

        // 3. 检查是否所有指标都已完成评分
        long selfScoreCount = scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(planId, ScoreType.SELF);
        long managerScoreCount = scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(planId, ScoreType.MANAGER);
        int totalIndicators = indicators.size();

        if (selfScoreCount < totalIndicators || managerScoreCount < totalIndicators) {
            throw new BusinessException("SCORE_INCOMPLETE", 
                    String.format("评分未完成：自评 %d/%d，上级评 %d/%d", 
                            selfScoreCount, totalIndicators, managerScoreCount, totalIndicators));
        }

        // 4. 获取所有评分
        List<Score> scores = scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(planId);

        // 5. 按指标分组
        Map<Long, List<Score>> scoresByIndicator = scores.stream()
                .collect(Collectors.groupingBy(Score::getIndicatorInstanceId));

        // 6. 计算每个指标的加权分数
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (IndicatorInstance indicator : indicators) {
            List<Score> indicatorScores = scoresByIndicator.get(indicator.getId());
            if (indicatorScores == null || indicatorScores.isEmpty()) {
                continue;
            }

            // 找到自评和上级评
            Score selfScore = indicatorScores.stream()
                    .filter(s -> s.getType() == ScoreType.SELF)
                    .findFirst()
                    .orElse(null);

            Score managerScore = indicatorScores.stream()
                    .filter(s -> s.getType() == ScoreType.MANAGER)
                    .findFirst()
                    .orElse(null);

            if (selfScore == null || managerScore == null) {
                continue;
            }

            // 计算加权分数：自评分 * 30% + 上级评分 * 70%
            BigDecimal weightedScore = selfScore.getScore().multiply(SELF_WEIGHT)
                    .add(managerScore.getScore().multiply(MANAGER_WEIGHT));

            // 乘以权重
            BigDecimal indicatorFinalScore = weightedScore.multiply(indicator.getWeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));

            totalWeightedScore = totalWeightedScore.add(indicatorFinalScore);
            totalWeight = totalWeight.add(indicator.getWeight());
        }

        // 7. 计算最终得分（总分 / 总权重 * 100）
        BigDecimal finalScore;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            finalScore = totalWeightedScore.divide(totalWeight.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);
        } else {
            finalScore = BigDecimal.ZERO;
        }

        // 8. 判定绩效等级
        PerformanceLevel level = determinePerformanceLevel(finalScore);

        // 9. 更新计划
        plan.setFinalScore(finalScore);
        plan.setPerformanceLevel(level);
        plan.setCalculatedAt(LocalDateTime.now());
        plan.setStatus(PlanStatus.EVALUATED);
        planRepository.save(plan);

        log.info("计划 {} 分数计算完成: finalScore={}, level={}", planId, finalScore, level);
    }

    /**
     * 根据分数判定绩效等级
     */
    private PerformanceLevel determinePerformanceLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("90")) >= 0) {
            return PerformanceLevel.A;
        } else if (score.compareTo(new BigDecimal("80")) >= 0) {
            return PerformanceLevel.B;
        } else if (score.compareTo(new BigDecimal("70")) >= 0) {
            return PerformanceLevel.C;
        } else {
            return PerformanceLevel.D;
        }
    }
}
