package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.PerformanceRecord;
import com.iyunxin.jxkh.module.performance.domain.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 绩效记录 Repository
 */
@Repository
public interface PerformanceRecordRepository extends JpaRepository<PerformanceRecord, Long> {
    
    /**
     * 按计划和类型查询记录列表（按日期倒序）
     */
    Page<PerformanceRecord> findByPlanIdAndTypeOrderByRecordDateDesc(
            @Param("planId") Long planId,
            @Param("type") RecordType type,
            Pageable pageable);
    
    /**
     * 按计划查询所有类型记录（按日期倒序）
     */
    Page<PerformanceRecord> findByPlanIdOrderByRecordDateDesc(
            @Param("planId") Long planId,
            Pageable pageable);
    
    /**
     * 按用户和日期范围查询记录
     */
    @Query("SELECT r FROM PerformanceRecord r WHERE r.userId = :userId " +
           "AND r.recordDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.recordDate DESC")
    List<PerformanceRecord> findByUserIdAndRecordDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 统计计划的记录数量
     */
    long countByPlanId(@Param("planId") Long planId);
}
