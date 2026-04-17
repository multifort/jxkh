package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.PerformancePlan;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 绩效计划 Repository
 */
@Repository
public interface PerformancePlanRepository extends JpaRepository<PerformancePlan, Long>, JpaSpecificationExecutor<PerformancePlan> {

    /**
     * 根据用户ID和周期ID查询计划（检查是否已存在）
     */
    Optional<PerformancePlan> findByUserIdAndCycleIdAndIsDeletedFalse(Long userId, Long cycleId);

    /**
     * 根据周期ID查询计划列表
     */
    List<PerformancePlan> findByCycleIdAndIsDeletedFalse(Long cycleId);

    /**
     * 根据状态查询计划列表
     */
    List<PerformancePlan> findByStatusAndIsDeletedFalse(PlanStatus status);

    /**
     * 根据组织ID查询计划列表
     */
    List<PerformancePlan> findByOrgIdAndIsDeletedFalse(Long orgId);

    /**
     * 根据评估人ID查询待评估的计划
     */
    List<PerformancePlan> findByEvaluatorIdAndStatusAndIsDeletedFalse(Long evaluatorId, PlanStatus status);

    /**
     * 根据用户ID查询计划列表
     */
    List<PerformancePlan> findByUserIdAndIsDeletedFalse(Long userId);

    /**
     * 根据ID和删除状态查询
     */
    Optional<PerformancePlan> findByIdAndIsDeletedFalse(Long id);
}
