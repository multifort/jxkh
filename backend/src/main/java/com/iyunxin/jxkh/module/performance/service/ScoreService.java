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
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评分服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final PerformancePlanRepository planRepository;
    private final IndicatorInstanceRepository indicatorInstanceRepository;

    /**
     * 提交评分
     */
    @Transactional
    public Long submitScore(ScoreSubmitRequest request, Long evaluatorId) {
        // 1. 验证绩效计划是否存在且处于评估阶段
        PerformancePlan plan = planRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "绩效计划不存在"));

        if (plan.getStatus() != PlanStatus.PENDING_EVAL && plan.getStatus() != PlanStatus.EVALUATED) {
            throw new BusinessException("PLAN_STATUS_INVALID", "当前计划状态不允许评分");
        }

        // 2. 验证指标实例是否存在
        IndicatorInstance indicator = indicatorInstanceRepository.findByIdAndIsDeletedFalse(request.getIndicatorInstanceId())
                .orElseThrow(() -> new BusinessException("INDICATOR_NOT_FOUND", "指标实例不存在"));

        if (!indicator.getPlanId().equals(request.getPlanId())) {
            throw new BusinessException("INDICATOR_PLAN_MISMATCH", "指标不属于该计划");
        }

        // 3. 检查是否已存在评分（防止重复评分）
        scoreRepository.findByPlanIdAndIndicatorInstanceIdAndEvaluatorIdAndTypeAndIsDeletedFalse(
                request.getPlanId(), request.getIndicatorInstanceId(), evaluatorId, request.getType())
                .ifPresent(existing -> {
                    throw new BusinessException("SCORE_ALREADY_EXISTS", "您已经对该指标进行过" + 
                            (request.getType() == ScoreType.SELF ? "自评" : "上级评") + "，不能重复评分");
                });

        // 4. 验证分数范围（0-100）
        if (request.getScore().compareTo(BigDecimal.ZERO) < 0 || request.getScore().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("SCORE_OUT_OF_RANGE", "分数必须在0-100之间");
        }

        // 5. 创建评分记录
        Score score = new Score();
        score.setPlanId(request.getPlanId());
        score.setIndicatorInstanceId(request.getIndicatorInstanceId());
        score.setEvaluatorId(evaluatorId);
        score.setScore(request.getScore());
        score.setComment(request.getComment());
        score.setType(request.getType());
        score.setStatus(ScoreStatus.SUBMITTED);
        score.setCreatedBy(evaluatorId);
        score.setUpdatedBy(evaluatorId);

        Score savedScore = scoreRepository.save(score);
        log.info("评分提交成功: scoreId={}, planId={}, evaluatorId={}, type={}, score={}", 
                savedScore.getId(), request.getPlanId(), evaluatorId, request.getType(), request.getScore());

        return savedScore.getId();
    }

    /**
     * 查询待评分列表
     */
    public List<Long> getPendingPlans(Long evaluatorId, ScoreType type) {
        return scoreRepository.findDistinctPlanIdsByEvaluatorIdAndType(evaluatorId, type);
    }

    /**
     * 查询计划的评分详情
     */
    public List<Score> getScoresByPlanId(Long planId) {
        return scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(planId);
    }

    /**
     * 统计计划的评分进度
     */
    public ScoreProgressDTO getScoreProgress(Long planId) {
        PerformancePlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "绩效计划不存在"));

        List<IndicatorInstance> indicators = indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(planId);
        int totalIndicators = indicators.size();

        long selfScoreCount = scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(planId, ScoreType.SELF);
        long managerScoreCount = scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(planId, ScoreType.MANAGER);

        ScoreProgressDTO progress = new ScoreProgressDTO();
        progress.setPlanId(planId);
        progress.setTotalIndicators(totalIndicators);
        progress.setSelfScoredCount((int) selfScoreCount);
        progress.setManagerScoredCount((int) managerScoreCount);
        progress.setSelfCompleted(selfScoreCount >= totalIndicators);
        progress.setManagerCompleted(managerScoreCount >= totalIndicators);
        progress.setAllCompleted(selfScoreCount >= totalIndicators && managerScoreCount >= totalIndicators);

        return progress;
    }

    /**
     * 批量查询多个计划的评分进度
     */
    public java.util.Map<Long, ScoreProgressDTO> getScoreProgressBatch(java.util.List<Long> planIds) {
        java.util.Map<Long, ScoreProgressDTO> progressMap = new java.util.HashMap<>();
        
        for (Long planId : planIds) {
            try {
                ScoreProgressDTO progress = getScoreProgress(planId);
                progressMap.put(planId, progress);
            } catch (Exception e) {
                log.warn("查询计划 {} 的评分进度失败: {}", planId, e.getMessage());
            }
        }
        
        return progressMap;
    }

    /**
     * 评分提交请求 DTO
     */
    public static class ScoreSubmitRequest {
        private Long planId;
        private Long indicatorInstanceId;
        private BigDecimal score;
        private String comment;
        private ScoreType type;

        // Getters and Setters
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        public Long getIndicatorInstanceId() { return indicatorInstanceId; }
        public void setIndicatorInstanceId(Long indicatorInstanceId) { this.indicatorInstanceId = indicatorInstanceId; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public ScoreType getType() { return type; }
        public void setType(ScoreType type) { this.type = type; }
    }

    /**
     * 评分进度 DTO
     */
    public static class ScoreProgressDTO {
        private Long planId;
        private int totalIndicators;
        private int selfScoredCount;
        private int managerScoredCount;
        private boolean selfCompleted;
        private boolean managerCompleted;
        private boolean allCompleted;

        // Getters and Setters
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        public int getTotalIndicators() { return totalIndicators; }
        public void setTotalIndicators(int totalIndicators) { this.totalIndicators = totalIndicators; }
        public int getSelfScoredCount() { return selfScoredCount; }
        public void setSelfScoredCount(int selfScoredCount) { this.selfScoredCount = selfScoredCount; }
        public int getManagerScoredCount() { return managerScoredCount; }
        public void setManagerScoredCount(int managerScoredCount) { this.managerScoredCount = managerScoredCount; }
        public boolean isSelfCompleted() { return selfCompleted; }
        public void setSelfCompleted(boolean selfCompleted) { this.selfCompleted = selfCompleted; }
        public boolean isManagerCompleted() { return managerCompleted; }
        public void setManagerCompleted(boolean managerCompleted) { this.managerCompleted = managerCompleted; }
        public boolean isAllCompleted() { return allCompleted; }
        public void setAllCompleted(boolean allCompleted) { this.allCompleted = allCompleted; }
    }
}
