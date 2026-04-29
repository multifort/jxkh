package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.performance.domain.PerformancePlan;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 绩效计划状态机
 * 严格控制计划状态流转，防止非法状态变更
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanStateMachine {

    private final PerformancePlanRepository planRepository;

    /**
     * 定义合法的状态转换规则
     * Key: 当前状态
     * Value: 允许转换到的目标状态集合
     */
    private static final Map<PlanStatus, Set<PlanStatus>> VALID_TRANSITIONS = Map.of(
            // 草稿可以提交审批
            PlanStatus.DRAFT, Set.of(PlanStatus.PENDING_APPROVE),
            
            // 待审批可以通过或驳回
            PlanStatus.PENDING_APPROVE, Set.of(PlanStatus.IN_PROGRESS, PlanStatus.DRAFT),
            
            // 执行中可以进入评估阶段
            PlanStatus.IN_PROGRESS, Set.of(PlanStatus.PENDING_EVAL),
            
            // 待评估可以完成评估
            PlanStatus.PENDING_EVAL, Set.of(PlanStatus.EVALUATED),
            
            // 已评估可以校准
            PlanStatus.EVALUATED, Set.of(PlanStatus.CALIBRATED),
            
            // 已校准可以归档
            PlanStatus.CALIBRATED, Set.of(PlanStatus.ARCHIVED)
    );

    /**
     * 执行状态转换
     *
     * @param plan         绩效计划
     * @param targetStatus 目标状态
     * @throws BusinessException 如果状态转换不合法
     */
    public void transition(PerformancePlan plan, PlanStatus targetStatus) {
        PlanStatus currentStatus = plan.getStatus();
        
        // 检查是否允许转换
        if (!canTransition(currentStatus, targetStatus)) {
            String errorMsg = String.format("不允许从 %s(%s) 转换到 %s(%s)",
                    currentStatus, currentStatus.getDescription(),
                    targetStatus, targetStatus.getDescription());
            log.warn("非法状态转换: {}", errorMsg);
            throw new BusinessException("PLAN_STATUS_INVALID", errorMsg);
        }
        
        // 执行状态转换
        plan.setStatus(targetStatus);
        planRepository.save(plan);
        
        log.info("计划状态转换成功: planId={}, {} -> {}", 
                plan.getId(), currentStatus, targetStatus);
    }

    /**
     * 检查状态转换是否合法
     *
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @return true 如果转换合法
     */
    public boolean canTransition(PlanStatus currentStatus, PlanStatus targetStatus) {
        Set<PlanStatus> allowedTargets = VALID_TRANSITIONS.get(currentStatus);
        return allowedTargets != null && allowedTargets.contains(targetStatus);
    }

    /**
     * 获取当前状态允许的所有下一个状态
     *
     * @param currentStatus 当前状态
     * @return 允许的目标状态集合
     */
    public Set<PlanStatus> getAllowedNextStates(PlanStatus currentStatus) {
        return VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    }
}
