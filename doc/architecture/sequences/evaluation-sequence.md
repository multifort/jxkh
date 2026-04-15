# 绩效评估流程序列图

## 📋 业务场景

描述绩效评估的完整流程，包括员工自评、上级评分、系统自动计算总分和等级。

## 👥 参与者定义

| 参与者 | 缩写 | 说明 |
|--------|------|------|
| 员工 | Employee | 被评估人 |
| 主管 | Manager | 评估人 |
| 前端应用 | FE | React 前端应用 |
| 评分控制器 | ScoreController | API 端点 |
| 评分服务 | ScoreService | 评分业务逻辑 |
| 计算引擎 | ScoreCalcEngine | 分数计算 |
| 通知服务 | NotificationService | 发送通知 |
| 数据库 | DB | MySQL |

---

## 🔄 主流程：自评 + 上级评

```mermaid
sequenceDiagram
    autonumber
    participant Emp as 员工
    participant Mgr as 主管
    participant FE as 前端应用
    participant SC as ScoreController
    participant SS as ScoreService
    participant SCE as ScoreCalcEngine
    participant NS as NotificationService
    participant DB as 数据库

    Note over Emp,DB: 阶段1: 员工自评
    Emp->>FE: 进入"我的绩效"页面
    activate FE
    
    FE->>SC: GET /api/plans/{planId}
    activate SC
    SC->>SS: getPlanDetail(planId)
    activate SS
    SS->>DB: 查询计划和指标
    activate DB
    DB-->>SS: 返回计划详情
    deactivate DB
    SS-->>SC: 返回 PlanDetail
    deactivate SS
    SC-->>FE: 200 {plan, indicators}
    deactivate SC
    
    alt 计划状态不是 PENDING_EVAL
        FE->>Emp: 提示"当前不可评估"
    else 状态正确
        FE->>Emp: 显示自评表单（指标列表、输入框）
        
        Emp->>FE: 填写自评分数和评语
        loop 每个指标
            Emp->>FE: 输入分数（0-100）和评语
        end
        
        Emp->>FE: 点击"提交自评"
        FE->>SC: POST /api/scores
        activate SC
        Note right of SC: {planId, scoreType: "SELF",<br/>dimensions: [{dimension, score, comment}]}
        
        SC->>SS: submitScore(scoreRequest)
        activate SS
        
        SS->>DB: INSERT INTO scores (plan_id, evaluator_id, score_type, score_value, ...)
        activate DB
        Note right of DB: score_type = 'SELF'
        DB-->>SS: OK
        deactivate DB
        
        SS-->>SC: 返回 success
        deactivate SS
        SC-->>FE: 201 {message: "自评提交成功"}
        deactivate SC
        
        FE->>Emp: 显示成功提示
        FE->>FE: 更新状态为"等待上级评分"
    end
    deactivate FE
    
    Note over Mgr,DB: 阶段2: 上级评分
    Mgr->>FE: 收到通知"有待评分的员工"
    activate Mgr
    Mgr->>FE: 进入"待我评分"页面
    
    FE->>SC: GET /api/scores/pending?evaluatorId={managerId}
    activate SC
    SC->>SS: getPendingScores(managerId)
    activate SS
    SS->>DB: 查询待评分计划
    activate DB
    DB-->>SS: 返回待评分列表
    deactivate DB
    SS-->>SC: 返回 List<PendingScore>
    deactivate SS
    SC-->>FE: 200 {pendingScores}
    deactivate SC
    
    FE->>Mgr: 显示待评分员工列表
    
    Mgr->>FE: 点击某个员工
    FE->>SC: GET /api/plans/{planId}
    activate SC
    SC->>SS: getPlanDetailWithScores(planId)
    activate SS
    SS->>DB: 查询计划、指标、自评结果
    activate DB
    DB-->>SS: 返回完整信息
    deactivate DB
    SS-->>SC: 返回 PlanDetail with SelfScore
    deactivate SS
    SC-->>FE: 200 {plan, indicators, selfScore}
    deactivate SC
    
    FE->>Mgr: 显示评分页面<br/>（员工自评、指标详情、评分表单）
    
    Mgr->>FE: 查看员工自评内容
    FE->>Mgr: 显示自评分数和评语
    
    Mgr->>FE: 填写上级评分
    loop 每个维度
        Mgr->>FE: 输入维度分数和评语
        Note right of FE: 维度：工作质量、工作效率、团队协作等
    end
    
    Mgr->>FE: 点击"提交评分"
    FE->>SC: POST /api/scores
    activate SC
    Note right of SC: {planId, scoreType: "MANAGER",<br/>dimensions: [...], comment: "..."}
    
    SC->>SS: submitScore(scoreRequest)
    activate SS
    
    SS->>DB: BEGIN TRANSACTION
    activate DB
    
    SS->>DB: INSERT INTO scores (plan_id, evaluator_id, score_type, score_value, dimensions, ...)
    DB-->>SS: OK
    
    Note over SS,SCE: 阶段3: 计算总分
    SS->>SCE: calculateTotalScore(planId)
    activate SCE
    
    SCE->>DB: SELECT * FROM indicator_instances WHERE plan_id = ?
    activate DB
    DB-->>SCE: 返回指标实例列表
    deactivate DB
    
    SCE->>SCE: 计算加权总分
    Note right of SCE: totalScore = Σ(indicatorScore * weight / 100)
    
    SCE->>SCE: 确定绩效等级
    Note right of SCE: A: >=90, B: >=80, C: >=70, D: <70
    
    SCE-->>SS: 返回 {totalScore, level}
    deactivate SCE
    
    SS->>DB: UPDATE performance_plans<br/>SET total_score = ?, final_level = ?,<br/>status = 'EVALUATED', evaluated_at = NOW()<br/>WHERE id = ?
    DB-->>SS: 更新成功
    
    Note over SS:NS: 检查是否所有评分完成
    SS->>DB: SELECT COUNT(*) FROM scores WHERE plan_id = ?<br/>AND score_type IN ('SELF', 'MANAGER')
    DB-->>SS: 返回 count
    
    alt 自评和上级评都完成
        SS->>NS: notifyEmployee(userId, "评估完成")
        activate NS
        NS->>DB: INSERT INTO notifications (...)
        activate DB
        DB-->>NS: OK
        deactivate DB
        NS-->>SS: 通知发送成功
        deactivate NS
    end
    
    SS->>DB: COMMIT
    DB-->>SS: 事务提交成功
    deactivate DB
    
    SS-->>SC: 返回 {totalScore, level}
    deactivate SS
    
    SC-->>FE: 201 {message: "评分提交成功", totalScore, level}
    deactivate SC
    
    FE->>Mgr: 显示成功提示和最终分数
    FE->>FE: 从待评分列表移除
    deactivate FE
    deactivate Mgr
```

