package com.iyunxin.jxkh.module.performance.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 绩效记录实体
 */
@Data
@Entity
@Table(name = "performance_records")
@SQLRestriction("is_deleted = 0")
@EqualsAndHashCode(callSuper = false)
public class PerformanceRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 绩效计划ID
     */
    @Column(name = "plan_id", nullable = false)
    private Long planId;
    
    /**
     * 员工ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 记录类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RecordType type;
    
    /**
     * 内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    /**
     * 进度（0-100）
     */
    @Column(name = "progress", precision = 5, scale = 2)
    private BigDecimal progress;
    
    /**
     * 附件URL列表（JSON）
     */
    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments;
    
    /**
     * 记录日期
     */
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    /**
     * AI总结
     */
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;
    
    /**
     * AI建议（JSON）
     */
    @Column(name = "ai_suggestions", columnDefinition = "JSON")
    private String aiSuggestions;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
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
     * 逻辑删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
