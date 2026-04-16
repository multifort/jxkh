package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.CycleStatus;
import com.iyunxin.jxkh.module.performance.domain.PerformanceCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 绩效周期 Repository
 */
@Repository
public interface PerformanceCycleRepository extends JpaRepository<PerformanceCycle, Long>, JpaSpecificationExecutor<PerformanceCycle> {

    /**
     * 根据状态查询周期列表
     */
    List<PerformanceCycle> findByStatusAndIsDeletedFalse(CycleStatus status);

    /**
     * 查询所有未删除的周期
     */
    List<PerformanceCycle> findByIsDeletedFalse();

    /**
     * 检查指定时间范围内是否存在冲突的周期
     */
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsDeletedFalse(
            LocalDate endDate, LocalDate startDate);

    /**
     * 根据组织ID查询周期列表
     */
    List<PerformanceCycle> findByOrgIdAndIsDeletedFalse(Long orgId);

    /**
     * 根据组织ID列表查询周期列表（用于数据权限过滤）
     */
    List<PerformanceCycle> findByOrgIdInAndIsDeletedFalse(List<Long> orgIds);

    /**
     * 根据ID和删除状态查询
     */
    Optional<PerformanceCycle> findByIdAndIsDeletedFalse(Long id);
}
