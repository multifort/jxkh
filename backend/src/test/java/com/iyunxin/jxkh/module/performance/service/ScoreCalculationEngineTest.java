package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.performance.domain.*;
import com.iyunxin.jxkh.module.performance.repository.IndicatorInstanceRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import com.iyunxin.jxkh.module.performance.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ScoreCalculationEngine 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("分数计算引擎测试")
class ScoreCalculationEngineTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private PerformancePlanRepository planRepository;

    @Mock
    private IndicatorInstanceRepository indicatorInstanceRepository;

    @InjectMocks
    private ScoreCalculationEngine calculationEngine;

    private PerformancePlan plan;
    private List<IndicatorInstance> indicators;
    private List<Score> scores;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        plan = new PerformancePlan();
        plan.setId(1L);
        plan.setUserId(2L);
        plan.setStatus(PlanStatus.PENDING_EVAL);

        // 创建3个指标实例（权重总和=100%）
        indicators = new ArrayList<>();
        
        IndicatorInstance indicator1 = new IndicatorInstance();
        indicator1.setId(10L);
        indicator1.setPlanId(1L);
        indicator1.setWeight(new BigDecimal("40")); // 40%
        indicators.add(indicator1);

        IndicatorInstance indicator2 = new IndicatorInstance();
        indicator2.setId(11L);
        indicator2.setPlanId(1L);
        indicator2.setWeight(new BigDecimal("35")); // 35%
        indicators.add(indicator2);

        IndicatorInstance indicator3 = new IndicatorInstance();
        indicator3.setId(12L);
        indicator3.setPlanId(1L);
        indicator3.setWeight(new BigDecimal("25")); // 25%
        indicators.add(indicator3);

        // 创建评分数据（自评和上级评）
        scores = new ArrayList<>();

        // 指标1：自评90，上级评85
        Score score1Self = new Score();
        score1Self.setId(1L);
        score1Self.setPlanId(1L);
        score1Self.setIndicatorInstanceId(10L);
        score1Self.setEvaluatorId(2L);
        score1Self.setScore(new BigDecimal("90"));
        score1Self.setType(ScoreType.SELF);
        scores.add(score1Self);

        Score score1Manager = new Score();
        score1Manager.setId(2L);
        score1Manager.setPlanId(1L);
        score1Manager.setIndicatorInstanceId(10L);
        score1Manager.setEvaluatorId(1L);
        score1Manager.setScore(new BigDecimal("85"));
        score1Manager.setType(ScoreType.MANAGER);
        scores.add(score1Manager);

        // 指标2：自评88，上级评82
        Score score2Self = new Score();
        score2Self.setId(3L);
        score2Self.setPlanId(1L);
        score2Self.setIndicatorInstanceId(11L);
        score2Self.setEvaluatorId(2L);
        score2Self.setScore(new BigDecimal("88"));
        score2Self.setType(ScoreType.SELF);
        scores.add(score2Self);

        Score score2Manager = new Score();
        score2Manager.setId(4L);
        score2Manager.setPlanId(1L);
        score2Manager.setIndicatorInstanceId(11L);
        score2Manager.setEvaluatorId(1L);
        score2Manager.setScore(new BigDecimal("82"));
        score2Manager.setType(ScoreType.MANAGER);
        scores.add(score2Manager);

        // 指标3：自评92，上级评87
        Score score3Self = new Score();
        score3Self.setId(5L);
        score3Self.setPlanId(1L);
        score3Self.setIndicatorInstanceId(12L);
        score3Self.setEvaluatorId(2L);
        score3Self.setScore(new BigDecimal("92"));
        score3Self.setType(ScoreType.SELF);
        scores.add(score3Self);

        Score score3Manager = new Score();
        score3Manager.setId(6L);
        score3Manager.setPlanId(1L);
        score3Manager.setIndicatorInstanceId(12L);
        score3Manager.setEvaluatorId(1L);
        score3Manager.setScore(new BigDecimal("87"));
        score3Manager.setType(ScoreType.MANAGER);
        scores.add(score3Manager);
    }

    @Test
    @DisplayName("计算计划分数 - 成功（A级）")
    void testCalculatePlanScore_LevelA() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        
        // 所有指标都已评分
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(3L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);
        
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(scores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify
        ArgumentCaptor<PerformancePlan> planCaptor = ArgumentCaptor.forClass(PerformancePlan.class);
        verify(planRepository, times(1)).save(planCaptor.capture());

        PerformancePlan savedPlan = planCaptor.getValue();
        assertNotNull(savedPlan.getFinalScore());
        assertNotNull(savedPlan.getPerformanceLevel());
        assertEquals(PlanStatus.EVALUATED, savedPlan.getStatus());
        assertNotNull(savedPlan.getCalculatedAt());

        // 验证等级为 A（>= 90分）
        // 计算过程：
        // 指标1: (90*0.3 + 85*0.7) * 0.4 = 86.5 * 0.4 = 34.6
        // 指标2: (88*0.3 + 82*0.7) * 0.35 = 83.8 * 0.35 = 29.33
        // 指标3: (92*0.3 + 87*0.7) * 0.25 = 88.5 * 0.25 = 22.125
        // 总分 = 34.6 + 29.33 + 22.125 = 86.055 ≈ 86.06
        // 由于 < 90，应该是 B 级
        assertEquals(PerformanceLevel.B, savedPlan.getPerformanceLevel());
    }

    @Test
    @DisplayName("计算计划分数 - B级（80-89分）")
    void testCalculatePlanScore_LevelB() {
        // 修改分数使得最终得分在 80-89 之间
        scores.get(0).setScore(new BigDecimal("85")); // 指标1自评
        scores.get(1).setScore(new BigDecimal("80")); // 指标1上级评

        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(3L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(scores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify
        ArgumentCaptor<PerformancePlan> planCaptor = ArgumentCaptor.forClass(PerformancePlan.class);
        verify(planRepository, times(1)).save(planCaptor.capture());

        PerformancePlan savedPlan = planCaptor.getValue();
        assertEquals(PerformanceLevel.B, savedPlan.getPerformanceLevel());
    }

    @Test
    @DisplayName("计算计划分数 - C级（70-79分）")
    void testCalculatePlanScore_LevelC() {
        // 修改分数使得最终得分在 70-79 之间
        for (Score score : scores) {
            score.setScore(new BigDecimal("75"));
        }

        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(3L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(scores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify
        ArgumentCaptor<PerformancePlan> planCaptor = ArgumentCaptor.forClass(PerformancePlan.class);
        verify(planRepository, times(1)).save(planCaptor.capture());

        PerformancePlan savedPlan = planCaptor.getValue();
        assertEquals(PerformanceLevel.C, savedPlan.getPerformanceLevel());
    }

    @Test
    @DisplayName("计算计划分数 - D级（< 70分）")
    void testCalculatePlanScore_LevelD() {
        // 修改分数使得最终得分 < 70
        for (Score score : scores) {
            score.setScore(new BigDecimal("60"));
        }

        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(3L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(scores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify
        ArgumentCaptor<PerformancePlan> planCaptor = ArgumentCaptor.forClass(PerformancePlan.class);
        verify(planRepository, times(1)).save(planCaptor.capture());

        PerformancePlan savedPlan = planCaptor.getValue();
        assertEquals(PerformanceLevel.D, savedPlan.getPerformanceLevel());
    }

    @Test
    @DisplayName("计算计划分数 - 计划不存在")
    void testCalculatePlanScore_PlanNotFound() {
        // Mock: 计划不存在
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            calculationEngine.calculatePlanScore(1L);
        });

        assertEquals("PLAN_NOT_FOUND", exception.getCode());
    }

    @Test
    @DisplayName("计算计划分数 - 没有指标实例")
    void testCalculatePlanScore_NoIndicators() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(new ArrayList<>());

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            calculationEngine.calculatePlanScore(1L);
        });

        assertEquals("NO_INDICATORS", exception.getCode());
    }

    @Test
    @DisplayName("计算计划分数 - 评分未完成")
    void testCalculatePlanScore_ScoreIncomplete() {
        // Mock: 只有2个自评，但有3个指标
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(2L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            calculationEngine.calculatePlanScore(1L);
        });

        assertTrue(exception.getMessage().contains("评分未完成"));
    }

    @Test
    @DisplayName("计算计划分数 - 边界值（正好90分）")
    void testCalculatePlanScore_Exactly90() {
        // 调整分数使得最终得分正好90分
        // 为了简化，我们让所有指标的加权分数都是90
        for (Score score : scores) {
            score.setScore(new BigDecimal("90"));
        }

        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(3L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(scores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify
        ArgumentCaptor<PerformancePlan> planCaptor = ArgumentCaptor.forClass(PerformancePlan.class);
        verify(planRepository, times(1)).save(planCaptor.capture());

        PerformancePlan savedPlan = planCaptor.getValue();
        assertEquals(PerformanceLevel.A, savedPlan.getPerformanceLevel());
        assertEquals(0, savedPlan.getFinalScore().compareTo(new BigDecimal("90")));
    }

    @Test
    @DisplayName("计算计划分数 - 边界值（正好80分）")
    void testCalculatePlanScore_Exactly80() {
        // 调整分数使得最终得分正好80分
        for (Score score : scores) {
            score.setScore(new BigDecimal("80"));
        }

        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(3L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(scores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify
        ArgumentCaptor<PerformancePlan> planCaptor = ArgumentCaptor.forClass(PerformancePlan.class);
        verify(planRepository, times(1)).save(planCaptor.capture());

        PerformancePlan savedPlan = planCaptor.getValue();
        assertEquals(PerformanceLevel.B, savedPlan.getPerformanceLevel());
        assertEquals(0, savedPlan.getFinalScore().compareTo(new BigDecimal("80")));
    }

    @Test
    @DisplayName("计算计划分数 - 边界值（正好70分）")
    void testCalculatePlanScore_Exactly70() {
        // 调整分数使得最终得分正好70分
        for (Score score : scores) {
            score.setScore(new BigDecimal("70"));
        }

        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(3L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(scores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify
        ArgumentCaptor<PerformancePlan> planCaptor = ArgumentCaptor.forClass(PerformancePlan.class);
        verify(planRepository, times(1)).save(planCaptor.capture());

        PerformancePlan savedPlan = planCaptor.getValue();
        assertEquals(PerformanceLevel.C, savedPlan.getPerformanceLevel());
        assertEquals(0, savedPlan.getFinalScore().compareTo(new BigDecimal("70")));
    }

    @Test
    @DisplayName("计算计划分数 - 单个指标")
    void testCalculatePlanScore_SingleIndicator() {
        // 只保留一个指标
        List<IndicatorInstance> singleIndicator = List.of(indicators.get(0));
        List<Score> singleScores = scores.subList(0, 2); // 只保留指标1的自评和上级评

        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(singleIndicator);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(1L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(1L);
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(singleScores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify
        ArgumentCaptor<PerformancePlan> planCaptor = ArgumentCaptor.forClass(PerformancePlan.class);
        verify(planRepository, times(1)).save(planCaptor.capture());

        PerformancePlan savedPlan = planCaptor.getValue();
        assertNotNull(savedPlan.getFinalScore());
        // 单个指标，权重100%，分数应该是 (90*0.3 + 85*0.7) = 86.5
        assertEquals(0, savedPlan.getFinalScore().compareTo(new BigDecimal("86.50")));
    }

    @Test
    @DisplayName("计算计划分数 - 验证事务性")
    void testCalculatePlanScore_Transactional() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(3L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(3L);
        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L)).thenReturn(scores);

        // Execute
        calculationEngine.calculatePlanScore(1L);

        // Verify: 确保只调用了一次 save
        verify(planRepository, times(1)).save(any(PerformancePlan.class));
    }
}
