package com.iyunxin.jxkh.module.performance.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指标实例实体
 * 解决"同一个指标不同人不同目标值"的问题
 */
@Data
@Entity
@Table(name = "indicator_instances")
public class IndicatorInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 指标模板ID
     */
    @Column(name = "indicator_id", nullable = false)
    private Long indicatorId;

    /**
     * 绩效计划ID
     */
    @Column(name = "plan_id", nullable = false)
    private Long planId;

    /**
     * 责任人ID
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * 指标名称（冗余）
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 指标类型（冗余）
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * 权重（0-100）
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    /**
     * 目标值
     */
    @Column(name = "target_value", precision = 15, scale = 2)
    private BigDecimal targetValue;

    /**
     * 当前值
     */
    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    /**
     * 进度百分比（0-100）
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal progress = BigDecimal.ZERO;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstanceStatus status = InstanceStatus.NOT_STARTED;

    /**
     * 单位
     */
    @Column(length = 20)
    private String unit;

    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;

    /**
     * 得分
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal score;

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
}
