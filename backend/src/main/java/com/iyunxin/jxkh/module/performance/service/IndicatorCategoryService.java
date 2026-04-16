package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.IndicatorCategory;
import com.iyunxin.jxkh.module.performance.repository.IndicatorCategoryRepository;
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
 * 指标分类服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorCategoryService {
    
    private final IndicatorCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrgRepository orgRepository;
    
    /**
     * 分页查询分类列表
     */
    public Page<IndicatorCategory> listCategories(String keyword, Long parentId, Pageable pageable) {
        User currentUser = getCurrentUser();
        
        Specification<IndicatorCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                Predicate namePredicate = cb.like(root.get("name"), "%" + keyword + "%");
                Predicate codePredicate = cb.like(root.get("code"), "%" + keyword + "%");
                predicates.add(cb.or(namePredicate, codePredicate));
            }
            
            // 父分类筛选
            if (parentId != null) {
                predicates.add(cb.equal(root.get("parentId"), parentId));
            } else {
                // 默认只查询根分类
                predicates.add(cb.isNull(root.get("parentId")));
            }
            
            // 数据权限过滤
            applyDataPermission(predicates, root, cb, currentUser);
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return categoryRepository.findAll(spec, pageable);
    }
    
    /**
     * 获取分类树
     */
    public List<IndicatorCategory> getCategoryTree() {
        User currentUser = getCurrentUser();
        
        // 查询所有可见的分类
        Specification<IndicatorCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            applyDataPermission(predicates, root, cb, currentUser);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        List<IndicatorCategory> allCategories = categoryRepository.findAll(spec);
        
        // 构建树形结构（简单实现，前端可以递归处理）
        return allCategories.stream()
                .sorted((c1, c2) -> c1.getSortOrder().compareTo(c2.getSortOrder()))
                .toList();
    }
    
    /**
     * 根据ID获取分类
     */
    public IndicatorCategory getCategory(Long id) {
        IndicatorCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "分类不存在"));
        
        checkDataPermission(category);
        return category;
    }
    
    /**
     * 创建分类
     */
    @Transactional
    public IndicatorCategory createCategory(IndicatorCategory category) {
        User currentUser = getCurrentUser();
        
        // 检查编码是否重复
        if (categoryRepository.findByCodeAndOrgId(category.getCode(), category.getOrgId()).isPresent()) {
            throw new BusinessException("CATEGORY_CODE_EXISTS", "分类编码已存在");
        }
        
        // 设置层级
        if (category.getParentId() == null) {
            category.setLevel(1);
        } else {
            IndicatorCategory parent = categoryRepository.findById(category.getParentId())
                    .orElseThrow(() -> new BusinessException("PARENT_CATEGORY_NOT_FOUND", "父分类不存在"));
            category.setLevel(parent.getLevel() + 1);
        }
        
        // 设置审计字段
        category.setCreatedBy(currentUser.getId());
        category.setCreatedAt(LocalDateTime.now());
        category.setIsDeleted(false);
        
        return categoryRepository.save(category);
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public IndicatorCategory updateCategory(Long id, IndicatorCategory category) {
        User currentUser = getCurrentUser();
        IndicatorCategory existing = getCategory(id);
        
        // 检查编码是否重复（排除自己）
        var duplicate = categoryRepository.findByCodeAndOrgId(category.getCode(), category.getOrgId());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new BusinessException("CATEGORY_CODE_EXISTS", "分类编码已存在");
        }
        
        // 不允许修改父分类为自己
        if (category.getParentId() != null && category.getParentId().equals(id)) {
            throw new BusinessException("INVALID_PARENT_CATEGORY", "父分类不能是自己");
        }
        
        // 更新字段
        existing.setName(category.getName());
        existing.setCode(category.getCode());
        existing.setParentId(category.getParentId());
        existing.setSortOrder(category.getSortOrder());
        existing.setDescription(category.getDescription());
        existing.setUpdatedBy(currentUser.getId());
        existing.setUpdatedAt(LocalDateTime.now());
        
        // 如果修改了父分类，重新计算层级
        if (!existing.getParentId().equals(category.getParentId())) {
            if (existing.getParentId() == null) {
                existing.setLevel(1);
            } else {
                IndicatorCategory parent = categoryRepository.findById(existing.getParentId())
                        .orElseThrow(() -> new BusinessException("PARENT_CATEGORY_NOT_FOUND", "父分类不存在"));
                existing.setLevel(parent.getLevel() + 1);
            }
        }
        
        return categoryRepository.save(existing);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        if (categoryRepository.existsByParentIdAndIsDeletedFalse(id)) {
            throw new BusinessException("CATEGORY_HAS_CHILDREN", "该分类下还有子分类，无法删除");
        }
        
        IndicatorCategory category = getCategory(id);
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermission(List<Predicate> predicates, jakarta.persistence.criteria.Root<IndicatorCategory> root, 
                                     jakarta.persistence.criteria.CriteriaBuilder cb, User currentUser) {
        // ADMIN和HR可以查看所有分类
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }
        
        if ("MANAGER".equals(currentUser.getRole())) {
            // 部门主管：只能查看本部门及子部门的分类
            List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
            if (accessibleOrgIds.isEmpty()) {
                predicates.add(cb.isNull(root.get("orgId")));
            } else {
                predicates.add(root.get("orgId").in(accessibleOrgIds));
            }
        } else {
            // 普通员工：只能查看自己部门的分类或全公司分类
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
    private void checkDataPermission(IndicatorCategory category) {
        User currentUser = getCurrentUser();
        
        // ADMIN和HR可以访问所有分类
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }
        
        if ("MANAGER".equals(currentUser.getRole())) {
            // 部门主管：只能访问本部门及子部门的分类
            List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
            boolean hasAccess = accessibleOrgIds.contains(category.getOrgId()) || 
                               (category.getOrgId() == null);
            if (!hasAccess) {
                throw new BusinessException("PERMISSION_DENIED", "无权访问该分类");
            }
        } else {
            // 普通员工：只能访问自己部门的分类或全公司分类
            boolean hasAccess = category.getOrgId() == null || 
                               category.getOrgId().equals(currentUser.getOrgId());
            if (!hasAccess) {
                throw new BusinessException("PERMISSION_DENIED", "无权访问该分类");
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
