package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.WeightScheme;
import com.iyunxin.jxkh.module.performance.domain.WeightSchemeItem;
import com.iyunxin.jxkh.module.performance.domain.WeightSchemeStatus;
import com.iyunxin.jxkh.module.performance.repository.WeightSchemeItemRepository;
import com.iyunxin.jxkh.module.performance.repository.WeightSchemeRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 权重方案服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeightSchemeService {
    
    private final WeightSchemeRepository schemeRepository;
    private final WeightSchemeItemRepository itemRepository;
    private final UserRepository userRepository;
    private final OrgRepository orgRepository;
    
    /**
     * 分页查询方案列表
     */
    public Page<WeightScheme> listSchemes(String keyword, Long cycleId, String status, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Specification<WeightScheme> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                Predicate namePredicate = cb.like(root.get("name"), "%" + keyword + "%");
                Predicate codePredicate = cb.like(root.get("code"), "%" + keyword + "%");
                predicates.add(cb.or(namePredicate, codePredicate));
            }
            
            // 周期筛选
            if (cycleId != null) {
                predicates.add(cb.equal(root.get("cycleId"), cycleId));
            }
            
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            // 数据权限过滤
            applyDataPermission(predicates, root, cb, currentUser);
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return schemeRepository.findAll(spec, pageable);
    }
    
    /**
     * 根据ID获取方案
     */
    public WeightScheme getScheme(Long id) {
        WeightScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SCHEME_NOT_FOUND", "方案不存在"));
        
        checkDataPermission(scheme);
        return scheme;
    }
    
    /**
     * 获取方案明细列表
     */
    public List<WeightSchemeItem> getSchemeItems(Long schemeId) {
        getScheme(schemeId); // 权限检查
        return itemRepository.findBySchemeIdOrderBySortOrderAsc(schemeId);
    }
    
    /**
     * 创建方案
     */
    @Transactional
    public WeightScheme createScheme(WeightScheme scheme) {
        User currentUser = getCurrentUser();
        
        // 检查编码是否存在草稿版本
        var existing = schemeRepository.findTopByCodeAndOrgIdOrderByVersionDesc(scheme.getCode(), scheme.getOrgId());
        if (existing.isPresent() && existing.get().getStatus() == WeightSchemeStatus.DRAFT) {
            throw new BusinessException("SCHEME_CODE_EXISTS", "该编码已存在草稿方案");
        }
        
        // 设置默认值
        LocalDateTime now = LocalDateTime.now();
        scheme.setVersion(1);
        scheme.setStatus(WeightSchemeStatus.DRAFT);
        scheme.setTotalWeight(BigDecimal.ZERO);
        scheme.setCreatedBy(currentUser.getId());
        scheme.setCreatedAt(now);
        scheme.setUpdatedAt(now);
        scheme.setIsDeleted(false);
        
        return schemeRepository.save(scheme);
    }
    
    /**
     * 更新方案基本信息
     */
    @Transactional
    public WeightScheme updateScheme(Long id, WeightScheme scheme) {
        User currentUser = getCurrentUser();
        WeightScheme existing = getScheme(id);
        
        // 已发布的方案不允许修改
        if (existing.getStatus() != WeightSchemeStatus.DRAFT) {
            throw new BusinessException("SCHEME_STATUS_INVALID", "只有草稿状态的方案可以修改");
        }
        
        // 检查编码是否重复（排除自己）
        var duplicate = schemeRepository.findTopByCodeAndOrgIdOrderByVersionDesc(scheme.getCode(), scheme.getOrgId());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new BusinessException("SCHEME_CODE_EXISTS", "方案编码已存在");
        }
        
        // 更新字段
        existing.setName(scheme.getName());
        existing.setCode(scheme.getCode());
        existing.setCycleId(scheme.getCycleId());
        existing.setDescription(scheme.getDescription());
        existing.setUpdatedBy(currentUser.getId());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return schemeRepository.save(existing);
    }
    
    /**
     * 保存方案明细（批量）
     */
    @Transactional
    public void saveSchemeItems(Long schemeId, List<WeightSchemeItem> items) {
        WeightScheme scheme = getScheme(schemeId);
        
        // 只有草稿状态可以修改明细
        if (scheme.getStatus() != WeightSchemeStatus.DRAFT) {
            throw new BusinessException("SCHEME_STATUS_INVALID", "只有草稿状态的方案可以修改权重");
        }
        
        // 校验权重总和
        BigDecimal totalWeight = items.stream()
                .map(WeightSchemeItem::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
            throw new BusinessException("WEIGHT_SUM_INVALID", 
                    String.format("权重总和必须等于100%%，当前为%.2f%%", totalWeight));
        }
        
        // 删除旧明细
        itemRepository.deleteBySchemeId(schemeId);
        
        // 保存新明细
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < items.size(); i++) {
            WeightSchemeItem item = items.get(i);
            item.setSchemeId(schemeId);
            item.setSortOrder(i);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            itemRepository.save(item);
        }
        
        // 更新方案权重总和
        scheme.setTotalWeight(totalWeight);
        scheme.setUpdatedAt(now);
        schemeRepository.save(scheme);
    }
    
    /**
     * 发布方案
     */
    @Transactional
    public WeightScheme publishScheme(Long id) {
        User currentUser = getCurrentUser();
        WeightScheme scheme = getScheme(id);
        
        // 只有草稿状态可以发布
        if (scheme.getStatus() != WeightSchemeStatus.DRAFT) {
            throw new BusinessException("SCHEME_STATUS_INVALID", "只有草稿状态的方案可以发布");
        }
        
        // 校验权重总和
        BigDecimal totalWeight = itemRepository.sumWeightBySchemeId(id);
        if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
            throw new BusinessException("WEIGHT_SUM_INVALID", 
                    String.format("权重总和必须等于100%%，当前为%.2f%%", totalWeight));
        }
        
        // 检查是否有明细
        if (itemRepository.findBySchemeIdOrderBySortOrderAsc(id).isEmpty()) {
            throw new BusinessException("SCHEME_NO_ITEMS", "方案必须包含至少一个指标");
        }
        
        // 更新状态
        scheme.setStatus(WeightSchemeStatus.PUBLISHED);
        scheme.setTotalWeight(totalWeight);
        scheme.setPublishedAt(LocalDateTime.now());
        scheme.setPublishedBy(currentUser.getId());
        scheme.setUpdatedAt(LocalDateTime.now());
        
        return schemeRepository.save(scheme);
    }
    
    /**
     * 归档方案
     */
    @Transactional
    public WeightScheme archiveScheme(Long id) {
        WeightScheme scheme = getScheme(id);
        
        // 只有已发布状态可以归档
        if (scheme.getStatus() != WeightSchemeStatus.PUBLISHED) {
            throw new BusinessException("SCHEME_STATUS_INVALID", "只有已发布的方案可以归档");
        }
        
        scheme.setStatus(WeightSchemeStatus.ARCHIVED);
        scheme.setUpdatedAt(LocalDateTime.now());
        
        return schemeRepository.save(scheme);
    }
    
    /**
     * 删除方案
     */
    @Transactional
    public void deleteScheme(Long id) {
        WeightScheme scheme = getScheme(id);
        
        // 只有草稿状态可以删除
        if (scheme.getStatus() != WeightSchemeStatus.DRAFT) {
            throw new BusinessException("SCHEME_STATUS_INVALID", "只有草稿状态的方案可以删除");
        }
        
        // 删除明细
        itemRepository.deleteBySchemeId(id);
        
        // 逻辑删除方案
        scheme.setIsDeleted(true);
        schemeRepository.save(scheme);
    }
    
    /**
     * 复制方案
     */
    @Transactional
    public WeightScheme copyScheme(Long id) {
        WeightScheme source = getScheme(id);
        
        // 创建新方案
        WeightScheme newScheme = new WeightScheme();
        newScheme.setName(source.getName() + " (复制)");
        newScheme.setCode(source.getCode() + "_COPY_" + System.currentTimeMillis());
        newScheme.setCycleId(source.getCycleId());
        newScheme.setOrgId(source.getOrgId());
        newScheme.setVersion(1);
        newScheme.setStatus(WeightSchemeStatus.DRAFT);
        newScheme.setDescription(source.getDescription());
        newScheme.setTotalWeight(BigDecimal.ZERO);
        
        User currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        newScheme.setCreatedBy(currentUser.getId());
        newScheme.setCreatedAt(now);
        newScheme.setUpdatedAt(now);
        newScheme.setIsDeleted(false);
        
        WeightScheme saved = schemeRepository.save(newScheme);
        
        // 复制明细
        List<WeightSchemeItem> sourceItems = itemRepository.findBySchemeIdOrderBySortOrderAsc(id);
        for (WeightSchemeItem item : sourceItems) {
            WeightSchemeItem newItem = new WeightSchemeItem();
            newItem.setSchemeId(saved.getId());
            newItem.setIndicatorId(item.getIndicatorId());
            newItem.setWeight(item.getWeight());
            newItem.setSortOrder(item.getSortOrder());
            newItem.setCreatedAt(now);
            newItem.setUpdatedAt(now);
            itemRepository.save(newItem);
        }
        
        return saved;
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermission(List<Predicate> predicates, jakarta.persistence.criteria.Root<WeightScheme> root, 
                                     jakarta.persistence.criteria.CriteriaBuilder cb, User currentUser) {
        // ADMIN和HR可以查看所有方案
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }
        
        if ("MANAGER".equals(currentUser.getRole())) {
            // 部门主管：只能查看本部门及子部门的方案
            List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
            if (accessibleOrgIds.isEmpty()) {
                predicates.add(cb.isNull(root.get("orgId")));
            } else {
                predicates.add(root.get("orgId").in(accessibleOrgIds));
            }
        } else {
            // 普通员工：只能查看自己部门的方案或全公司方案
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
    private void checkDataPermission(WeightScheme scheme) {
        User currentUser = getCurrentUser();
        
        // ADMIN和HR可以访问所有方案
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }
        
        if ("MANAGER".equals(currentUser.getRole())) {
            // 部门主管：只能访问本部门及子部门的方案
            List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
            boolean hasAccess = accessibleOrgIds.contains(scheme.getOrgId()) || 
                               (scheme.getOrgId() == null);
            if (!hasAccess) {
                throw new BusinessException("PERMISSION_DENIED", "无权访问该方案");
            }
        } else {
            // 普通员工：只能访问自己部门的方案或全公司方案
            boolean hasAccess = scheme.getOrgId() == null || 
                               scheme.getOrgId().equals(currentUser.getOrgId());
            if (!hasAccess) {
                throw new BusinessException("PERMISSION_DENIED", "无权访问该方案");
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
