package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.common.util.SecurityUtils;
import com.iyunxin.jxkh.module.org.domain.Org;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.*;
import com.iyunxin.jxkh.module.performance.dto.PlanDetailDTO;
import com.iyunxin.jxkh.module.performance.dto.PlanListDTO;
import com.iyunxin.jxkh.module.performance.repository.IndicatorInstanceRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformanceCycleRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final com.iyunxin.jxkh.module.performance.repository.IndicatorRepository indicatorRepository;
    private final PlanStateMachine planStateMachine;

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

        // 5. 校验指标是否存在
        validateIndicatorsExist(request.getIndicators());

        // 6. 创建计划实体
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

        // 7. 创建指标实例
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
        PerformancePlan plan = findPlanById(planId);

        // 2. 只有草稿状态可以修改
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw new BusinessException("PLAN_STATUS_INVALID", "只有草稿状态的计划可以修改");
        }

        // 3. 校验权重总和
        validateWeight(request.getIndicators());

        // 4. 校验指标是否存在
        validateIndicatorsExist(request.getIndicators());

        // 5. 逻辑删除旧的指标实例
        logicalDeleteInstances(planId);

        // 6. 创建新的指标实例
        List<IndicatorInstance> instances = createIndicatorInstances(planId, request.getIndicators(), currentUserId);
        instanceRepository.saveAll(instances);

        log.info("更新计划草稿成功: {}", planId);
    }

    /**
     * 提交计划审批
     */
    @Transactional
    public void submitPlan(Long planId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        try {
            // 1. 查询计划
            PerformancePlan plan = findPlanById(planId);

            // 2. 校验状态（只有草稿可以提交）
            if (plan.getStatus() != PlanStatus.DRAFT) {
                throw new BusinessException("PLAN_STATUS_INVALID", "只有草稿状态的计划可以提交");
            }

            // 3. 校验权重总和
            BigDecimal totalWeight = instanceRepository.sumWeightByPlanId(planId);
            if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
                throw new BusinessException("PLAN_WEIGHT_INVALID", 
                    String.format("权重总和必须为100%%，当前为%.2f%%", totalWeight));
            }

            // 4. 使用状态机转换状态
            planStateMachine.transition(plan, PlanStatus.PENDING_APPROVE);

            // 5. 记录提交时间
            plan.setSubmittedAt(LocalDateTime.now());
            plan.setUpdatedBy(currentUserId);
            planRepository.save(plan);

            // 6. 获取主管信息并发送通知（暂时用日志占位）
            User employee = userRepository.findById(plan.getUserId())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
            
            if (employee.getManagerId() != null) {
                log.info("[通知占位] 发送审批通知给主管: managerId={}, planId={}, employeeName={}",
                        employee.getManagerId(), planId, employee.getName());
            } else {
                log.warn("[通知占位] 员工没有设置主管: userId={}, planId={}", employee.getId(), planId);
            }

            log.info("计划提交审批成功: planId={}", planId);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.warn("计划并发更新冲突: planId={}", planId);
            throw new BusinessException("PLAN_CONCURRENT_UPDATE", "计划已被他人修改，请刷新后重试");
        }
    }

    /**
     * 审批计划
     *
     * @param planId   计划ID
     * @param approved 是否通过
     * @param comment  审批意见
     */
    @Transactional
    public void approvePlan(Long planId, Boolean approved, String comment) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        try {
            // 1. 查询计划
            PerformancePlan plan = findPlanById(planId);

            // 2. 校验状态（只有待审批可以审批）
            if (plan.getStatus() != PlanStatus.PENDING_APPROVE) {
                throw new BusinessException("PLAN_STATUS_INVALID", "只有待审批状态的计划可以审批");
            }

            // 3. 使用状态机转换状态
            PlanStatus targetStatus = approved ? PlanStatus.IN_PROGRESS : PlanStatus.DRAFT;
            planStateMachine.transition(plan, targetStatus);

            // 4. 记录审批信息
            plan.setApprovedAt(LocalDateTime.now());
            plan.setComment(comment);
            plan.setUpdatedBy(currentUserId);
            planRepository.save(plan);

            // 5. 发送通知给员工（暂时用日志占位）
            User employee = userRepository.findById(plan.getUserId())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
            
            String result = approved ? "通过" : "驳回";
            log.info("[通知占位] 发送审批结果通知给员工: userId={}, planId={}, result={}, comment={}",
                    employee.getId(), planId, result, comment);

            log.info("计划审批完成: planId={}, result={}", planId, result);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.warn("计划并发更新冲突: planId={}", planId);
            throw new BusinessException("PLAN_CONCURRENT_UPDATE", "计划已被他人修改，请刷新后重试");
        }
    }

    /**
     * 根据ID查询计划详情
     */
    public PlanDetailDTO getPlanById(Long id) {
        PerformancePlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "绩效计划不存在"));

        // 数据权限校验
        checkDataPermission(plan);

        // 加载关联数据
        User employee = userRepository.findById(plan.getUserId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        PerformanceCycle cycle = cycleRepository.findById(plan.getCycleId())
                .orElseThrow(() -> new BusinessException("CYCLE_NOT_FOUND", "周期不存在"));
        
        Org org = orgRepository.findById(plan.getOrgId())
                .orElseThrow(() -> new BusinessException("ORG_NOT_FOUND", "组织不存在"));

        // 加载指标实例
        List<IndicatorInstance> instances = instanceRepository.findByPlanIdAndIsDeletedFalse(id);

        // 转换为 DTO
        return convertToPlanDetailDTO(plan, employee, cycle, org, instances);
    }

    /**
     * 根据ID查询计划实体（内部使用）
     */
    private PerformancePlan findPlanById(Long id) {
        return planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "绩效计划不存在"));
    }

    /**
     * 转换为 PlanDetailDTO
     */
    private PlanDetailDTO convertToPlanDetailDTO(PerformancePlan plan,
                                                   User employee,
                                                   PerformanceCycle cycle,
                                                   Org org,
                                                   List<IndicatorInstance> instances) {
        PlanDetailDTO dto = new PlanDetailDTO();
        dto.setId(plan.getId());
        dto.setUserId(plan.getUserId());
        dto.setEmployeeName(employee.getName());
        dto.setCycleId(plan.getCycleId());
        dto.setCycleName(cycle.getName());
        dto.setOrgId(plan.getOrgId());
        dto.setOrgName(org.getName());
        dto.setStatus(plan.getStatus());
        dto.setTotalScore(plan.getTotalScore());
        dto.setFinalLevel(plan.getFinalLevel());
        dto.setEvaluatorId(plan.getEvaluatorId());
        dto.setComment(plan.getComment());
        dto.setSubmittedAt(plan.getSubmittedAt());
        dto.setApprovedAt(plan.getApprovedAt());
        dto.setEvaluatedAt(plan.getEvaluatedAt());
        dto.setCalibratedAt(plan.getCalibratedAt());
        dto.setArchivedAt(plan.getArchivedAt());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());
        dto.setCreatedBy(plan.getCreatedBy());

        // 转换指标列表
        List<PlanDetailDTO.PlanIndicatorDetailDTO> indicatorDTOs = instances.stream()
            .map(instance -> {
                PlanDetailDTO.PlanIndicatorDetailDTO indicatorDTO = new PlanDetailDTO.PlanIndicatorDetailDTO();
                indicatorDTO.setId(instance.getId());
                indicatorDTO.setIndicatorId(instance.getIndicatorId());
                indicatorDTO.setOwnerId(instance.getOwnerId());
                indicatorDTO.setName(instance.getName());
                indicatorDTO.setType(instance.getType());
                indicatorDTO.setWeight(instance.getWeight());
                indicatorDTO.setTargetValue(instance.getTargetValue());
                indicatorDTO.setCurrentValue(instance.getCurrentValue());
                indicatorDTO.setProgress(instance.getProgress());
                indicatorDTO.setStatus(instance.getStatus() != null ? instance.getStatus().name() : null);
                indicatorDTO.setUnit(instance.getUnit());
                indicatorDTO.setRemark(instance.getRemark());
                indicatorDTO.setScore(instance.getScore());
                return indicatorDTO;
            })
            .collect(Collectors.toList());
        dto.setIndicators(indicatorDTOs);

        return dto;
    }

    /**
     * 分页查询计划列表
     */
    public Page<PlanListDTO> listPlans(int page, int size, Long cycleId, PlanStatus status) {
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

        // 批量加载关联数据（用户、周期）
        List<Long> userIds = plans.getContent().stream()
            .map(PerformancePlan::getUserId)
            .distinct()
            .collect(Collectors.toList());
        
        List<Long> cycleIds = plans.getContent().stream()
            .map(PerformancePlan::getCycleId)
            .distinct()
            .collect(Collectors.toList());

        // 查询用户信息
        java.util.Map<Long, String> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userRepository.findAllById(userIds);
            userMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        }

        // 查询周期信息
        java.util.Map<Long, String> cycleMap = new java.util.HashMap<>();
        if (!cycleIds.isEmpty()) {
            List<PerformanceCycle> cycles = cycleRepository.findAllById(cycleIds);
            cycleMap = cycles.stream()
                .collect(Collectors.toMap(PerformanceCycle::getId, PerformanceCycle::getName));
        }

        // 批量加载指标实例（优化N+1查询）
        List<Long> planIds = plans.getContent().stream()
            .map(PerformancePlan::getId)
            .collect(Collectors.toList());
        
        java.util.Map<Long, List<IndicatorInstance>> instancesMap = new java.util.HashMap<>();
        if (!planIds.isEmpty()) {
            List<IndicatorInstance> allInstances = instanceRepository.findByPlanIdInAndIsDeletedFalse(planIds);
            instancesMap = allInstances.stream()
                .collect(Collectors.groupingBy(IndicatorInstance::getPlanId));
        }

        // 转换为 DTO
        java.util.Map<Long, String> finalUserMap = userMap;
        java.util.Map<Long, String> finalCycleMap = cycleMap;
        java.util.Map<Long, List<IndicatorInstance>> finalInstancesMap = instancesMap;

        List<PlanListDTO> dtoList = plans.getContent().stream()
            .map(plan -> convertToPlanListDTO(plan, finalUserMap, finalCycleMap, finalInstancesMap))
            .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, plans.getTotalElements());
    }

    /**
     * 转换为 PlanListDTO
     */
    private PlanListDTO convertToPlanListDTO(PerformancePlan plan, 
                                              java.util.Map<Long, String> userMap,
                                              java.util.Map<Long, String> cycleMap,
                                              java.util.Map<Long, List<IndicatorInstance>> instancesMap) {
        PlanListDTO dto = new PlanListDTO();
        dto.setId(plan.getId());
        dto.setUserId(plan.getUserId());
        dto.setEmployeeName(userMap.getOrDefault(plan.getUserId(), "未知"));
        dto.setCycleId(plan.getCycleId());
        dto.setCycleName(cycleMap.getOrDefault(plan.getCycleId(), "未知"));
        dto.setOrgId(plan.getOrgId());
        dto.setStatus(plan.getStatus());
        dto.setTotalScore(plan.getTotalScore());
        dto.setFinalLevel(plan.getFinalLevel());
        dto.setSubmittedAt(plan.getSubmittedAt());
        dto.setApprovedAt(plan.getApprovedAt());
        dto.setEvaluatedAt(plan.getEvaluatedAt());
        dto.setCalibratedAt(plan.getCalibratedAt());
        dto.setArchivedAt(plan.getArchivedAt());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());

        // 转换指标列表
        List<IndicatorInstance> instances = instancesMap.getOrDefault(plan.getId(), new ArrayList<>());
        List<PlanListDTO.PlanIndicatorDTO> indicatorDTOs = instances.stream()
            .map(instance -> {
                PlanListDTO.PlanIndicatorDTO indicatorDTO = new PlanListDTO.PlanIndicatorDTO();
                indicatorDTO.setId(instance.getId());
                indicatorDTO.setIndicatorId(instance.getIndicatorId());
                indicatorDTO.setName(instance.getName());
                indicatorDTO.setType(instance.getType());
                indicatorDTO.setWeight(instance.getWeight());
                indicatorDTO.setTargetValue(instance.getTargetValue());
                indicatorDTO.setCurrentValue(instance.getCurrentValue());
                indicatorDTO.setProgress(instance.getProgress());
                indicatorDTO.setUnit(instance.getUnit());
                indicatorDTO.setRemark(instance.getRemark());
                return indicatorDTO;
            })
            .collect(Collectors.toList());
        dto.setIndicators(indicatorDTOs);

        return dto;
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
     * 校验指标是否存在
     */
    private void validateIndicatorsExist(List<IndicatorItemRequest> indicators) {
        for (IndicatorItemRequest item : indicators) {
            indicatorRepository.findByIdAndIsDeletedFalse(item.getIndicatorId())
                .orElseThrow(() -> new BusinessException("INDICATOR_NOT_FOUND", 
                    "指标不存在: " + item.getIndicatorId()));
        }
    }

    /**
     * 逻辑删除指标实例
     */
    @Transactional
    private void logicalDeleteInstances(Long planId) {
        List<IndicatorInstance> instances = instanceRepository.findByPlanIdAndIsDeletedFalse(planId);
        instances.forEach(instance -> instance.setIsDeleted(true));
        if (!instances.isEmpty()) {
            instanceRepository.saveAll(instances);
            log.info("逻辑删除指标实例: planId={}, count={}", planId, instances.size());
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
