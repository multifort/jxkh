package com.iyunxin.jxkh.module.notification.service;

import com.iyunxin.jxkh.module.notification.domain.Notification;
import com.iyunxin.jxkh.module.notification.domain.NotificationType;
import com.iyunxin.jxkh.module.notification.repository.NotificationRepository;
import com.iyunxin.jxkh.module.performance.domain.IndicatorInstance;
import com.iyunxin.jxkh.module.performance.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("通知服务测试")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Long userId = 1L;
    private Long planId = 1L;

    @Test
    @DisplayName("发送风险预警通知 - 成功")
    void testSendRiskNotification_Success() {
        // Given
        List<IndicatorInstance> riskIndicators = Arrays.asList(
                createMockIndicator(1L, "年度营业收入", 45.0),
                createMockIndicator(2L, "季度利润率", 30.0)
        );

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        savedNotification.setUserId(userId);
        savedNotification.setType(NotificationType.RISK_WARNING);
        savedNotification.setTitle("绩效计划风险预警");
        savedNotification.setIsRead(false);

        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        notificationService.sendRiskNotification(userId, planId, riskIndicators);

        // Then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification captured = captor.getValue();
        assertEquals(userId, captured.getUserId());
        assertEquals(NotificationType.RISK_WARNING, captured.getType());
        assertEquals("绩效计划风险预警", captured.getTitle());
        assertFalse(captured.getIsRead());
        assertEquals(planId, captured.getRelatedId());
        assertTrue(captured.getContent().contains("2 个指标存在延期风险"));
    }

    @Test
    @DisplayName("发送风险预警通知 - 单个指标")
    void testSendRiskNotification_SingleIndicator() {
        // Given
        List<IndicatorInstance> riskIndicators = Arrays.asList(
                createMockIndicator(1L, "年度营业收入", 45.0)
        );

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        notificationService.sendRiskNotification(userId, planId, riskIndicators);

        // Then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification captured = captor.getValue();
        assertTrue(captured.getContent().contains("1 个指标存在延期风险"));
        assertTrue(captured.getContent().contains("年度营业收入"));
    }

    @Test
    @DisplayName("发送风险预警通知 - 多个指标名称分隔")
    void testSendRiskNotification_MultipleIndicatorsNameSeparator() {
        // Given
        List<IndicatorInstance> riskIndicators = Arrays.asList(
                createMockIndicator(1L, "年度营业收入", 45.0),
                createMockIndicator(2L, "季度利润率", 30.0),
                createMockIndicator(3L, "客户满意度", 50.0)
        );

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        notificationService.sendRiskNotification(userId, planId, riskIndicators);

        // Then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification captured = captor.getValue();
        assertTrue(captured.getContent().contains("年度营业收入、季度利润率、客户满意度"));
    }

    @Test
    @DisplayName("发送风险预警通知 - 异常处理")
    void testSendRiskNotification_ExceptionHandling() {
        // Given
        List<IndicatorInstance> riskIndicators = Arrays.asList(
                createMockIndicator(1L, "年度营业收入", 45.0)
        );

        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then - 不应该抛出异常，只记录日志
        assertDoesNotThrow(() -> {
            notificationService.sendRiskNotification(userId, planId, riskIndicators);
        });
    }

    @Test
    @DisplayName("发送风险预警通知 - 空列表")
    void testSendRiskNotification_EmptyList() {
        // Given
        List<IndicatorInstance> riskIndicators = Arrays.asList();

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        notificationService.sendRiskNotification(userId, planId, riskIndicators);

        // Then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification captured = captor.getValue();
        assertTrue(captured.getContent().contains("0 个指标存在延期风险"));
    }

    // Helper methods
    private IndicatorInstance createMockIndicator(Long id, String name, Double progress) {
        IndicatorInstance indicator = new IndicatorInstance();
        indicator.setId(id);
        indicator.setName(name);
        indicator.setProgress(BigDecimal.valueOf(progress));
        return indicator;
    }
}
