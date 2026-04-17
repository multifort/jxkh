package com.iyunxin.jxkh.module.performance.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 绩效计划实体
 */
@Data
@Entity
@Table(name = "performance_plans")
public class PerformancePlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 计划ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 周期ID
     */
    @Column(name = "cycle_id", nullable = false)
    private Long cycleId;

    /**
     * 组织ID
     */
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus status = PlanStatus.DRAFT;

    /**
     * 总分
     */
    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    /**
     * 最终等级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "final_level", length = 10)
    private PerformanceLevel finalLevel;

    /**
     * 评估人ID
     */
    @Column(name = "evaluator_id")
    private Long evaluatorId;

    /**
     * 评语
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * 提交时间
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 审批时间
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 评估时间
     */
    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    /**
     * 校准时间
     */
    @Column(name = "calibrated_at")
    private LocalDateTime calibratedAt;

    /**
     * 归档时间
     */
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * 指标实例列表（非持久化字段，用于查询）
     */
    @Transient
    private List<IndicatorInstance> indicators;
}
