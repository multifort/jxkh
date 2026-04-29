package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.performance.domain.PerformancePlan;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PlanStateMachine 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlanStateMachine 单元测试")
class PlanStateMachineTest {

    @Mock
    private PerformancePlanRepository planRepository;

    @InjectMocks
    private PlanStateMachine planStateMachine;

    private PerformancePlan testPlan;

    @BeforeEach
    void setUp() {
        testPlan = new PerformancePlan();
        testPlan.setId(1L);
        testPlan.setStatus(PlanStatus.DRAFT);
    }

    @Test
    @DisplayName("合法转换 - DRAFT 到 PENDING_APPROVE")
    void testValidTransition_DraftToPendingApprove() {
        // Execute
        planStateMachine.transition(testPlan, PlanStatus.PENDING_APPROVE);

        // Verify
        assertEquals(PlanStatus.PENDING_APPROVE, testPlan.getStatus());
        verify(planRepository).save(testPlan);
    }

    @Test
    @DisplayName("合法转换 - PENDING_APPROVE 到 IN_PROGRESS（审批通过）")
    void testValidTransition_PendingApproveToInProgress() {
        testPlan.setStatus(PlanStatus.PENDING_APPROVE);

        // Execute
        planStateMachine.transition(testPlan, PlanStatus.IN_PROGRESS);

        // Verify
        assertEquals(PlanStatus.IN_PROGRESS, testPlan.getStatus());
        verify(planRepository).save(testPlan);
    }

    @Test
    @DisplayName("合法转换 - PENDING_APPROVE 到 DRAFT（审批驳回）")
    void testValidTransition_PendingApproveToDraft() {
        testPlan.setStatus(PlanStatus.PENDING_APPROVE);

        // Execute
        planStateMachine.transition(testPlan, PlanStatus.DRAFT);

        // Verify
        assertEquals(PlanStatus.DRAFT, testPlan.getStatus());
        verify(planRepository).save(testPlan);
    }

    @Test
    @DisplayName("非法转换 - DRAFT 直接到 IN_PROGRESS")
    void testInvalidTransition_DraftToInProgress() {
        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            planStateMachine.transition(testPlan, PlanStatus.IN_PROGRESS);
        });

        assertEquals("PLAN_STATUS_INVALID", exception.getCode());
        assertTrue(exception.getMessage().contains("DRAFT"));
        assertTrue(exception.getMessage().contains("IN_PROGRESS"));
    }

    @Test
    @DisplayName("非法转换 - DRAFT 到 EVALUATED")
    void testInvalidTransition_DraftToEvaluated() {
        // Execute & Verify
        assertThrows(BusinessException.class, () -> {
            planStateMachine.transition(testPlan, PlanStatus.EVALUATED);
        });
    }

    @Test
    @DisplayName("非法转换 - IN_PROGRESS 回到 DRAFT")
    void testInvalidTransition_InProgressToDraft() {
        testPlan.setStatus(PlanStatus.IN_PROGRESS);

        // Execute & Verify
        assertThrows(BusinessException.class, () -> {
            planStateMachine.transition(testPlan, PlanStatus.DRAFT);
        });
    }

    @Test
    @DisplayName("canTransition - 检查合法转换")
    void testCanTransition_Valid() {
        assertTrue(planStateMachine.canTransition(PlanStatus.DRAFT, PlanStatus.PENDING_APPROVE));
        assertTrue(planStateMachine.canTransition(PlanStatus.PENDING_APPROVE, PlanStatus.IN_PROGRESS));
        assertTrue(planStateMachine.canTransition(PlanStatus.PENDING_APPROVE, PlanStatus.DRAFT));
    }

    @Test
    @DisplayName("canTransition - 检查非法转换")
    void testCanTransition_Invalid() {
        assertFalse(planStateMachine.canTransition(PlanStatus.DRAFT, PlanStatus.IN_PROGRESS));
        assertFalse(planStateMachine.canTransition(PlanStatus.IN_PROGRESS, PlanStatus.DRAFT));
        assertFalse(planStateMachine.canTransition(PlanStatus.ARCHIVED, PlanStatus.DRAFT));
    }

    @Test
    @DisplayName("getAllowedNextStates - 获取允许的下一个状态")
    void testGetAllowedNextStates() {
        Set<PlanStatus> draftNextStates = planStateMachine.getAllowedNextStates(PlanStatus.DRAFT);
        assertEquals(1, draftNextStates.size());
        assertTrue(draftNextStates.contains(PlanStatus.PENDING_APPROVE));

        Set<PlanStatus> pendingApproveNextStates = planStateMachine.getAllowedNextStates(PlanStatus.PENDING_APPROVE);
        assertEquals(2, pendingApproveNextStates.size());
        assertTrue(pendingApproveNextStates.contains(PlanStatus.IN_PROGRESS));
        assertTrue(pendingApproveNextStates.contains(PlanStatus.DRAFT));

        // ARCHIVED 是终态，没有下一个状态
        Set<PlanStatus> archivedNextStates = planStateMachine.getAllowedNextStates(PlanStatus.ARCHIVED);
        assertEquals(0, archivedNextStates.size());
    }

    @Test
    @DisplayName("完整流程测试 - 正常流转")
    void testCompleteWorkflow() {
        // DRAFT -> PENDING_APPROVE
        planStateMachine.transition(testPlan, PlanStatus.PENDING_APPROVE);
        assertEquals(PlanStatus.PENDING_APPROVE, testPlan.getStatus());

        // PENDING_APPROVE -> IN_PROGRESS
        planStateMachine.transition(testPlan, PlanStatus.IN_PROGRESS);
        assertEquals(PlanStatus.IN_PROGRESS, testPlan.getStatus());

        // IN_PROGRESS -> PENDING_EVAL
        planStateMachine.transition(testPlan, PlanStatus.PENDING_EVAL);
        assertEquals(PlanStatus.PENDING_EVAL, testPlan.getStatus());

        // PENDING_EVAL -> EVALUATED
        planStateMachine.transition(testPlan, PlanStatus.EVALUATED);
        assertEquals(PlanStatus.EVALUATED, testPlan.getStatus());

        // EVALUATED -> CALIBRATED
        planStateMachine.transition(testPlan, PlanStatus.CALIBRATED);
        assertEquals(PlanStatus.CALIBRATED, testPlan.getStatus());

        // CALIBRATED -> ARCHIVED
        planStateMachine.transition(testPlan, PlanStatus.ARCHIVED);
        assertEquals(PlanStatus.ARCHIVED, testPlan.getStatus());

        // 验证 save 被调用了 6 次
        verify(planRepository, times(6)).save(any(PerformancePlan.class));
    }
}
