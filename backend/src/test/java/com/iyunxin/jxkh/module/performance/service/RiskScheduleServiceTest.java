package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.module.performance.domain.IndicatorInstance;
import com.iyunxin.jxkh.module.performance.domain.PerformancePlan;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RiskScheduleService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("风险定时任务服务测试")
class RiskScheduleServiceTest {

    @Mock
    private PerformancePlanRepository planRepository;

    @Mock
    private RecordService recordService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RiskScheduleService riskScheduleService;

    @Test
    @DisplayName("每日风险检测 - 发现风险指标")
    void testDailyRiskDetection_WithRisks() {
        // Given
        PerformancePlan plan = createMockPlan(1L, 2L, PlanStatus.IN_PROGRESS);
        List<PerformancePlan> activePlans = Arrays.asList(plan);

        IndicatorInstance riskIndicator = createMockIndicator(1L, "年度营业收入", 45.0);
        List<IndicatorInstance> riskIndicators = Arrays.asList(riskIndicator);

        when(planRepository.findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS))
                .thenReturn(activePlans);
        when(recordService.getRiskIndicators(1L)).thenReturn(riskIndicators);

        // When
        riskScheduleService.dailyRiskDetection();

        // Then
        verify(planRepository, times(1))
                .findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS);
        verify(recordService, times(1)).getRiskIndicators(1L);
        verify(notificationService, times(1))
                .sendRiskNotification(eq(2L), eq(1L), anyList());
    }

    @Test
    @DisplayName("每日风险检测 - 无风险指标")
    void testDailyRiskDetection_NoRisks() {
        // Given
        PerformancePlan plan = createMockPlan(1L, 2L, PlanStatus.IN_PROGRESS);
        List<PerformancePlan> activePlans = Arrays.asList(plan);

        when(planRepository.findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS))
                .thenReturn(activePlans);
        when(recordService.getRiskIndicators(1L))
                .thenReturn(Collections.emptyList());

        // When
        riskScheduleService.dailyRiskDetection();

        // Then
        verify(planRepository, times(1))
                .findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS);
        verify(recordService, times(1)).getRiskIndicators(1L);
        verify(notificationService, never())
                .sendRiskNotification(anyLong(), anyLong(), anyList());
    }

    @Test
    @DisplayName("每日风险检测 - 无执行中的计划")
    void testDailyRiskDetection_NoActivePlans() {
        // Given
        when(planRepository.findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());

        // When
        riskScheduleService.dailyRiskDetection();

        // Then
        verify(planRepository, times(1))
                .findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS);
        verify(recordService, never()).getRiskIndicators(anyLong());
        verify(notificationService, never())
                .sendRiskNotification(anyLong(), anyLong(), anyList());
    }

    @Test
    @DisplayName("每日风险检测 - 多个计划多个风险")
    void testDailyRiskDetection_MultiplePlansWithRisks() {
        // Given
        PerformancePlan plan1 = createMockPlan(1L, 2L, PlanStatus.IN_PROGRESS);
        PerformancePlan plan2 = createMockPlan(2L, 3L, PlanStatus.IN_PROGRESS);
        List<PerformancePlan> activePlans = Arrays.asList(plan1, plan2);

        IndicatorInstance risk1 = createMockIndicator(1L, "年度营业收入", 45.0);
        IndicatorInstance risk2 = createMockIndicator(2L, "季度利润率", 30.0);
        IndicatorInstance risk3 = createMockIndicator(3L, "客户满意度", 50.0);

        when(planRepository.findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS))
                .thenReturn(activePlans);
        when(recordService.getRiskIndicators(1L))
                .thenReturn(Arrays.asList(risk1));
        when(recordService.getRiskIndicators(2L))
                .thenReturn(Arrays.asList(risk2, risk3));

        // When
        riskScheduleService.dailyRiskDetection();

        // Then
        verify(planRepository, times(1))
                .findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS);
        verify(recordService, times(1)).getRiskIndicators(1L);
        verify(recordService, times(1)).getRiskIndicators(2L);
        verify(notificationService, times(1))
                .sendRiskNotification(eq(2L), eq(1L), anyList());
        verify(notificationService, times(1))
                .sendRiskNotification(eq(3L), eq(2L), anyList());
    }

    @Test
    @DisplayName("每日风险检测 - 异常处理")
    void testDailyRiskDetection_ExceptionHandling() {
        // Given
        when(planRepository.findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then - 不应该抛出异常，只记录日志
        assertDoesNotThrow(() -> {
            riskScheduleService.dailyRiskDetection();
        });
    }

    // Helper methods
    private PerformancePlan createMockPlan(Long id, Long userId, PlanStatus status) {
        PerformancePlan plan = new PerformancePlan();
        plan.setId(id);
        plan.setUserId(userId);
        plan.setStatus(status);
        return plan;
    }

    private IndicatorInstance createMockIndicator(Long id, String name, Double progress) {
        IndicatorInstance indicator = new IndicatorInstance();
        indicator.setId(id);
        indicator.setName(name);
        indicator.setProgress(java.math.BigDecimal.valueOf(progress));
        return indicator;
    }
}
