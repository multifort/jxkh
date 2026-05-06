package com.iyunxin.jxkh.module.performance.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评分实体
 */
@Data
@Entity
@Table(name = "scores")
public class Score implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评分ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 绩效计划ID
     */
    @Column(name = "plan_id", nullable = false)
    private Long planId;

    /**
     * 指标实例ID
     */
    @Column(name = "indicator_instance_id", nullable = false)
    private Long indicatorInstanceId;

    /**
     * 评分人ID
     */
    @Column(name = "evaluator_id", nullable = false)
    private Long evaluatorId;

    /**
     * 分数（0-100）
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    /**
     * 评语
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * 评分类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScoreType type;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScoreStatus status = ScoreStatus.SUBMITTED;

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
     * 版本号（乐观锁）
     */
    @Version
    @Column(nullable = false)
    private Integer version = 0;
}
