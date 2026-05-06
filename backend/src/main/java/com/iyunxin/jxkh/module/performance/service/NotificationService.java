package com.iyunxin.jxkh.module.performance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyunxin.jxkh.module.notification.domain.Notification;
import com.iyunxin.jxkh.module.notification.domain.NotificationType;
import com.iyunxin.jxkh.module.notification.repository.NotificationRepository;
import com.iyunxin.jxkh.module.performance.domain.IndicatorInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送风险预警通知
     *
     * @param userId 用户ID
     * @param planId 计划ID
     * @param riskIndicators 风险指标列表
     */
    @Transactional
    public void sendRiskNotification(Long userId, Long planId, List<IndicatorInstance> riskIndicators) {
        try {
            // 构建通知内容
            String title = "绩效计划风险预警";
            
            // 提取风险指标名称
            String indicatorNames = riskIndicators.stream()
                    .map(IndicatorInstance::getName)
                    .collect(Collectors.joining("、"));
            
            String content = String.format(
                    "您的绩效计划（ID: %d）中有 %d 个指标存在延期风险：%s。请及时关注并调整工作进度。",
                    planId,
                    riskIndicators.size(),
                    indicatorNames
            );
            
            // 创建通知
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(NotificationType.RISK_WARNING);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedId(planId);
            notification.setIsRead(false);
            
            // 保存通知
            notificationRepository.save(notification);
            
            log.info("风险预警通知发送成功: userId={}, planId={}, riskCount={}", 
                    userId, planId, riskIndicators.size());
            
        } catch (Exception e) {
            log.error("发送风险预警通知失败: userId={}, planId={}", userId, planId, e);
        }
    }
}
