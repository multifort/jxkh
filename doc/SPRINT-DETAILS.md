# Sprint 详细任务分解（补充）

本文档补充 development-plan.md 中拆分后的 Sprint 详细任务。

---

## Sprint 3a: 绩效计划创建（第 6-7 周）⭐ P0

**对应设计文档**：
- [plan-create-sequence.md](./architecture/sequences/plan-create-sequence.md)
- [domain-model-detail.md](./architecture/domain-model-detail.md#34-performanceplan绩效计划)

**目标**：实现绩效计划的创建、草稿保存和权重校验

### 任务 3a.1: 绩效计划实体与 Repository

**开发步骤**：
1. [ ] 创建 `PerformancePlan` 实体（含状态字段）
2. [ ] 创建 `IndicatorInstance` 实体（指标实例）
3. [ ] 创建 `PerformancePlanRepository`
4. [ ] 创建 `IndicatorInstanceRepository`
5. [ ] 编写单元测试

**验收标准**：
- ✅ 实体映射正确
- ✅ Repository CRUD 测试通过

---

### 任务 3a.2: 计划创建 Service

**需求**：
- 从指标库选择指标
- 设置目标值和权重
- 权重总和校验（必须=100%）
- 保存草稿

**开发步骤**：
1. [ ] 实现 `PlanService.create()` 方法
2. [ ] 实现权重校验逻辑（`WeightValidator`）
3. [ ] 实现草稿保存功能
4. [ ] 添加事务控制（@Transactional）
5. [ ] 编写单元测试（覆盖率 ≥ 85%）

**关键代码**：
```java
@Transactional
public Long create(PlanCreateRequest request) {
    // 1. 校验权重总和
    validateWeight(request.getIndicators());
    
    // 2. 检查是否已存在计划
    checkDuplicate(request.getUserId(), request.getCycleId());
    
    // 3. 创建计划实体
    PerformancePlan plan = convertToEntity(request);
    plan.setStatus(PlanStatus.DRAFT);
    
    // 4. 创建指标实例
    List<IndicatorInstance> instances = createIndicatorInstances(plan, request.getIndicators());
    
    // 5. 保存
    PerformancePlan saved = planRepository.save(plan);
    indicatorInstanceRepository.saveAll(instances);
    
    return saved.getId();
}
```

**验收标准**：
- ✅ 权重校验生效（≠100% 时拒绝）
- ✅ 重复计划检测生效
- ✅ 事务回滚测试通过

---

### 任务 3a.3: 计划创建 Controller

**API 设计**：
```http
POST /api/v1/plans              # 创建计划
GET  /api/v1/plans?cycleId={id} # 查询列表
GET  /api/v1/plans/{id}         # 查询详情
PUT  /api/v1/plans/{id}         # 更新草稿
```

**开发步骤**：
1. [ ] 实现 `PlanController`
2. [ ] 添加 Swagger 注解
3. [ ] 参数校验（@Valid）
4. [ ] 统一异常处理

**验收标准**：
- ✅ API 文档自动生成
- ✅ 参数校验正常工作
- ✅ 异常返回统一格式

---

### 任务 3a.4: 前端计划创建向导

**需求**：
- 多步骤表单（Steps）
- 指标选择器（支持搜索、多选）
- 权重实时校验
- 草稿自动保存

**开发步骤**：
1. [ ] 创建 `PlanCreatePage` 组件
2. [ ] 实现 Steps 向导（选择周期 → 选择指标 → 设置目标 → 确认提交）
3. [ ] 实现指标选择器（Ant Design Select + 搜索）
4. [ ] 实现权重实时计算和提示
5. [ ] 实现草稿自动保存（防抖 2s）

**关键代码**：
```tsx
// 权重实时校验
const totalWeight = indicators.reduce((sum, item) => sum + item.weight, 0);
const isWeightValid = Math.abs(totalWeight - 100) < 0.01;

// 草稿自动保存
useEffect(() => {
  const timer = setTimeout(() => {
    if (hasChanges) {
      saveDraft();
    }
  }, 2000);
  return () => clearTimeout(timer);
}, [indicators]);
```

**验收标准**：
- ✅ 权重总和不等于100%时禁止提交
- ✅ 草稿自动保存成功
- ✅ 页面刷新后可恢复草稿

---

**Sprint 3a 交付物**：
- ✅ 绩效计划创建功能
- ✅ 权重校验逻辑
- ✅ 草稿保存功能
- ✅ 前端向导页面

---

## Sprint 3b: 审批流程与状态机（第 8 周）⭐ P0

**对应设计文档**：
- [plan-approval-sequence.md](./architecture/sequences/plan-approval-sequence.md)

**目标**：实现计划提交、审批流程和状态机控制

### 任务 3b.1: 状态机实现

**需求**：
- 严格控制计划状态流转
- 防止非法状态变更

**状态流转图**：
```
DRAFT → PENDING_SUBMIT → PENDING_APPROVE → IN_PROGRESS → 
PENDING_EVAL → EVALUATED → CALIBRATED → ARCHIVED
```

**开发步骤**：
1. [ ] 创建 `PlanStatus` 枚举
2. [ ] 实现 `PlanStateMachine` 服务
3. [ ] 定义状态转换规则（Map）
4. [ ] 所有状态变更必须通过状态机

**关键代码**：
```java
@Component
public class PlanStateMachine {
    
    private static final Map<PlanStatus, Set<PlanStatus>> TRANSITIONS = Map.of(
        PlanStatus.DRAFT, Set.of(PlanStatus.PENDING_SUBMIT),
        PlanStatus.PENDING_SUBMIT, Set.of(PlanStatus.DRAFT, PlanStatus.PENDING_APPROVE),
        PlanStatus.PENDING_APPROVE, Set.of(PlanStatus.DRAFT, PlanStatus.IN_PROGRESS),
        // ...
    );
    
    public void transition(PerformancePlan plan, PlanStatus targetStatus) {
        PlanStatus currentStatus = plan.getStatus();
        
        if (!TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new BusinessException("PLAN_STATUS_INVALID", 
                String.format("不允许从 %s 转换到 %s", currentStatus, targetStatus));
        }
        
        plan.setStatus(targetStatus);
        planRepository.save(plan);
    }
}
```

**验收标准**：
- ✅ 非法状态转换抛出异常
- ✅ 所有合法转换测试通过

---

### 任务 3b.2: 提交审批

**API 设计**：
```http
POST /api/v1/plans/{id}/submit  # 员工提交审批
```

**开发步骤**：
1. [ ] 实现 `PlanService.submit()` 方法
2. [ ] 调用状态机转换状态
3. [ ] 发送通知给主管
4. [ ] 前端"提交"按钮

**验收标准**：
- ✅ 提交后状态变为 PENDING_APPROVE
- ✅ 主管收到通知

---

### 任务 3b.3: 主管审批

**API 设计**：
```http
GET  /api/v1/plans?status=PENDING_APPROVE&managerId={id}  # 待审批列表
POST /api/v1/plans/{id}/approve?approved=true&comment=xxx  # 审批
```

**开发步骤**：
1. [ ] 实现 `PlanService.approve()` 方法
2. [ ] 审批通过：状态 → IN_PROGRESS
3. [ ] 审批驳回：状态 → DRAFT
4. [ ] 发送通知给员工
5. [ ] 前端待审批列表页面
6. [ ] 前端审批对话框

**验收标准**：
- ✅ 审批通过后状态变为 IN_PROGRESS
- ✅ 审批驳回后状态回到 DRAFT
- ✅ 员工收到通知

---

### 任务 3b.4: 行级锁防止并发

**需求**：
- 防止多人同时审批同一计划
- 防止重复提交

**技术方案**：
- 使用 `SELECT FOR UPDATE` 行级锁
- 或使用乐观锁（version 字段）

**开发步骤**：
1. [ ] 在 `performance_plans` 表添加 `version` 字段
2. [ ] 使用 JPA 乐观锁（@Version）
3. [ ] 处理 `OptimisticLockingFailureException`

**关键代码**：
```java
@Entity
public class PerformancePlan {
    @Version
    private Integer version;
}

@Transactional
public void approve(Long planId, Boolean approved, String comment) {
    try {
        PerformancePlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND"));
        
        // 状态机转换
        PlanStatus targetStatus = approved ? PlanStatus.IN_PROGRESS : PlanStatus.DRAFT;
        stateMachine.transition(plan, targetStatus);
        
        planRepository.save(plan);
    } catch (OptimisticLockingFailureException e) {
        throw new BusinessException("PLAN_CONCURRENT_UPDATE", "计划已被他人修改，请刷新后重试");
    }
}
```

**验收标准**：
- ✅ 并发更新被正确拦截
- ✅ 友好的错误提示

---

**Sprint 3b 交付物**：
- ✅ 状态机控制
- ✅ 提交审批功能
- ✅ 主管审批功能
- ✅ 并发控制

---

## Sprint 4.5: 集成测试（阶段一）（第 10.5 周）

**目标**：验证核心业务流程的端到端集成

### 任务 4.5.1: E2E 测试场景

**测试场景**：
1. 完整绩效计划流程
   - 员工创建计划 → 提交审批 → 主管审批 → 计划生效
   
2. 进度记录流程
   - 员工填写周报 → AI 总结 → 主管查看

3. 权限控制验证
   - 不同角色访问不同资源
   - 数据隔离验证

**开发步骤**：
1. [ ] 编写 Playwright E2E 测试
2. [ ] 配置测试数据工厂
3. [ ] 执行测试并生成报告

**验收标准**：
- ✅ 所有 E2E 测试通过
- ✅ 测试覆盖率报告生成

---

### 任务 4.5.2: 性能基准测试

**测试场景**：
1. 登录接口 QPS
2. 计划列表查询响应时间
3. 数据库连接池性能

**开发步骤**：
1. [ ] 使用 JMeter 编写压测脚本
2. [ ] 执行基准测试
3. [ ] 记录性能指标

**验收标准**：
- ✅ 登录接口 QPS ≥ 100
- ✅ 计划列表查询 P95 < 500ms
- ✅ 无内存泄漏

---

**Sprint 4.5 交付物**：
- ✅ E2E 测试报告
- ✅ 性能基准测试报告
- ✅ 问题清单和优化建议

---

## Sprint 6.5: 集成测试（阶段二）（第 15.5 周）

**目标**：验证完整业务流程和回归测试

### 任务 6.5.1: 完整业务流程测试

**测试场景**：
1. 完整绩效周期流程
   - 创建周期 → 创建计划 → 进度跟踪 → 绩效评估 → 校准 → 归档

2. 360度评估流程
   - 自评 → 上级评 → 同事评 → 分数计算

3. 强制分布校准
   - HR 校准 → 分布校验 → 调整等级

**验收标准**：
- ✅ 所有业务流程测试通过
- ✅ 无阻塞性 Bug

---

### 任务 6.5.2: 回归测试

**测试范围**：
- Sprint 1-6 的所有功能
- 重点测试修改过的模块

**验收标准**：
- ✅ 回归测试通过率 100%
- ✅ 无新增 Bug

---

**Sprint 6.5 交付物**：
- ✅ 完整业务流程测试报告
- ✅ 回归测试报告
- ✅ Bug 修复清单

---

## Sprint 7.5: UAT 用户验收测试（第 17.5 周）

**目标**：真实用户场景测试，收集反馈

### 任务 7.5.1: UAT 测试准备

**准备工作**：
1. [ ] 准备 UAT 环境（独立数据库）
2. [ ] 准备测试数据（真实场景数据）
3. [ ] 编写 UAT 测试用例
4. [ ] 培训参与测试的用户

**验收标准**：
- ✅ UAT 环境就绪
- ✅ 测试用例覆盖核心场景

---

### 任务 7.5.2: UAT 执行

**参与人员**：
- HR 代表（2-3人）
- 部门主管（3-5人）
- 普通员工（5-10人）

**测试周期**：3-5 天

**测试内容**：
1. 日常操作流程
2. 边界场景测试
3. 用户体验反馈
4. 性能体验反馈

**验收标准**：
- ✅ UAT 通过率 ≥ 95%
- ✅ 收集至少 10 条有效反馈
- ✅ 严重 Bug 全部修复

---

**Sprint 7.5 交付物**：
- ✅ UAT 测试报告
- ✅ 用户反馈清单
- ✅ Bug 修复记录

---

## Sprint 8.5: 生产环境部署（第 19.5 周）⭐ P0

**目标**：平稳上线，确保系统稳定运行

### 任务 8.5.1: 生产环境配置

**配置内容**：
1. [ ] 生产数据库配置（主从复制）
2. [ ] Redis 集群配置
3. [ ] JWT 密钥更换为强随机密钥
4. [ ] HTTPS 证书配置
5. [ ] 域名和 DNS 配置

**验收标准**：
- ✅ 所有配置项检查通过
- ✅ 安全扫描无高危漏洞

---

### 任务 8.5.2: 监控告警配置

**监控指标**：
1. [ ] 应用健康检查（/actuator/health）
2. [ ] JVM 内存和 GC 监控
3. [ ] 数据库连接池监控
4. [ ] API 响应时间监控
5. [ ] 错误率监控

**告警规则**：
- CPU > 80% 持续 5 分钟 → 警告
- 错误率 > 5% → 严重
- 响应时间 P95 > 2s → 警告

**验收标准**：
- ✅ 所有监控指标正常采集
- ✅ 告警通知正常发送

---

### 任务 8.5.3: 灰度发布

**发布策略**：
1. 第 1 天：内部员工试用（10%流量）
2. 第 2-3 天：小范围部门试用（30%流量）
3. 第 4-7 天：全量发布（100%流量）

**回滚预案**：
- 发现严重 Bug → 立即回滚到上一版本
- 数据备份 → 每小时一次

**验收标准**：
- ✅ 系统稳定运行 24 小时
- ✅ 无严重 Bug
- ✅ 用户反馈良好

---

**Sprint 8.5 交付物**：
- ✅ 生产环境部署完成
- ✅ 监控告警系统运行
- ✅ 灰度发布成功
- ✅ 运维手册交付

---

## 附录：Sprint 调整对比

| 项目 | 原计划 | 调整后 | 说明 |
|------|--------|--------|------|
| 总周期 | 18 周 | 19.5 周 | +1.5 周缓冲 |
| Sprint 0 | 1 周 | 0a + 0b | 降低初期风险 |
| Sprint 3 | 3 周 | 3a (2周) + 3b (1周) | 核心功能保护 |
| 集成测试 | 无 | 4.5 + 6.5 | 提前发现问题 |
| UAT | 无 | 7.5 (1周) | 确保符合业务需求 |
| 生产部署 | 包含在 S8 | 8.5 (0.5周) | 独立部署阶段 |

**调整理由**：
1. **降低风险**：基础设施分两阶段，避免一次性投入过大
2. **核心保护**：Sprint 3 是核心枢纽，单独保护确保质量
3. **提前发现问题**：集成测试周可提前发现模块间集成问题
4. **用户导向**：UAT 确保系统符合真实业务需求
5. **平稳上线**：独立的生产部署阶段，确保万无一失

---

**文档版本**: V2.0  
**最后更新**: 2026-04-15  
**维护者**: JXKH Team
