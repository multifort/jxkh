package com.iyunxin.jxkh.module.performance.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 权重方案实体
 */
@Entity
@Table(name = "weight_schemes")
@SQLRestriction("is_deleted = 0")
public class WeightScheme {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String code;
    
    @Column(name = "cycle_id")
    private Long cycleId;
    
    @Column(name = "org_id")
    private Long orgId;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WeightSchemeStatus status = WeightSchemeStatus.DRAFT;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "total_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal totalWeight = BigDecimal.ZERO;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "published_by")
    private Long publishedBy;
    
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
    
    public Long getCycleId() {
        return cycleId;
    }
    
    public void setCycleId(Long cycleId) {
        this.cycleId = cycleId;
    }
    
    public Long getOrgId() {
        return orgId;
    }
    
    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public WeightSchemeStatus getStatus() {
        return status;
    }
    
    public void setStatus(WeightSchemeStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getTotalWeight() {
        return totalWeight;
    }
    
    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public Long getPublishedBy() {
        return publishedBy;
    }
    
    public void setPublishedBy(Long publishedBy) {
        this.publishedBy = publishedBy;
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
