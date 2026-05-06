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
 * ScoreService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评分服务测试")
class ScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private PerformancePlanRepository planRepository;

    @Mock
    private IndicatorInstanceRepository indicatorInstanceRepository;

    @InjectMocks
    private ScoreService scoreService;

    private PerformancePlan plan;
    private IndicatorInstance indicator;
    private ScoreService.ScoreSubmitRequest submitRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        plan = new PerformancePlan();
        plan.setId(1L);
        plan.setUserId(2L);
        plan.setStatus(PlanStatus.PENDING_EVAL);

        indicator = new IndicatorInstance();
        indicator.setId(10L);
        indicator.setPlanId(1L);
        indicator.setWeight(new BigDecimal("20"));

        // 准备提交请求
        submitRequest = new ScoreService.ScoreSubmitRequest();
        submitRequest.setPlanId(1L);
        submitRequest.setIndicatorInstanceId(10L);
        submitRequest.setScore(new BigDecimal("85"));
        submitRequest.setComment("表现良好");
        submitRequest.setType(ScoreType.SELF);
    }

    @Test
    @DisplayName("提交评分 - 成功")
    void testSubmitScore_Success() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator));
        when(scoreRepository.findByPlanIdAndIndicatorInstanceIdAndEvaluatorIdAndTypeAndIsDeletedFalse(
                anyLong(), anyLong(), anyLong(), any(ScoreType.class)))
                .thenReturn(Optional.empty());

        Score savedScore = new Score();
        savedScore.setId(1L);
        savedScore.setPlanId(1L);
        savedScore.setIndicatorInstanceId(10L);
        savedScore.setEvaluatorId(2L);
        savedScore.setScore(new BigDecimal("85"));
        savedScore.setType(ScoreType.SELF);
        when(scoreRepository.save(any(Score.class))).thenReturn(savedScore);

        // Execute
        Long scoreId = scoreService.submitScore(submitRequest, 2L);

        // Verify
        assertNotNull(scoreId);
        assertEquals(1L, scoreId);
        verify(scoreRepository, times(1)).save(any(Score.class));
    }

    @Test
    @DisplayName("提交评分 - 计划不存在")
    void testSubmitScore_PlanNotFound() {
        // Mock: 计划不存在
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scoreService.submitScore(submitRequest, 2L);
        });

        assertEquals("PLAN_NOT_FOUND", exception.getCode());
    }

    @Test
    @DisplayName("提交评分 - 计划状态不允许评分")
    void testSubmitScore_InvalidPlanStatus() {
        // Mock: 计划状态不是 PENDING_EVAL
        plan.setStatus(PlanStatus.DRAFT);
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scoreService.submitScore(submitRequest, 2L);
        });

        assertEquals("PLAN_STATUS_INVALID", exception.getCode());
    }

    @Test
    @DisplayName("提交评分 - 指标实例不存在")
    void testSubmitScore_IndicatorNotFound() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.empty());

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scoreService.submitScore(submitRequest, 2L);
        });

        assertEquals("INDICATOR_NOT_FOUND", exception.getCode());
    }

    @Test
    @DisplayName("提交评分 - 指标不属于该计划")
    void testSubmitScore_IndicatorPlanMismatch() {
        // Mock: 指标的 planId 不匹配
        indicator.setPlanId(99L);
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator));

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scoreService.submitScore(submitRequest, 2L);
        });

        assertEquals("INDICATOR_PLAN_MISMATCH", exception.getCode());
    }

    @Test
    @DisplayName("提交评分 - 重复评分")
    void testSubmitScore_DuplicateScore() {
        // Mock: 已存在评分
        Score existingScore = new Score();
        existingScore.setId(1L);
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator));
        when(scoreRepository.findByPlanIdAndIndicatorInstanceIdAndEvaluatorIdAndTypeAndIsDeletedFalse(
                1L, 10L, 2L, ScoreType.SELF))
                .thenReturn(Optional.of(existingScore));

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scoreService.submitScore(submitRequest, 2L);
        });

        assertTrue(exception.getMessage().contains("不能重复评分"));
    }

    @Test
    @DisplayName("提交评分 - 分数超出范围（小于0）")
    void testSubmitScore_ScoreOutOfRange_Low() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator));
        when(scoreRepository.findByPlanIdAndIndicatorInstanceIdAndEvaluatorIdAndTypeAndIsDeletedFalse(
                anyLong(), anyLong(), anyLong(), any(ScoreType.class)))
                .thenReturn(Optional.empty());

        // 设置分数为负数
        submitRequest.setScore(new BigDecimal("-10"));

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scoreService.submitScore(submitRequest, 2L);
        });

        assertEquals("SCORE_OUT_OF_RANGE", exception.getCode());
    }

    @Test
    @DisplayName("提交评分 - 分数超出范围（大于100）")
    void testSubmitScore_ScoreOutOfRange_High() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator));
        when(scoreRepository.findByPlanIdAndIndicatorInstanceIdAndEvaluatorIdAndTypeAndIsDeletedFalse(
                anyLong(), anyLong(), anyLong(), any(ScoreType.class)))
                .thenReturn(Optional.empty());

        // 设置分数超过100
        submitRequest.setScore(new BigDecimal("150"));

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            scoreService.submitScore(submitRequest, 2L);
        });

        assertEquals("SCORE_OUT_OF_RANGE", exception.getCode());
    }

    @Test
    @DisplayName("查询待评分计划列表")
    void testGetPendingPlans() {
        // Mock
        List<Long> expectedPlanIds = List.of(1L, 2L, 3L);
        when(scoreRepository.findDistinctPlanIdsByEvaluatorIdAndType(2L, ScoreType.SELF))
                .thenReturn(expectedPlanIds);

        // Execute
        List<Long> planIds = scoreService.getPendingPlans(2L, ScoreType.SELF);

        // Verify
        assertNotNull(planIds);
        assertEquals(3, planIds.size());
        assertEquals(1L, planIds.get(0));
        verify(scoreRepository, times(1)).findDistinctPlanIdsByEvaluatorIdAndType(2L, ScoreType.SELF);
    }

    @Test
    @DisplayName("查询计划的评分详情")
    void testGetScoresByPlanId() {
        // Mock
        List<Score> expectedScores = new ArrayList<>();
        Score score1 = new Score();
        score1.setId(1L);
        score1.setPlanId(1L);
        score1.setType(ScoreType.SELF);
        expectedScores.add(score1);

        Score score2 = new Score();
        score2.setId(2L);
        score2.setPlanId(1L);
        score2.setType(ScoreType.MANAGER);
        expectedScores.add(score2);

        when(scoreRepository.findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L))
                .thenReturn(expectedScores);

        // Execute
        List<Score> scores = scoreService.getScoresByPlanId(1L);

        // Verify
        assertNotNull(scores);
        assertEquals(2, scores.size());
        verify(scoreRepository, times(1)).findByPlanIdAndIsDeletedFalseOrderByCreatedAtAsc(1L);
    }

    @Test
    @DisplayName("查询评分进度 - 全部完成")
    void testGetScoreProgress_AllCompleted() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));

        List<IndicatorInstance> indicators = new ArrayList<>();
        indicators.add(indicator);
        indicators.add(new IndicatorInstance());
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);

        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(2L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(2L);

        // Execute
        ScoreService.ScoreProgressDTO progress = scoreService.getScoreProgress(1L);

        // Verify
        assertNotNull(progress);
        assertEquals(1L, progress.getPlanId());
        assertEquals(2, progress.getTotalIndicators());
        assertEquals(2, progress.getSelfScoredCount());
        assertEquals(2, progress.getManagerScoredCount());
        assertTrue(progress.isSelfCompleted());
        assertTrue(progress.isManagerCompleted());
        assertTrue(progress.isAllCompleted());
    }

    @Test
    @DisplayName("查询评分进度 - 部分完成")
    void testGetScoreProgress_PartialCompleted() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));

        List<IndicatorInstance> indicators = new ArrayList<>();
        indicators.add(indicator);
        indicators.add(new IndicatorInstance());
        indicators.add(new IndicatorInstance());
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(1L)).thenReturn(indicators);

        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.SELF)).thenReturn(2L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(1L, ScoreType.MANAGER)).thenReturn(1L);

        // Execute
        ScoreService.ScoreProgressDTO progress = scoreService.getScoreProgress(1L);

        // Verify
        assertNotNull(progress);
        assertEquals(3, progress.getTotalIndicators());
        assertEquals(2, progress.getSelfScoredCount());
        assertEquals(1, progress.getManagerScoredCount());
        assertFalse(progress.isSelfCompleted());
        assertFalse(progress.isManagerCompleted());
        assertFalse(progress.isAllCompleted());
    }

    @Test
    @DisplayName("批量查询评分进度")
    void testGetScoreProgressBatch() {
        // Mock
        List<Long> planIds = List.of(1L, 2L);

        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(planRepository.findByIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(plan));

        List<IndicatorInstance> indicators = new ArrayList<>();
        indicators.add(indicator);
        when(indicatorInstanceRepository.findByPlanIdAndIsDeletedFalse(anyLong())).thenReturn(indicators);

        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(anyLong(), eq(ScoreType.SELF))).thenReturn(1L);
        when(scoreRepository.countByPlanIdAndTypeAndIsDeletedFalse(anyLong(), eq(ScoreType.MANAGER))).thenReturn(1L);

        // Execute
        var progressMap = scoreService.getScoreProgressBatch(planIds);

        // Verify
        assertNotNull(progressMap);
        assertEquals(2, progressMap.size());
        assertTrue(progressMap.containsKey(1L));
        assertTrue(progressMap.containsKey(2L));
    }

    @Test
    @DisplayName("边界值测试 - 分数为0")
    void testSubmitScore_ScoreZero() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator));
        when(scoreRepository.findByPlanIdAndIndicatorInstanceIdAndEvaluatorIdAndTypeAndIsDeletedFalse(
                anyLong(), anyLong(), anyLong(), any(ScoreType.class)))
                .thenReturn(Optional.empty());

        Score savedScore = new Score();
        savedScore.setId(1L);
        when(scoreRepository.save(any(Score.class))).thenReturn(savedScore);

        // 设置分数为0
        submitRequest.setScore(BigDecimal.ZERO);

        // Execute
        Long scoreId = scoreService.submitScore(submitRequest, 2L);

        // Verify
        assertNotNull(scoreId);
        verify(scoreRepository, times(1)).save(argThat(score -> 
            score.getScore().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    @DisplayName("边界值测试 - 分数为100")
    void testSubmitScore_ScoreHundred() {
        // Mock
        when(planRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(plan));
        when(indicatorInstanceRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(indicator));
        when(scoreRepository.findByPlanIdAndIndicatorInstanceIdAndEvaluatorIdAndTypeAndIsDeletedFalse(
                anyLong(), anyLong(), anyLong(), any(ScoreType.class)))
                .thenReturn(Optional.empty());

        Score savedScore = new Score();
        savedScore.setId(1L);
        when(scoreRepository.save(any(Score.class))).thenReturn(savedScore);

        // 设置分数为100
        submitRequest.setScore(new BigDecimal("100"));

        // Execute
        Long scoreId = scoreService.submitScore(submitRequest, 2L);

        // Verify
        assertNotNull(scoreId);
        verify(scoreRepository, times(1)).save(argThat(score -> 
            score.getScore().compareTo(new BigDecimal("100")) == 0
        ));
    }
}
