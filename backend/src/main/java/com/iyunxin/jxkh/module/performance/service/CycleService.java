package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.common.util.SecurityUtils;
import com.iyunxin.jxkh.module.org.domain.Org;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.CycleStatus;
import com.iyunxin.jxkh.module.performance.domain.CycleType;
import com.iyunxin.jxkh.module.performance.domain.PerformanceCycle;
import com.iyunxin.jxkh.module.performance.repository.PerformanceCycleRepository;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 绩效周期服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CycleService {

    private final PerformanceCycleRepository cycleRepository;
    private final UserRepository userRepository;
    private final OrgRepository orgRepository;

    /**
     * 分页查询周期列表（带数据权限控制）
     */
    public Page<PerformanceCycle> getCycles(int page, int size, String keyword, CycleStatus status, Long orgId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 获取当前用户信息
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 构建查询条件
        Specification<PerformanceCycle> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只查询未删除的记录
            predicates.add(cb.equal(root.get("isDeleted"), false));
            
            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
            }
            
            // 状态筛选
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            // 组织ID筛选
            if (orgId != null) {
                predicates.add(cb.equal(root.get("orgId"), orgId));
            }
            
            // 数据权限过滤
            if (!"ADMIN".equals(currentUser.getRole()) && !"HR".equals(currentUser.getRole())) {
                if ("MANAGER".equals(currentUser.getRole())) {
                    // 部门主管：只能查看本部门及子部门的周期
                    List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
                    if (accessibleOrgIds.isEmpty()) {
                        predicates.add(cb.isNull(root.get("orgId")));
                    } else {
                        predicates.add(root.get("orgId").in(accessibleOrgIds));
                    }
                } else {
                    // 普通员工：只能查看自己部门的周期或全公司周期
                    predicates.add(
                        cb.or(
                            cb.equal(root.get("orgId"), currentUser.getOrgId()),
                            cb.isNull(root.get("orgId"))
                        )
                    );
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return cycleRepository.findAll(spec, pageable);
    }

    /**
     * 根据ID查询周期
     */
    public PerformanceCycle getCycleById(Long id) {
        PerformanceCycle cycle = cycleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("PERFORMANCE_CYCLE_NOT_FOUND", "绩效周期不存在"));
        
        // 数据权限校验
        checkDataPermission(cycle);
        
        return cycle;
    }

    /**
     * 创建周期
     */
    @Transactional
    public PerformanceCycle createCycle(PerformanceCycle cycle) {
        // 校验参数
        validateCycle(cycle);
        
        // 检查时间冲突
        checkDateConflict(cycle.getStartDate(), cycle.getEndDate(), null);
        
        // 设置默认值
        cycle.setStatus(CycleStatus.DRAFT);
        cycle.setCreatedBy(SecurityUtils.getCurrentUserId());
        cycle.setIsDeleted(false);
        
        PerformanceCycle saved = cycleRepository.save(cycle);
        log.info("创建绩效周期成功: {}", saved.getId());
        
        return saved;
    }

    /**
     * 更新周期
     */
    @Transactional
    public PerformanceCycle updateCycle(Long id, PerformanceCycle cycle) {
        PerformanceCycle existing = getCycleById(id);
        
        // 已开始的周期不允许修改
        if (existing.getStatus() != CycleStatus.DRAFT) {
            throw new BusinessException("CYCLE_CANNOT_MODIFY", "周期已开始，不允许修改");
        }
        
        // 校验参数
        validateCycle(cycle);
        
        // 检查时间冲突（排除自身）
        checkDateConflict(cycle.getStartDate(), cycle.getEndDate(), id);
        
        // 更新字段
        existing.setName(cycle.getName());
        existing.setType(cycle.getType());
        existing.setStartDate(cycle.getStartDate());
        existing.setEndDate(cycle.getEndDate());
        existing.setOrgId(cycle.getOrgId());
        existing.setRemark(cycle.getRemark());
        existing.setUpdatedBy(SecurityUtils.getCurrentUserId());
        
        PerformanceCycle updated = cycleRepository.save(existing);
        log.info("更新绩效周期成功: {}", updated.getId());
        
        return updated;
    }

    /**
     * 删除周期（逻辑删除）
     */
    @Transactional
    public void deleteCycle(Long id) {
        PerformanceCycle cycle = getCycleById(id);
        
        // 只有草稿状态的周期可以删除
        if (cycle.getStatus() != CycleStatus.DRAFT) {
            throw new BusinessException("CYCLE_CANNOT_DELETE", "只有草稿状态的周期可以删除");
        }
        
        cycle.setIsDeleted(true);
        cycle.setUpdatedBy(SecurityUtils.getCurrentUserId());
        cycleRepository.save(cycle);
        
        log.info("删除绩效周期成功: {}", id);
    }

    /**
     * 启动周期
     */
    @Transactional
    public PerformanceCycle startCycle(Long id) {
        PerformanceCycle cycle = getCycleById(id);
        
        // 只有草稿状态可以启动
        if (cycle.getStatus() != CycleStatus.DRAFT) {
            throw new BusinessException("CYCLE_STATUS_INVALID", "只有草稿状态的周期可以启动");
        }
        
        // 检查开始日期是否已到
        if (cycle.getStartDate().isAfter(LocalDate.now())) {
            throw new BusinessException("CYCLE_START_DATE_NOT_REACHED", "周期开始日期未到");
        }
        
        cycle.setStatus(CycleStatus.IN_PROGRESS);
        cycle.setUpdatedBy(SecurityUtils.getCurrentUserId());
        PerformanceCycle updated = cycleRepository.save(cycle);
        
        log.info("启动绩效周期成功: {}", id);
        return updated;
    }

    /**
     * 结束周期
     */
    @Transactional
    public PerformanceCycle endCycle(Long id) {
        PerformanceCycle cycle = getCycleById(id);
        
        // 只有进行中的周期可以结束
        if (cycle.getStatus() != CycleStatus.IN_PROGRESS) {
            throw new BusinessException("CYCLE_STATUS_INVALID", "只有进行中的周期可以结束");
        }
        
        cycle.setStatus(CycleStatus.ENDED);
        cycle.setUpdatedBy(SecurityUtils.getCurrentUserId());
        PerformanceCycle updated = cycleRepository.save(cycle);
        
        log.info("结束绩效周期成功: {}", id);
        return updated;
    }

    /**
     * 校验周期参数
     */
    private void validateCycle(PerformanceCycle cycle) {
        if (cycle.getName() == null || cycle.getName().trim().isEmpty()) {
            throw new BusinessException("CYCLE_NAME_REQUIRED", "周期名称不能为空");
        }
        
        if (cycle.getType() == null) {
            throw new BusinessException("CYCLE_TYPE_REQUIRED", "周期类型不能为空");
        }
        
        if (cycle.getStartDate() == null) {
            throw new BusinessException("CYCLE_START_DATE_REQUIRED", "开始日期不能为空");
        }
        
        if (cycle.getEndDate() == null) {
            throw new BusinessException("CYCLE_END_DATE_REQUIRED", "结束日期不能为空");
        }
        
        if (cycle.getStartDate().isAfter(cycle.getEndDate())) {
            throw new BusinessException("CYCLE_DATE_INVALID", "开始日期不能晚于结束日期");
        }
    }

    /**
     * 检查时间冲突
     */
    private void checkDateConflict(LocalDate startDate, LocalDate endDate, Long excludeId) {
        boolean hasConflict = cycleRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsDeletedFalse(
                endDate, startDate);
        
        if (hasConflict) {
            // 需要进一步确认是否是同一个周期
            if (excludeId != null) {
                PerformanceCycle conflictingCycle = cycleRepository.findByIsDeletedFalse().stream()
                        .filter(c -> !c.getId().equals(excludeId))
                        .filter(c -> !c.getStartDate().isAfter(endDate) && !c.getEndDate().isBefore(startDate))
                        .findFirst()
                        .orElse(null);
                
                if (conflictingCycle != null) {
                    throw new BusinessException("CYCLE_DATE_CONFLICT", 
                            String.format("与周期 [%s] 的时间范围冲突", conflictingCycle.getName()));
                }
            } else {
                throw new BusinessException("CYCLE_DATE_CONFLICT", "时间范围与其他周期冲突");
            }
        }
    }

    /**
     * 数据权限校验
     */
    private void checkDataPermission(PerformanceCycle cycle) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // ADMIN 和 HR 可以查看所有周期
        if ("ADMIN".equals(currentUser.getRole()) || "HR".equals(currentUser.getRole())) {
            return;
        }
        
        // MANAGER 只能查看本部门及子部门的周期
        if ("MANAGER".equals(currentUser.getRole())) {
            if (cycle.getOrgId() != null) {
                List<Long> accessibleOrgIds = getSubOrgIds(currentUser.getOrgId());
                if (!accessibleOrgIds.contains(cycle.getOrgId())) {
                    throw new BusinessException("PERMISSION_DENIED", "无权访问该周期的数据");
                }
            }
            return;
        }
        
        // EMPLOYEE 只能查看本部门或全公司的周期
        if (cycle.getOrgId() != null && !cycle.getOrgId().equals(currentUser.getOrgId())) {
            throw new BusinessException("PERMISSION_DENIED", "无权访问该周期的数据");
        }
    }

    /**
     * 获取子组织ID列表（包含自身）
     */
    private List<Long> getSubOrgIds(Long orgId) {
        if (orgId == null) {
            return List.of();
        }
        
        List<Long> result = new ArrayList<>();
        result.add(orgId);
        
        // 递归查询子组织
        List<Org> subOrgs = orgRepository.findByParentId(orgId);
        for (Org subOrg : subOrgs) {
            result.addAll(getSubOrgIds(subOrg.getId()));
        }
        
        return result;
    }
}
