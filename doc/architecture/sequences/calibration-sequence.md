# 绩效校准流程序列图

## 📋 业务场景

描述HR进行绩效校准的流程，包括查看部门分布、拖拽调整等级、强制分布校验。

## 👥 参与者定义

| 参与者 | 缩写 | 说明 |
|--------|------|------|
| HR管理员 | HR | 校准人 |
| 前端应用 | FE | React 前端应用（含拖拽组件） |
| 校准控制器 | CalibrationController | API 端点 |
| 校准服务 | CalibrationService | 校准业务逻辑 |
| 分布检查器 | DistributionChecker | 强制分布校验 |
| 数据库 | DB | MySQL |

---

## 🔄 主流程：绩效校准

```mermaid
sequenceDiagram
    autonumber
    participant HR as HR管理员
    participant FE as 前端应用
    participant CC as CalibrationController
    participant CS as CalibrationService
    participant DC as DistributionChecker
    participant DB as 数据库

    Note over HR,DB: 阶段1: 查看部门绩效分布
    HR->>FE: 进入"绩效校准"页面
    activate FE
    
    FE->>CC: GET /api/calibrations?cycleId={cycleId}&orgId={orgId}
    activate CC
    CC->>CS: getCalibrationData(cycleId, orgId)
    activate CS
    
    CS->>DB: SELECT pp.*, u.name, u.employee_no<br/>FROM performance_plans pp<br/>JOIN users u ON pp.user_id = u.id<br/>WHERE pp.cycle_id = ? AND pp.org_id = ?<br/>AND pp.status = 'EVALUATED'
    activate DB
    DB-->>CS: 返回已评估计划列表
    deactivate DB
    
    CS->>CS: 统计等级分布
    Note right of CS: distribution = {A: 15, B: 60, C: 20, D: 5}
    
    CS->>DC: checkDistribution(distribution, totalEmployees)
    activate DC
    DC->>DC: 计算当前比例
    Note right of DC: currentRatio = {A: 15%, B: 60%, C: 20%, D: 5%}
    
    DC->>DC: 对比目标分布
    Note right of DC: targetRatio = {A: 20%, B: 70%, C: 10%, D: 0%}
    
    DC-->>CS: 返回 {compliant: false, adjustments: [...]}
    deactivate DC
    
    CS-->>CC: 返回 CalibrationData
    deactivate CS
    CC-->>FE: 200 {plans, distribution, suggestions}
    deactivate CC
    
    FE->>HR: 显示校准工作台<br/>（左侧：等级分布图，右侧：人员列表）
    
    Note over HR,DB: 阶段2: 拖拽调整等级
    HR->>FE: 拖拽员工卡片到不同等级区域
    FE->>FE: 更新本地状态
    FE->>FE: 实时计算新分布
    
    loop 每次拖拽
        FE->>FE: 计算新的等级分布
        FE->>FE: 检查是否符合强制分布
        
        alt 不符合分布
            FE->>HR: 红色提示"还需调整X人到A级"
            FE->>FE: 禁用"提交校准"按钮
        else 符合分布
            FE->>HR: 绿色提示"分布符合要求"
            FE->>FE: 启用"提交校准"按钮
        end
    end
    
    Note over HR,DB: 阶段3: 填写校准原因并提交
    HR->>FE: 点击"提交校准"
    FE->>FE: 打开确认对话框
    
    HR->>FE: 为每个调整的员工填写原因
    loop 每个调整的员工
        HR->>FE: 输入调整原因
    end
    
    HR->>FE: 确认提交
    FE->>CC: POST /api/calibrations
    activate CC
    Note right of CC: {cycleId, orgId,<br/>adjustments: [{planId, afterScore, afterLevel, reason}]}
    
    CC->>CS: calibrate(calibrationRequest)
    activate CS
    
    CS->>DB: BEGIN TRANSACTION
    activate DB
    
    CS->>DC: validateDistribution(adjustments)
    activate DC
    DC->>DC: 重新计算分布
    DC->>DC: 检查是否符合规则
    
    alt 分布不符合要求
        DC-->>CS: 抛出 BusinessException
        CS-->>CC: 返回错误
        CC-->>FE: 422 {message: "强制分布校验失败"}
        FE->>HR: 显示错误，要求继续调整
        CS->>DB: ROLLBACK
        deactivate DB
    else 分布符合要求
        DC-->>CS: 验证通过
        deactivate DC
        
        loop 每个调整项
            CS->>DB: SELECT * FROM performance_plans WHERE id = ? FOR UPDATE
            DB-->>CS: 返回计划对象
            
            CS->>DB: INSERT INTO calibrations<br/>(cycle_id, org_id, plan_id,<br/>before_score, after_score,<br/>before_level, after_level,<br/>adjust_reason, calibrated_by)<br/>VALUES (...)
            DB-->>CS: OK
            
            CS->>DB: UPDATE performance_plans<br/>SET total_score = ?, final_level = ?,<br/>status = 'CALIBRATED', calibrated_at = NOW()<br/>WHERE id = ?
            DB-->>CS: 更新成功
        end
        
        CS->>DB: COMMIT
        DB-->>CS: 事务提交成功
        deactivate DB
        
        CS-->>CC: 返回 success
        deactivate CS
        
        CC-->>FE: 201 {message: "校准完成"}
        deactivate CC
        
        FE->>HR: 显示成功提示
        FE->>FE: 跳转到校准结果页
    end
    deactivate FE
```

---

## 💡 技术实现要点

### 强制分布检查器

```java
@Component
public class DistributionChecker {
    
    private static final Map<PerformanceLevel, BigDecimal> TARGET_RATIO = Map.of(
        PerformanceLevel.A, new BigDecimal("0.20"),
        PerformanceLevel.B, new BigDecimal("0.70"),
        PerformanceLevel.C, new BigDecimal("0.10"),
        PerformanceLevel.D, BigDecimal.ZERO
    );
    
    public DistributionResult checkDistribution(Map<PerformanceLevel, Integer> actual, int total) {
        Map<PerformanceLevel, BigDecimal> actualRatio = new HashMap<>();
        
        for (Map.Entry<PerformanceLevel, Integer> entry : actual.entrySet()) {
            BigDecimal ratio = new BigDecimal(entry.getValue())
                    .divide(new BigDecimal(total), 4, RoundingMode.HALF_UP);
            actualRatio.put(entry.getKey(), ratio);
        }
        
        boolean compliant = true;
        List<String> violations = new ArrayList<>();
        
        for (Map.Entry<PerformanceLevel, BigDecimal> target : TARGET_RATIO.entrySet()) {
            BigDecimal actual = actualRatio.getOrDefault(target.getKey(), BigDecimal.ZERO);
            BigDecimal diff = actual.subtract(target.getValue()).abs();
            
            // 允许5%的误差
            if (diff.compareTo(new BigDecimal("0.05")) > 0) {
                compliant = false;
                violations.add(String.format("%s级: 实际%.1f%%, 目标%.1f%%",
                    target.getKey(), actual.multiply(new BigDecimal("100")),
                    target.getValue().multiply(new BigDecimal("100"))));
            }
        }
        
        return new DistributionResult(compliant, violations);
    }
}
```

---

## 🔗 相关文档

- [API 接口设计 - 绩效校准](../../api/api-design.md#10-绩效校准接口)
- [领域模型设计 - Calibration](../domain-model-detail.md#38-calibration绩效校准)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 架构团队