---

## 💡 技术实现要点

### 评分计算引擎

```java
@Component
public class ScoreCalculationEngine {
    
    public ScoreResult calculateTotalScore(Long planId) {
        List<IndicatorInstance> instances = instanceRepository.findByPlanId(planId);
        
        // 计算加权总分
        BigDecimal totalScore = instances.stream()
                .map(instance -> {
                    BigDecimal score = instance.getScore() != null 
                        ? instance.getScore() 
                        : BigDecimal.ZERO;
                    return score.multiply(instance.getWeight())
                               .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 确定等级
        PerformanceLevel level = determineLevel(totalScore);
        
        return new ScoreResult(totalScore, level);
    }
    
    private PerformanceLevel determineLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("90")) >= 0) {
            return PerformanceLevel.A;
        } else if (score.compareTo(new BigDecimal("80")) >= 0) {
            return PerformanceLevel.B;
        } else if (score.compareTo(new BigDecimal("70")) >= 0) {
            return PerformanceLevel.C;
        } else {
            return PerformanceLevel.D;
        }
    }
}
```

---

## 🔗 相关文档

- [API 接口设计 - 绩效评估](../../api/api-design.md#9-绩效评估接口)
- [领域模型设计 - Score](../domain-model-detail.md#37-score评分)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 架构团队
