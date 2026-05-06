package com.iyunxin.jxkh.module.performance.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 定时任务执行历史实体
 */
@Data
@Entity
@Table(name = "schedule_execution_log")
@SQLRestriction("1=1")
public class ScheduleExecutionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务名称
     */
    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 执行状态：SUCCESS/FAILED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    /**
     * 检测到的风险数量
     */
    @Column(name = "risk_count")
    private Integer riskCount = 0;
    
    /**
     * 发送的通知数量
     */
    @Column(name = "notification_count")
    private Integer notificationCount = 0;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 执行时长（毫秒）
     */
    @Column(name = "duration_ms")
    private Long durationMs;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
