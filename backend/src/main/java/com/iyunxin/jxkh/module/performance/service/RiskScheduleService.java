package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.module.notification.repository.NotificationRepository;
import com.iyunxin.jxkh.module.performance.domain.IndicatorInstance;
import com.iyunxin.jxkh.module.performance.domain.PerformancePlan;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import com.iyunxin.jxkh.module.performance.domain.ScheduleExecutionLog;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import com.iyunxin.jxkh.module.performance.repository.ScheduleExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险预警定时任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskScheduleService {
    
    private final PerformancePlanRepository planRepository;
    private final RecordService recordService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final ScheduleExecutionLogRepository executionLogRepository;
    
    /**
     * 每天凌晨 2 点执行风险检测
     * cron 表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyRiskDetection() {
        log.info("开始执行每日风险检测任务...");
        
        // 创建执行记录
        ScheduleExecutionLog executionLog = new ScheduleExecutionLog();
        executionLog.setTaskName("dailyRiskDetection");
        executionLog.setStartTime(LocalDateTime.now());
        executionLog.setStatus("RUNNING");
        executionLog = executionLogRepository.save(executionLog);
        
        try {
            // 查询所有执行中的计划
            List<PerformancePlan> activePlans = planRepository.findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS);
            
            log.info("检测到 {} 个执行中的计划", activePlans.size());
            
            int totalRisks = 0;
            int totalNotifications = 0;
            
            for (PerformancePlan plan : activePlans) {
                // 查询该计划的风险指标
                List<IndicatorInstance> riskIndicators = recordService.getRiskIndicators(plan.getId());
                
                if (!riskIndicators.isEmpty()) {
                    log.warn("计划 {} 发现 {} 个风险指标", plan.getId(), riskIndicators.size());
                    
                    // 发送通知给计划负责人
                    notificationService.sendRiskNotification(
                            plan.getUserId(),
                            plan.getId(),
                            riskIndicators
                    );
                    
                    totalRisks += riskIndicators.size();
                    totalNotifications++;
                }
            }
            
            log.info("每日风险检测完成，共发现 {} 个风险指标", totalRisks);
            
            // 更新执行记录
            executionLog.setEndTime(LocalDateTime.now());
            executionLog.setStatus("SUCCESS");
            executionLog.setRiskCount(totalRisks);
            executionLog.setNotificationCount(totalNotifications);
            executionLog.setDurationMs(
                java.time.Duration.between(executionLog.getStartTime(), executionLog.getEndTime()).toMillis()
            );
            executionLogRepository.save(executionLog);
            
        } catch (Exception e) {
            log.error("每日风险检测任务执行失败", e);
            
            // 更新执行记录为失败
            executionLog.setEndTime(LocalDateTime.now());
            executionLog.setStatus("FAILED");
            executionLog.setErrorMessage(e.getMessage());
            executionLog.setDurationMs(
                java.time.Duration.between(executionLog.getStartTime(), executionLog.getEndTime()).toMillis()
            );
            executionLogRepository.save(executionLog);
        }
    }
    
    /**
     * 每周日凌晨 3 点清理 30 天前的已读通知
     * cron 表达式：秒 分 时 日 月 周（0 = 周日）
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    @Transactional
    public void cleanupOldNotifications() {
        log.info("开始清理旧通知...");
        
        // 创建执行记录
        ScheduleExecutionLog executionLog = new ScheduleExecutionLog();
        executionLog.setTaskName("cleanupOldNotifications");
        executionLog.setStartTime(LocalDateTime.now());
        executionLog.setStatus("RUNNING");
        executionLog = executionLogRepository.save(executionLog);
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            
            // 删除 30 天前的已读通知
            int deletedCount = notificationRepository.deleteByIsReadTrueAndCreatedAtBefore(cutoffDate);
            
            log.info("通知清理完成，共删除 {} 条已读通知（创建时间早于 {}）", deletedCount, cutoffDate);
            
            // 更新执行记录
            executionLog.setEndTime(LocalDateTime.now());
            executionLog.setStatus("SUCCESS");
            executionLog.setNotificationCount(deletedCount);
            executionLog.setDurationMs(
                java.time.Duration.between(executionLog.getStartTime(), executionLog.getEndTime()).toMillis()
            );
            executionLogRepository.save(executionLog);
            
        } catch (Exception e) {
            log.error("通知清理任务执行失败", e);
            
            // 更新执行记录为失败
            executionLog.setEndTime(LocalDateTime.now());
            executionLog.setStatus("FAILED");
            executionLog.setErrorMessage(e.getMessage());
            executionLog.setDurationMs(
                java.time.Duration.between(executionLog.getStartTime(), executionLog.getEndTime()).toMillis()
            );
            executionLogRepository.save(executionLog);
        }
    }
}
