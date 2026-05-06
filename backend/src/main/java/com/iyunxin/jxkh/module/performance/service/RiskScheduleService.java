package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.module.performance.domain.IndicatorInstance;
import com.iyunxin.jxkh.module.performance.domain.PerformancePlan;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import com.iyunxin.jxkh.module.performance.repository.PerformancePlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    
    /**
     * 每天凌晨 2 点执行风险检测
     * cron 表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyRiskDetection() {
        log.info("开始执行每日风险检测任务...");
        
        try {
            // 查询所有执行中的计划
            List<PerformancePlan> activePlans = planRepository.findByStatusAndIsDeletedFalse(PlanStatus.IN_PROGRESS);
            
            log.info("检测到 {} 个执行中的计划", activePlans.size());
            
            int totalRisks = 0;
            
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
                }
            }
            
            log.info("每日风险检测完成，共发现 {} 个风险指标", totalRisks);
            
        } catch (Exception e) {
            log.error("每日风险检测任务执行失败", e);
        }
    }
}
