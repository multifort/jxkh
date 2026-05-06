package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.IndicatorInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 指标实例 Repository
 */
@Repository
public interface IndicatorInstanceRepository extends JpaRepository<IndicatorInstance, Long>, JpaSpecificationExecutor<IndicatorInstance> {

    /**
     * 根据计划ID查询指标实例列表
     */
    List<IndicatorInstance> findByPlanIdAndIsDeletedFalse(Long planId);

    /**
     * 根据责任人ID查询指标实例列表
     */
    List<IndicatorInstance> findByOwnerIdAndIsDeletedFalse(Long ownerId);

    /**
     * 计算指定计划的权重总和
     */
    @Query("SELECT COALESCE(SUM(i.weight), 0) FROM IndicatorInstance i WHERE i.planId = :planId AND i.isDeleted = false")
    BigDecimal sumWeightByPlanId(@Param("planId") Long planId);

    /**
     * 根据ID和删除状态查询
     */
    Optional<IndicatorInstance> findByIdAndIsDeletedFalse(Long id);

    /**
     * 批量查询指定计划的指标实例（用于优化N+1查询）
     */
    List<IndicatorInstance> findByPlanIdInAndIsDeletedFalse(List<Long> planIds);
    
    /**
     * 批量统计多个计划的指标实例数量
     * @return List<Object[]> where Object[0] = planId, Object[1] = count
     */
    @Query("SELECT i.planId, COUNT(i) FROM IndicatorInstance i WHERE i.planId IN :planIds AND i.isDeleted = false GROUP BY i.planId")
    List<Object[]> countByPlanIdInAndIsDeletedFalse(@Param("planIds") List<Long> planIds);
}
