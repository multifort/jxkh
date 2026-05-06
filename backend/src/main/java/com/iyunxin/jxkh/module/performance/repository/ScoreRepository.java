package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.Score;
import com.iyunxin.jxkh.module.performance.domain.ScoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评分 Repository
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    /**
     * 查询计划的某个指标某人的某种类型评分
     */
    Optional<Score> findByPlanIdAndIndicatorInstanceIdAndEvaluatorIdAndTypeAndIsDeletedFalse(
            Long planId, Long indicatorInstanceId, Long evaluatorId, ScoreType type);

    /**
     * 查询计划的所有评分
     */
    List<Score> findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(Long planId);

    /**
     * 查询某人的待评分列表（按评分类型）
     */
    @Query("SELECT DISTINCT s.planId FROM Score s WHERE s.evaluatorId = :evaluatorId " +
           "AND s.type = :type AND s.isDeleted = false")
    List<Long> findDistinctPlanIdsByEvaluatorIdAndType(@Param("evaluatorId") Long evaluatorId,
                                                        @Param("type") ScoreType type);

    /**
     * 统计计划的某种类型评分数量
     */
    long countByPlanIdAndTypeAndIsDeletedFalse(Long planId, ScoreType type);

    /**
     * 查询某人已提交的评分数量
     */
    long countByEvaluatorIdAndStatusAndIsDeletedFalse(Long evaluatorId, 
                                                       com.iyunxin.jxkh.module.performance.domain.ScoreStatus status);
    
    /**
     * 批量统计多个计划的某种类型评分数量
     * @return List<Object[]> where Object[0] = planId, Object[1] = count
     */
    @Query("SELECT s.planId, COUNT(s) FROM Score s WHERE s.planId IN :planIds AND s.type = :type AND s.isDeleted = false GROUP BY s.planId")
    List<Object[]> countByPlanIdInAndTypeAndIsDeletedFalse(@Param("planIds") List<Long> planIds,
                                                            @Param("type") ScoreType type);
}
