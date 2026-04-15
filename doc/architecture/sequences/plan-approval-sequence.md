# 绩效审批流程序列图

## 📋 业务场景

描述主管审批员工提交的绩效计划，包括查看详情、审批通过/驳回、通知员工等流程。

## 👥 参与者定义

| 参与者 | 缩写 | 说明 |
|--------|------|------|
| 主管 | Manager | 审批人 |
| 前端应用 | FE | React 前端应用 |
| 计划控制器 | PlanController | API 端点 |
| 计划服务 | PlanService | 计划业务逻辑 |
| 通知服务 | NotificationService | 发送通知 |
| 数据库 | DB | MySQL |

---

## 🔄 主流程：审批通过

```mermaid
sequenceDiagram
    autonumber
    participant Mgr as 主管
    participant FE as 前端应用
    participant PC as PlanController
    participant PS as PlanService
    participant NS as NotificationService
    participant DB as 数据库

    Note over Mgr,DB: 阶段1: 查看待审批列表
    Mgr->>FE: 进入"待我审批"页面
    activate FE
    
    FE->>PC: GET /api/plans?status=PENDING_APPROVE&managerId={managerId}
    activate PC
    PC->>PS: getPendingPlans(managerId)
    activate PS
    
    PS->>DB: SELECT pp.* FROM performance_plans pp<br/>JOIN users u ON pp.user_id = u.id<br/>WHERE u.manager_id = ? AND pp.status = 'PENDING_APPROVE'
    activate DB
    DB-->>PS: 返回待审批计划列表
    deactivate DB
    
    PS-->>PC: 返回 List<PerformancePlan>
    deactivate PS
    PC-->>FE: 200 {plans, total}
    deactivate PC
    
    FE->>Mgr: 显示待审批列表（姓名、部门、提交时间）
    
    Note over Mgr,DB: 阶段2: 查看计划详情
    Mgr->>FE: 点击某个计划
    FE->>PC: GET /api/plans/{planId}
    activate PC
    PC->>PS: getPlanDetail(planId)
    activate PS
    
    PS->>DB: SELECT * FROM performance_plans WHERE id = ?
    activate DB
    DB-->>PS: 返回计划基本信息
    deactivate DB
    
    PS->>DB: SELECT * FROM indicator_instances WHERE plan_id = ?
    activate DB
    DB-->>PS: 返回指标实例列表
    deactivate DB
    
    PS->>DB: SELECT u.* FROM users u WHERE u.id = (SELECT manager_id FROM users WHERE id = ?)
    activate DB
    DB-->>PS: 返回员工信息（含主管）
    deactivate DB
    
    PS-->>PC: 返回 PlanDetailDTO
    deactivate PS
    PC-->>FE: 200 {plan, indicators, employee}
    deactivate PC
    
    FE->>Mgr: 显示计划详情（指标列表、目标值、权重）
    
    Note over Mgr,DB: 阶段3: 审批决策
    Mgr->>FE: 点击"审批"按钮
    FE->>FE: 打开审批对话框
    
    Mgr->>FE: 选择"通过"或"驳回"
    Mgr->>FE: 填写审批意见
    
    alt 审批通过
        Mgr->>FE: 选择"通过"，输入意见"同意"
        FE->>PC: POST /api/plans/{planId}/approve
        activate PC
        Note right of PC: {approved: true, comment: "同意"}
        
        PC->>PS: approvePlan(planId, true, comment)
        activate PS
        
        PS->>DB: BEGIN TRANSACTION
        activate DB
        
        PS->>DB: SELECT * FROM performance_plans WHERE id = ? FOR UPDATE
        DB-->>PS: 返回计划对象
        
        alt 计划状态不是 PENDING_APPROVE
            PS-->>PC: 抛出 IllegalStateException
            PC-->>FE: 409 {message: "计划状态已变更"}
            FE->>Mgr: 刷新页面
        else 状态正确
            PS->>DB: UPDATE performance_plans<br/>SET status = 'IN_PROGRESS',<br/>approved_at = NOW(),<br/>approved_by = ?<br/>WHERE id = ?
            DB-->>PS: 更新成功
            
            Note over PS:NS: 通知员工审批结果
            PS->>NS: notifyEmployee(userId, planId, "approved")
            activate NS
            NS->>DB: INSERT INTO notifications<br/>(user_id, type, title, content)<br/>VALUES (?, 'SYSTEM', '绩效计划已通过', ...)
            activate DB
            DB-->>NS: OK
            deactivate DB
            NS-->>PS: 通知发送成功
            deactivate NS
            
            PS->>DB: COMMIT
            DB-->>PS: 事务提交成功
            deactivate DB
            
            PS-->>PC: 返回 success
            deactivate PS
            
            PC-->>FE: 200 {message: "审批通过"}
            deactivate PC
            
            FE->>Mgr: 显示成功提示
            FE->>FE: 从待审批列表移除该计划
        end
        
    else 审批驳回
        Mgr->>FE: 选择"驳回"，输入原因"指标设置不合理"
        FE->>PC: POST /api/plans/{planId}/approve
        activate PC
        Note right of PC: {approved: false, comment: "指标设置不合理"}
        
        PC->>PS: approvePlan(planId, false, comment)
        activate PS
        
        PS->>DB: BEGIN TRANSACTION
        activate DB
        
        PS->>DB: UPDATE performance_plans<br/>SET status = 'DRAFT',<br/>approved_at = NOW(),<br/>approved_by = ?<br/>WHERE id = ?
        DB-->>PS: 更新成功
        
        Note over PS:NS: 通知员工驳回原因
        PS->>NS: notifyEmployee(userId, planId, "rejected", comment)
        activate NS
        NS->>DB: INSERT INTO notifications<br/>(user_id, type, title, content)<br/>VALUES (?, 'SYSTEM', '绩效计划已驳回', '原因：指标设置不合理')
        activate DB
        DB-->>NS: OK
        deactivate DB
        NS-->>PS: 通知发送成功
        deactivate NS
        
        PS->>DB: COMMIT
        DB-->>PS: 事务提交成功
        deactivate DB
        
        PS-->>PC: 返回 success
        deactivate PS
        
        PC-->>FE: 200 {message: "已驳回"}
        deactivate PC
        
        FE->>Mgr: 显示成功提示
        FE->>FE: 从待审批列表移除该计划
    end
    deactivate FE
```

