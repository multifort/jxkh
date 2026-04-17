package com.iyunxin.jxkh.module.performance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 权重方案明细实体
 */
@Entity
@Table(name = "weight_scheme_items")
public class WeightSchemeItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;
    
    @Column(name = "indicator_id", nullable = false)
    private Long indicatorId;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSchemeId() {
        return schemeId;
    }
    
    public void setSchemeId(Long schemeId) {
        this.schemeId = schemeId;
    }
    
    public Long getIndicatorId() {
        return indicatorId;
    }
    
    public void setIndicatorId(Long indicatorId) {
        this.indicatorId = indicatorId;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
