package com.iyunxin.jxkh.module.performance.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指标实体
 */
@Entity
@Table(name = "indicators")
@SQLRestriction("is_deleted = 0")
public class Indicator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String code;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IndicatorType type;
    
    @Column(length = 20)
    private String unit;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "calculation_method", columnDefinition = "TEXT")
    private String calculationMethod;
    
    @Column(name = "data_source", length = 200)
    private String dataSource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20)
    private TargetType targetType;
    
    @Column(name = "default_weight", precision = 5, scale = 2)
    private BigDecimal defaultWeight;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IndicatorStatus status = IndicatorStatus.ACTIVE;
    
    @Column(name = "org_id")
    private Long orgId;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public IndicatorType getType() {
        return type;
    }
    
    public void setType(IndicatorType type) {
        this.type = type;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCalculationMethod() {
        return calculationMethod;
    }
    
    public void setCalculationMethod(String calculationMethod) {
        this.calculationMethod = calculationMethod;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    public TargetType getTargetType() {
        return targetType;
    }
    
    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }
    
    public BigDecimal getDefaultWeight() {
        return defaultWeight;
    }
    
    public void setDefaultWeight(BigDecimal defaultWeight) {
        this.defaultWeight = defaultWeight;
    }
    
    public IndicatorStatus getStatus() {
        return status;
    }
    
    public void setStatus(IndicatorStatus status) {
        this.status = status;
    }
    
    public Long getOrgId() {
        return orgId;
    }
    
    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
