package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.Indicator;
import com.iyunxin.jxkh.module.performance.domain.IndicatorStatus;
import com.iyunxin.jxkh.module.performance.repository.IndicatorCategoryRepository;
import com.iyunxin.jxkh.module.performance.repository.IndicatorRepository;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 指标服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorService {
    
    private final IndicatorRepository indicatorRepository;
    private final IndicatorCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrgRepository orgRepository;
    
    /**
     * 分页查询指标列表
     */
    public Page<Indicator> listIndicators(String keyword, Long categoryId, 
                                          String type, String status, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Specification<Indicator> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                Predicate namePredicate = cb.like(root.get("name"), "%" + keyword + "%");
                Predicate codePredicate = cb.like(root.get("code"), "%" + keyword + "%");
                predicates.add(cb.or(namePredicate, codePredicate));
            }
            
            // 分类筛选
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            
            // 类型筛选
            if (type != null && !type.isEmpty()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            // 数据权限过滤
            applyDataPermission(predicates, root, cb, currentUser);
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return indicatorRepository.findAll(spec, pageable);
    }
    
    /**
     * 根据ID获取指标
     */
    public Indicator getIndicator(Long id) {
        Indicator indicator = indicatorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("INDICATOR_NOT_FOUND", "指标不存在"));
        
        checkDataPermission(indicator);
        return indicator;
    }
    
    /**
     * 创建指标
     */
    @Transactional
    public Indicator createIndicator(Indicator indicator) {
        User currentUser = getCurrentUser();
        
        // 检查编码是否重复
        if (indicatorRepository.findByCodeAndOrgId(indicator.getCode(), indicator.getOrgId()).isPresent()) {
            throw new BusinessException("INDICATOR_CODE_EXISTS", "指标编码已存在");
        }
        
        // 检查分类是否存在
        var category = categoryRepository.findById(indicator.getCategoryId());
        if (category.isEmpty()) {
            throw new BusinessException("CATEGORY_NOT_FOUND", "分类不存在");
        }
        
        // 设置默认状态
        if (indicator.getStatus() == null) {
            indicator.setStatus(IndicatorStatus.ACTIVE);
        }
        
        // 设置审计字段
        indicator.setCreatedBy(currentUser.getId());
        indicator.setCreatedAt(LocalDateTime.now());
        indicator.setIsDeleted(false);
        
        return indicatorRepository.save(indicator);
    }
    
    /**
     * 更新指标
     */
    @Transactional
    public Indicator updateIndicator(Long id, Indicator indicator) {
        User currentUser = getCurrentUser();
        Indicator existing = getIndicator(id);
        
        // 检查编码是否重复（排除自己）
        var duplicate = indicatorRepository.findByCodeAndOrgId(indicator.getCode(), indicator.getOrgId());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new BusinessException("INDICATOR_CODE_EXISTS", "指标编码已存在");
        }
        
        // 检查分类是否存在
        if (!categoryRepository.existsById(indicator.getCategoryId())) {
            throw new BusinessException("CATEGORY_NOT_FOUND", "分类不存在");
        }
        
        // 更新字段
        existing.setName(indicator.getName());
        existing.setCode(indicator.getCode());
        existing.setCategoryId(indicator.getCategoryId());
        existing.setType(indicator.getType());
        existing.setUnit(indicator.getUnit());
        existing.setDescription(indicator.getDescription());
        existing.setCalculationMethod(indicator.getCalculationMethod());
        existing.setDataSource(indicator.getDataSource());
        existing.setTargetType(indicator.getTargetType());
        existing.setDefaultWeight(indicator.getDefaultWeight());
        existing.setStatus(indicator.getStatus());
        existing.setUpdatedBy(currentUser.getId());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return indicatorRepository.save(existing);
    }
    
    /**
     * 删除指标
     */
    @Transactional
    public void deleteIndicator(Long id) {
        Indicator indicator = getIndicator(id);
        indicator.setIsDeleted(true);
        indicatorRepository.save(indicator);
    }
    
    /**
     * 启用/禁用指标
     */
    @Transactional
    public Indicator toggleStatus(Long id) {
        Indicator indicator = getIndicator(id);
        
        if (indicator.getStatus() == IndicatorStatus.ACTIVE) {
            indicator.setStatus(IndicatorStatus.INACTIVE);
        } else {
            indicator.setStatus(IndicatorStatus.ACTIVE);
        }
        
        indicator.setUpdatedAt(LocalDateTime.now());
        return indicatorRepository.save(indicator);
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermission(List<Predicate> predicates, jakarta.persistence.criteria.Root<Indicator> root, 
                                     jakarta.persistence.criteria.CriteriaBuilder cb, User currentUser) {
        // ADMIN和HR可以查看所有指标
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }
        
        if ("MANAGER".equals(currentUser.getRole())) {
            // 部门主管：只能查看本部门及子部门的指标
            List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
            if (accessibleOrgIds.isEmpty()) {
                predicates.add(cb.isNull(root.get("orgId")));
            } else {
                predicates.add(root.get("orgId").in(accessibleOrgIds));
            }
        } else {
            // 普通员工：只能查看自己部门的指标或全公司指标
            predicates.add(
                cb.or(
                    cb.equal(root.get("orgId"), currentUser.getOrgId()),
                    cb.isNull(root.get("orgId"))
                )
            );
        }
    }
    
    /**
     * 检查数据权限
     */
    private void checkDataPermission(Indicator indicator) {
        User currentUser = getCurrentUser();
        
        // ADMIN和HR可以访问所有指标
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }
        
        if ("MANAGER".equals(currentUser.getRole())) {
            // 部门主管：只能访问本部门及子部门的指标
            List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
            boolean hasAccess = accessibleOrgIds.contains(indicator.getOrgId()) || 
                               (indicator.getOrgId() == null);
            if (!hasAccess) {
                throw new BusinessException("PERMISSION_DENIED", "无权访问该指标");
            }
        } else {
            // 普通员工：只能访问自己部门的指标或全公司指标
            boolean hasAccess = indicator.getOrgId() == null || 
                               indicator.getOrgId().equals(currentUser.getOrgId());
            if (!hasAccess) {
                throw new BusinessException("PERMISSION_DENIED", "无权访问该指标");
            }
        }
    }
    
    /**
     * 获取子组织ID列表（包括自己）
     */
    private List<Long> getSubOrgIds(Long orgId) {
        if (orgId == null) {
            return new ArrayList<>();
        }
        
        List<Long> result = new ArrayList<>();
        result.add(orgId);
        
        // 递归查询子组织
        orgRepository.findByParentId(orgId).forEach(child -> {
            result.addAll(getSubOrgIds(child.getId()));
        });
        
        return result;
    }
    
    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("UNAUTHORIZED", "未登录");
        }
        
        Long userId = (Long) authentication.getPrincipal();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    }
}
