package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.*;
import com.iyunxin.jxkh.module.performance.repository.IndicatorInstanceRepository;
import com.iyunxin.jxkh.module.performance.repository.IndicatorRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformanceCycleRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PlanService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("绩效计划服务测试")
class PlanServiceTest {

    @Mock
    private PerformancePlanRepository planRepository;

    @Mock
    private IndicatorInstanceRepository instanceRepository;

    @Mock
    private PerformanceCycleRepository cycleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrgRepository orgRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @InjectMocks
    private PlanService planService;

    private User adminUser;
    private PerformanceCycle cycle;
    private PlanService.PlanCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");
        adminUser.setOrgId(1L);

        cycle = new PerformanceCycle();
        cycle.setId(1L);
        cycle.setName("2026 Q2");

        // 准备创建请求
        createRequest = new PlanService.PlanCreateRequest();
        createRequest.setUserId(2L);
        createRequest.setCycleId(1L);

        List<PlanService.IndicatorItemRequest> indicators = new ArrayList<>();
        
        PlanService.IndicatorItemRequest item1 = new PlanService.IndicatorItemRequest();
        item1.setIndicatorId(10L);
        item1.setOwnerId(2L);
        item1.setName("营业收入");
        item1.setType("QUANTITATIVE");
        item1.setWeight(new BigDecimal("60"));
        item1.setTargetValue(new BigDecimal("1000"));
        indicators.add(item1);

        PlanService.IndicatorItemRequest item2 = new PlanService.IndicatorItemRequest();
        item2.setIndicatorId(11L);
        item2.setOwnerId(2L);
        item2.setName("客户满意度");
        item2.setType("QUANTITATIVE");
        item2.setWeight(new BigDecimal("40"));
        item2.setTargetValue(new BigDecimal("90"));
        indicators.add(item2);

        createRequest.setIndicators(indicators);

        // 设置当前用户
        setCurrentUser(adminUser);
    }

    @Test
    @DisplayName("创建计划 - 成功")
    void testCreatePlan_Success() {
        // Mock
        when(planRepository.findByUserIdAndCycleIdAndIsDeletedFalse(2L, 1L)).thenReturn(Optional.empty());
        when(cycleRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(cycle));
        
        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setOrgId(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        // Mock 指标存在
        Indicator indicator1 = new Indicator();
        indicator1.setId(10L);
        when(indicatorRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator1));
        
        Indicator indicator2 = new Indicator();
        indicator2.setId(11L);
        when(indicatorRepository.findByIdAndIsDeletedFalse(11L)).thenReturn(Optional.of(indicator2));

        PerformancePlan savedPlan = new PerformancePlan();
        savedPlan.setId(1L);
        savedPlan.setUserId(2L);
        savedPlan.setCycleId(1L);
        savedPlan.setStatus(PlanStatus.DRAFT);
        when(planRepository.save(any(PerformancePlan.class))).thenReturn(savedPlan);

        when(instanceRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Execute
        Long planId = planService.createPlan(createRequest);

        // Verify
        assertNotNull(planId);
        assertEquals(1L, planId);
        verify(planRepository, times(1)).save(any(PerformancePlan.class));
        verify(instanceRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("创建计划 - 重复计划")
    void testCreatePlan_Duplicate() {
        // Mock: 已存在计划
        when(planRepository.findByUserIdAndCycleIdAndIsDeletedFalse(2L, 1L)).thenReturn(Optional.of(new PerformancePlan()));

        // Execute & Verify
        assertThrows(BusinessException.class, () -> {
            planService.createPlan(createRequest);
        });
    }

    @Test
    @DisplayName("创建计划 - 权重不等于100%")
    void testCreatePlan_InvalidWeight() {
        // Mock
        when(planRepository.findByUserIdAndCycleIdAndIsDeletedFalse(2L, 1L)).thenReturn(Optional.empty());
        when(cycleRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(cycle));
        
        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setOrgId(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        // 修改权重为不等于100%
        createRequest.getIndicators().get(0).setWeight(new BigDecimal("50"));
        createRequest.getIndicators().get(1).setWeight(new BigDecimal("30"));

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            planService.createPlan(createRequest);
        });

        assertEquals("PLAN_WEIGHT_INVALID", exception.getCode());
    }

    @Test
    @DisplayName("创建计划 - 指标不存在")
    void testCreatePlan_IndicatorNotFound() {
        // Mock
        when(planRepository.findByUserIdAndCycleIdAndIsDeletedFalse(2L, 1L)).thenReturn(Optional.empty());
        when(cycleRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(cycle));
        
        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setOrgId(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        // Mock: 指标不存在
        when(indicatorRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(BusinessException.class, () -> {
            planService.createPlan(createRequest);
        });
    }

    @Test
    @DisplayName("更新草稿 - 成功")
    void testUpdatePlanDraft_Success() {
        // Mock
        PerformancePlan plan = new PerformancePlan();
        plan.setId(1L);
        plan.setStatus(PlanStatus.DRAFT);
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));

        // Mock 指标存在
        Indicator indicator1 = new Indicator();
        indicator1.setId(10L);
        when(indicatorRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator1));
        
        Indicator indicator2 = new Indicator();
        indicator2.setId(11L);
        when(indicatorRepository.findByIdAndIsDeletedFalse(11L)).thenReturn(Optional.of(indicator2));

        when(instanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(new ArrayList<>());
        when(instanceRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Execute
        PlanService.PlanUpdateRequest updateRequest = new PlanService.PlanUpdateRequest();
        updateRequest.setIndicators(createRequest.getIndicators());
        
        planService.updatePlanDraft(1L, updateRequest);

        // Verify
        verify(instanceRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("更新草稿 - 非草稿状态")
    void testUpdatePlanDraft_NotDraft() {
        // Mock
        PerformancePlan plan = new PerformancePlan();
        plan.setId(1L);
        plan.setStatus(PlanStatus.IN_PROGRESS);
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));

        // Execute & Verify
        PlanService.PlanUpdateRequest updateRequest = new PlanService.PlanUpdateRequest();
        updateRequest.setIndicators(createRequest.getIndicators());
        
        assertThrows(BusinessException.class, () -> {
            planService.updatePlanDraft(1L, updateRequest);
        });
    }

    @Test
    @DisplayName("查询计划详情 - 成功")
    void testGetPlanById_Success() {
        // Mock
        PerformancePlan plan = new PerformancePlan();
        plan.setId(1L);
        plan.setUserId(2L);
        plan.setOrgId(1L);
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(instanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(new ArrayList<>());

        // Execute
        PerformancePlan result = planService.getPlanById(1L);

        // Verify
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * 设置当前用户到 SecurityContext
     */
    private void setCurrentUser(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getId(), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }
}
