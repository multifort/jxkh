# 进度跟踪流程序列图

## 📋 业务场景

描述员工填报周报、更新指标进度、AI自动总结、风险预警的完整流程。

## 👥 参与者定义

| 参与者 | 缩写 | 说明 |
|--------|------|------|
| 员工 | Employee | 填报人 |
| AI服务 | AIService | LLM 智能总结 |
| 前端应用 | FE | React 前端应用 |
| 记录控制器 | RecordController | API 端点 |
| 记录服务 | RecordService | 记录业务逻辑 |
| 进度服务 | ProgressService | 进度计算和预警 |
| 通知服务 | NotificationService | 发送预警通知 |
| 数据库 | DB | MySQL |

---

## 🔄 主流程：填报周报并更新进度

```mermaid
sequenceDiagram
    autonumber
    participant Emp as 员工
    participant AI as AI服务
    participant FE as 前端应用
    participant RC as RecordController
    participant RS as RecordService
    participant PS as ProgressService
    participant NS as NotificationService
    participant DB as 数据库

    Note over Emp,DB: 阶段1: 填写周报
    Emp->>FE: 进入"进度跟踪"页面
    activate FE
    
    FE->>RC: GET /api/plans/{planId}/indicators
    activate RC
    RC->>DB: 查询指标实例列表
    activate DB
    DB-->>RC: 返回 indicators
    deactivate DB
    RC-->>FE: 200 {indicators}
    deactivate RC
    
    FE->>Emp: 显示周报表单<br/>（工作内容、进度更新、附件上传）
    
    Emp->>FE: 填写本周工作内容
    Note right of FE: 富文本编辑器，支持图片、列表等
    
    Emp->>FE: 更新各指标进度
    loop 每个指标
        Emp->>FE: 输入当前值或进度百分比
        FE->>FE: 实时计算完成率
    end
    
    Emp->>FE: 上传附件（可选）
    FE->>FE: 文件上传到对象存储
    Note right of FE: 返回文件URL列表
    
    Note over Emp,AI: 阶段2: AI 自动总结
    Emp->>FE: 点击"AI 智能总结"
    FE->>AI: POST /api/ai/weekly-summary
    activate AI
    Note right of AI: {workContent, indicators, lastWeekSummary}
    
    AI->>AI: 调用 LLM API
    Note right of AI: Prompt: "根据以下工作内容生成绩效周报总结..."
    
    AI-->>FE: 返回 AI 总结
    deactivate AI
    Note right of AI: {summary, highlights, risks, suggestions}
    
    FE->>Emp: 显示 AI 生成的总结
    Emp->>FE: 编辑和完善总结内容
    
    Note over Emp,DB: 阶段3: 提交周报
    Emp->>FE: 点击"提交周报"
    FE->>RC: POST /api/records
    activate RC
    Note right of RC: {planId, type: "WEEKLY_REPORT",<br/>content, progress, attachments,<br/>aiSummary, recordDate}
    
    RC->>RS: createRecord(recordRequest)
    activate RS
    
    RS->>DB: INSERT INTO performance_records<br/>(plan_id, user_id, type, content,<br/>progress, attachments, ai_summary,<br/>record_date)<br/>VALUES (...)
    activate DB
    DB-->>RS: OK
    deactivate DB
    
    Note over RS,PS: 阶段4: 更新指标进度
    RS->>PS: updateIndicatorProgress(planId, progressData)
    activate PS
    
    loop 每个有更新的指标
        PS->>DB: UPDATE indicator_instances<br/>SET current_value = ?, progress = ?,<br/>status = ?<br/>WHERE id = ?
        activate DB
        
        alt 进度 < 60% 且时间 > 70%
            PS->>PS: 标记为风险
            PS->>DB: UPDATE indicator_instances SET status = 'DELAYED'
            DB-->>PS: OK
        else 正常
            PS->>DB: UPDATE indicator_instances SET status = 'IN_PROGRESS'
            DB-->>PS: OK
        end
        deactivate DB
    end
    
    PS-->>RS: 更新完成
    deactivate PS
    
    Note over RS:NS: 检查风险预警
    RS->>PS: detectRisks(planId)
    activate PS
    PS->>DB: 查询滞后指标
    activate DB
    DB-->>PS: 返回风险列表
    deactivate DB
    
    alt 存在高风险
        PS-->>RS: 返回 List<RiskAlert>
        
        RS->>NS: notifyRisk(employeeId, managerId, risks)
        activate NS
        NS->>DB: INSERT INTO notifications<br/>(user_id, type, title, content)<br/>VALUES (managerId, 'TASK', '绩效风险预警', ...)
        activate DB
        DB-->>NS: OK
        deactivate DB
        NS-->>RS: 通知发送成功
        deactivate NS
    end
    deactivate PS
    
    RS-->>RC: 返回 success
    deactivate RS
    
    RC-->>FE: 201 {message: "周报提交成功"}
    deactivate RC
    
    FE->>Emp: 显示成功提示
    FE->>FE: 刷新进度展示
    deactivate FE
```

