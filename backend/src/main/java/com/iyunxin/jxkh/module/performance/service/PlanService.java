package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.common.util.SecurityUtils;
import com.iyunxin.jxkh.module.org.domain.Org;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.*;
import com.iyunxin.jxkh.module.performance.repository.IndicatorInstanceRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformanceCycleRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 绩效计划服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PerformancePlanRepository planRepository;
    private final IndicatorInstanceRepository instanceRepository;
    private final PerformanceCycleRepository cycleRepository;
    private final UserRepository userRepository;
    private final OrgRepository orgRepository;

    /**
     * 创建绩效计划
     */
    @Transactional
    public Long createPlan(PlanCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = getCurrentUser();

        // 1. 检查是否已存在计划（同一用户+周期）
        checkDuplicatePlan(request.getUserId(), request.getCycleId());

        // 2. 校验周期是否存在且有效
        PerformanceCycle cycle = cycleRepository.findByIdAndIsDeletedFalse(request.getCycleId())
                .orElseThrow(() -> new BusinessException("CYCLE_NOT_FOUND", "绩效周期不存在"));

        // 3. 获取用户信息以确定组织ID
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        // 4. 校验权重总和
        validateWeight(request.getIndicators());

        // 5. 创建计划实体
        PerformancePlan plan = new PerformancePlan();
        plan.setUserId(request.getUserId());
        plan.setCycleId(request.getCycleId());
        plan.setOrgId(targetUser.getOrgId());
        plan.setStatus(PlanStatus.DRAFT);
        plan.setCreatedBy(currentUserId);
        plan.setUpdatedBy(currentUserId);
        plan.setIsDeleted(false);

        PerformancePlan savedPlan = planRepository.save(plan);
        log.info("创建绩效计划成功: {}", savedPlan.getId());

        // 6. 创建指标实例
        List<IndicatorInstance> instances = createIndicatorInstances(savedPlan.getId(), request.getIndicators(), currentUserId);
        instanceRepository.saveAll(instances);

        return savedPlan.getId();
    }

    /**
     * 更新计划草稿
     */
    @Transactional
    public void updatePlanDraft(Long planId, PlanUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. 查询计划
        PerformancePlan plan = getPlanById(planId);

        // 2. 只有草稿状态可以修改
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw new BusinessException("PLAN_STATUS_INVALID", "只有草稿状态的计划可以修改");
        }

        // 3. 校验权重总和
        validateWeight(request.getIndicators());

        // 4. 删除旧的指标实例
        instanceRepository.deleteByPlanIdAndIsDeletedFalse(planId);

        // 5. 创建新的指标实例
        List<IndicatorInstance> instances = createIndicatorInstances(planId, request.getIndicators(), currentUserId);
        instanceRepository.saveAll(instances);

        log.info("更新计划草稿成功: {}", planId);
    }

    /**
     * 根据ID查询计划详情
     */
    public PerformancePlan getPlanById(Long id) {
        PerformancePlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "绩效计划不存在"));

        // 数据权限校验
        checkDataPermission(plan);

        // 加载指标实例
        List<IndicatorInstance> instances = instanceRepository.findByPlanIdAndIsDeletedFalse(id);
        plan.setIndicators(instances);

        return plan;
    }

    /**
     * 分页查询计划列表
     */
    public Page<PerformancePlan> listPlans(int page, int size, Long cycleId, PlanStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 获取当前用户
        User currentUser = getCurrentUser();

        // 构建查询条件
        Specification<PerformancePlan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 只查询未删除的记录
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // 周期ID筛选
            if (cycleId != null) {
                predicates.add(cb.equal(root.get("cycleId"), cycleId));
            }

            // 状态筛选
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 数据权限过滤
            applyDataPermission(predicates, root, cb, currentUser);

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PerformancePlan> plans = planRepository.findAll(spec, pageable);

        // 加载每个计划的指标实例
        plans.getContent().forEach(plan -> {
            List<IndicatorInstance> instances = instanceRepository.findByPlanIdAndIsDeletedFalse(plan.getId());
            plan.setIndicators(instances);
        });

        return plans;
    }

    /**
     * 检查是否已存在计划
     */
    private void checkDuplicatePlan(Long userId, Long cycleId) {
        boolean exists = planRepository.findByUserIdAndCycleIdAndIsDeletedFalse(userId, cycleId).isPresent();
        if (exists) {
            throw new BusinessException("PLAN_DUPLICATE", "该用户在此周期下已存在绩效计划");
        }
    }

    /**
     * 校验权重总和（必须等于100%）
     */
    private void validateWeight(List<IndicatorItemRequest> indicators) {
        if (indicators == null || indicators.isEmpty()) {
            throw new BusinessException("PLAN_INDICATORS_EMPTY", "至少需要添加一个指标");
        }

        BigDecimal totalWeight = indicators.stream()
                .map(IndicatorItemRequest::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 允许0.01的误差
        if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
            throw new BusinessException("PLAN_WEIGHT_INVALID",
                    String.format("权重总和必须等于100%%，当前为%.2f%%", totalWeight.doubleValue()));
        }
    }

    /**
     * 创建指标实例列表
     */
    private List<IndicatorInstance> createIndicatorInstances(Long planId, List<IndicatorItemRequest> items, Long currentUserId) {
        List<IndicatorInstance> instances = new ArrayList<>();

        for (IndicatorItemRequest item : items) {
            IndicatorInstance instance = new IndicatorInstance();
            instance.setIndicatorId(item.getIndicatorId());
            instance.setPlanId(planId);
            instance.setOwnerId(item.getOwnerId());
            instance.setName(item.getName());
            instance.setType(item.getType());
            instance.setWeight(item.getWeight());
            instance.setTargetValue(item.getTargetValue());
            instance.setCurrentValue(BigDecimal.ZERO);
            instance.setProgress(BigDecimal.ZERO);
            instance.setStatus(InstanceStatus.NOT_STARTED);
            instance.setUnit(item.getUnit());
            instance.setRemark(item.getRemark());
            instance.setCreatedBy(currentUserId);
            instance.setUpdatedBy(currentUserId);
            instance.setIsDeleted(false);

            instances.add(instance);
        }

        return instances;
    }

    /**
     * 应用数据权限过滤
     */
    private void applyDataPermission(List<Predicate> predicates, jakarta.persistence.criteria.Root<PerformancePlan> root,
                                     jakarta.persistence.criteria.CriteriaBuilder cb, User currentUser) {
        // ADMIN和HR可以查看所有计划
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }

        if ("MANAGER".equals(currentUser.getRole())) {
            // 部门主管：只能查看本部门及子部门的计划
            List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
            if (accessibleOrgIds.isEmpty()) {
                predicates.add(cb.isNull(root.get("orgId")));
            } else {
                predicates.add(root.get("orgId").in(accessibleOrgIds));
            }
        } else {
            // 普通员工：只能查看自己的计划
            predicates.add(cb.equal(root.get("userId"), currentUser.getId()));
        }
    }

    /**
     * 检查数据权限
     */
    private void checkDataPermission(PerformancePlan plan) {
        User currentUser = getCurrentUser();

        // ADMIN和HR可以访问所有计划
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }

        if ("MANAGER".equals(currentUser.getRole())) {
            // 部门主管：只能访问本部门及子部门的计划
            List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
            boolean hasAccess = accessibleOrgIds.contains(plan.getOrgId());
            if (!hasAccess) {
                throw new BusinessException("PERMISSION_DENIED", "无权访问该计划");
            }
        } else {
            // 普通员工：只能访问自己的计划
            if (!plan.getUserId().equals(currentUser.getId())) {
                throw new BusinessException("PERMISSION_DENIED", "无权访问该计划");
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
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    }

    // ==================== DTO 类 ====================

    /**
     * 创建计划请求
     */
    @lombok.Data
    public static class PlanCreateRequest {
        private Long userId;
        private Long cycleId;
        private List<IndicatorItemRequest> indicators;
    }

    /**
     * 更新计划请求
     */
    @lombok.Data
    public static class PlanUpdateRequest {
        private List<IndicatorItemRequest> indicators;
    }

    /**
     * 指标项请求
     */
    @lombok.Data
    public static class IndicatorItemRequest {
        private Long indicatorId;
        private Long ownerId;
        private String name;
        private String type;
        private BigDecimal weight;
        private BigDecimal targetValue;
        private String unit;
        private String remark;
    }
}