---

## 💡 技术实现要点

### 后端实现

**审批服务**：
```java
@Service
@Transactional
public class PlanService {
    
    public void approvePlan(Long planId, boolean approved, String comment) {
        PerformancePlan plan = planRepository.findByIdWithLock(planId)
                .orElseThrow(() -> new NotFoundException("计划不存在"));
        
        // 状态检查
        if (plan.getStatus() != PlanStatus.PENDING_APPROVE) {
            throw new IllegalStateException("只有待审批状态的计划可以审批");
        }
        
        User currentUser = SecurityUtils.getCurrentUser();
        
        if (approved) {
            // 审批通过
            plan.setStatus(PlanStatus.IN_PROGRESS);
            plan.setApprovedAt(LocalDateTime.now());
            plan.setApprovedBy(currentUser.getId());
            
            // 通知员工
            notificationService.sendSystemNotification(
                plan.getUserId(),
                "绩效计划已通过",
                "您的绩效计划已审批通过，请开始执行"
            );
        } else {
            // 审批驳回
            plan.setStatus(PlanStatus.DRAFT);
            plan.setApprovedAt(LocalDateTime.now());
            plan.setApprovedBy(currentUser.getId());
            
            // 通知员工驳回原因
            notificationService.sendSystemNotification(
                plan.getUserId(),
                "绩效计划已驳回",
                "驳回原因：" + comment
            );
        }
        
        planRepository.save(plan);
    }
}
```

---

## 🔗 相关文档

- [API 接口设计 - 绩效计划审批](../../api/api-design.md#65-审批绩效计划)
- [领域模型设计 - 状态流转](../domain-model-detail.md#34-performanceplan绩效计划)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 架构团队