---

## 🔀 异常流程：风险预警触发

```mermaid
sequenceDiagram
    autonumber
    participant PS as ProgressService
    participant NS as NotificationService
    participant DB as 数据库

    Note over PS: 定时任务：每天凌晨检查风险
    
    PS->>DB: SELECT ii.*, pp.user_id, u.manager_id<br/>FROM indicator_instances ii<br/>JOIN performance_plans pp ON ii.plan_id = pp.id<br/>JOIN users u ON pp.user_id = u.id<br/>WHERE ii.progress < 60<br/>AND DATEDIFF(NOW(), pp.cycle.start_date) /<br/>DATEDIFF(pp.cycle.end_date, pp.cycle.start_date) > 0.7
    activate DB
    DB-->>PS: 返回风险指标列表
    deactivate DB
    
    loop 每个风险指标
        PS->>NS: sendRiskAlert(userId, managerId, indicator)
        activate NS
        
        NS->>DB: INSERT INTO notifications<br/>(user_id, type, title, content, related_type, related_id)
        activate DB
        Note right of DB: type = 'TASK'<br/>title = '绩效进度滞后预警'<br/>content = '指标XXX进度仅为XX%，请及时跟进'
        DB-->>NS: OK
        deactivate DB
        
        NS-->>PS: 通知发送成功
        deactivate NS
    end
```

---

## 💡 技术实现要点

### AI 总结服务

```java
@Service
public class AISummaryService {
    
    @Autowired
    private OpenAiClient openAiClient;
    
    public WeeklySummary generateSummary(String workContent, List<Indicator> indicators) {
        String prompt = buildPrompt(workContent, indicators);
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4")
                .messages(List.of(
                    new ChatMessage("system", "你是专业的绩效管理助手"),
                    new ChatMessage("user", prompt)
                ))
                .temperature(0.7)
                .maxTokens(500)
                .build();
        
        ChatCompletionResult result = openAiClient.createChatCompletion(request);
        String summary = result.getChoices().get(0).getMessage().getContent();
        
        return parseSummary(summary);
    }
}
```

### 风险检测服务

```java
@Service
public class RiskDetectionService {
    
    public List<RiskAlert> detectRisks(Long planId) {
        PerformancePlan plan = planRepository.findById(planId).orElseThrow();
        LocalDate today = LocalDate.now();
        
        // 计算周期进度
        long totalDays = ChronoUnit.DAYS.between(plan.getCycle().getStartDate(), 
                                                  plan.getCycle().getEndDate());
        long elapsedDays = ChronoUnit.DAYS.between(plan.getCycle().getStartDate(), today);
        double timeProgress = (double) elapsedDays / totalDays;
        
        List<IndicatorInstance> instances = instanceRepository.findByPlanId(planId);
        List<RiskAlert> risks = new ArrayList<>();
        
        for (IndicatorInstance instance : instances) {
            double indicatorProgress = instance.getProgress().doubleValue() / 100;
            
            // 风险判定：进度滞后超过20%
            if (timeProgress - indicatorProgress > 0.2) {
                risks.add(new RiskAlert(
                    instance.getId(),
                    instance.getName(),
                    instance.getProgress(),
                    RiskLevel.HIGH,
                    String.format("进度滞后%.1f%%", (timeProgress - indicatorProgress) * 100)
                ));
            }
        }
        
        return risks;
    }
}
```

---

## 🔗 相关文档

- [API 接口设计 - 进度跟踪](../../api/api-design.md#8-进度跟踪接口)
- [领域模型设计 - PerformanceRecord](../domain-model-detail.md#36-performancerecord绩效记录)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 架构团队
