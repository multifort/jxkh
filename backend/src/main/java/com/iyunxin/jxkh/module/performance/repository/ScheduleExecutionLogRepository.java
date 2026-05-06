package com.iyunxin.jxkh.module.performance.repository;

import com.iyunxin.jxkh.module.performance.domain.ScheduleExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务执行历史 Repository
 */
@Repository
public interface ScheduleExecutionLogRepository extends JpaRepository<ScheduleExecutionLog, Long> {
    
    /**
     * 分页查询任务执行历史
     */
    Page<ScheduleExecutionLog> findByTaskNameOrderByStartTimeDesc(String taskName, Pageable pageable);
    
    /**
     * 查询最近N次执行记录
     */
    List<ScheduleExecutionLog> findTop10ByTaskNameOrderByStartTimeDesc(String taskName);
    
    /**
     * 查询指定时间范围内的执行记录
     */
    List<ScheduleExecutionLog> findByTaskNameAndStartTimeBetweenOrderByStartTimeDesc(
            String taskName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计执行成功率
     */
    long countByTaskNameAndStatusAndStartTimeAfter(String taskName, String status, LocalDateTime startTime);
}
