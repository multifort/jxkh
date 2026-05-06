# Sprint 6 开发准备清单

**准备日期**: 2026-05-06  
**Sprint 周期**: 第 14-15 周（绩效校准与强制分布）  
**前置条件**: Sprint 5 已完成并通过测试

---

## ✅ Sprint 5 完成情况检查

### 功能完成状态
- [x] ScoreService - 评分服务层
- [x] ScoreCalculationEngine - 分数计算引擎
- [x] SelfEvaluationPage - 员工自评页面
- [x] ManagerEvaluationPage - 主管评分页面
- [x] ScoreSummaryPage - 评分汇总页面
- [x] 自动化测试（27个测试用例）
- [x] 测试数据脚本（V16迁移）

### 技术债务处理
- [x] P1-32: ScoreService 并发控制缺失 ✅ 已修复
- [ ] P1-33: ScoreService N+1 查询优化 ⚠️ 用户撤销
- [x] P1-34: 前端富文本编辑器内存泄漏修复 ✅ 已修复

### 代码质量
- [x] 单元测试通过率: 100% (52/52)
- [x] Code Review 完成
- [x] API 文档更新
- [x] 技术债务清单更新

---

## 📋 Sprint 6 任务清单

### 核心功能：绩效校准与强制分布

#### 任务 6.1: 校准工作台后端开发

**需求**:
- HR 可以查看所有员工的评分结果
- 支持拖拽调整绩效等级
- 强制分布规则校验（如：A级≤10%, B级≤30%等）
- 批量保存调整结果

**技术方案**:
- 后端：CalibrationService + CalibrationController
- 数据库：新增 calibration_records 表
- 算法：强制分布校验算法

**开发步骤**:
1. [ ] 创建 CalibrationRecord 实体
2. [ ] 创建 CalibrationRepository
3. [ ] 实现 CalibrationService（含强制分布校验）
4. [ ] 实现 CalibrationController（REST API）
5. [ ] 编写单元测试（覆盖率 ≥ 85%）

**API 设计**:
```http
GET  /api/v1/calibrations?cycleId={id}          # 获取校准数据
POST /api/v1/calibrations/{id}/adjust           # 调整等级
POST /api/v1/calibrations/{id}/validate         # 校验分布规则
PUT  /api/v1/calibrations/{id}/batch-save       # 批量保存
```

**验收标准**:
- ✅ 强制分布规则生效
- ✅ 违规调整被拒绝
- ✅ 批量保存事务一致

---

#### 任务 6.2: 校准工作台前段开发

**需求**:
- 表格展示所有员工评分结果
- 拖拽调整绩效等级
- 实时显示分布比例
- 违规提示和阻止提交

**技术方案**:
- React + Ant Design Table
- react-beautiful-dnd（拖拽库）
- 实时计算分布比例

**开发步骤**:
1. [ ] 创建 CalibrationPage 组件
2. [ ] 实现表格展示（含当前等级、建议等级）
3. [ ] 集成拖拽功能
4. [ ] 实现分布比例实时计算
5. [ ] 添加违规提示和阻止逻辑

**关键代码**:
```tsx
// 分布比例计算
const distribution = employees.reduce((acc, emp) => {
  acc[emp.calibratedLevel] = (acc[emp.calibratedLevel] || 0) + 1;
  return acc;
}, {});

// 违规检测
const isViolation = Object.entries(distribution).some(([level, count]) => {
  const percentage = count / employees.length;
  return percentage > MAX_PERCENTAGE[level];
});
```

**验收标准**:
- ✅ 拖拽流畅，无卡顿
- ✅ 分布比例实时更新
- ✅ 违规时禁止提交

---

#### 任务 6.3: 强制分布规则配置

**需求**:
- 支持不同周期设置不同的分布规则
- 规则存储在数据库，可动态修改
- 默认规则：A≤10%, B≤30%, C≤50%, D≤10%

**技术方案**:
- 数据库：distribution_rules 表
- 后端：DistributionRuleService
- 前端：规则配置页面

**开发步骤**:
1. [ ] 创建 DistributionRule 实体
2. [ ] 实现规则 CRUD API
3. [ ] 创建规则配置页面
4. [ ] 校准工作时自动加载规则

**验收标准**:
- ✅ 规则可配置
- ✅ 校准时自动应用规则
- ✅ 规则变更不影响历史数据

---

#### 任务 6.4: 校准历史记录

**需求**:
- 记录每次调整的详细信息
- 支持查看调整历史
- 支持回滚到之前的版本

**技术方案**:
- 数据库：calibration_history 表
- 后端：CalibrationHistoryService
- 前端：历史记录查看对话框

**开发步骤**:
1. [ ] 创建 CalibrationHistory 实体
2. [ ] 每次调整自动记录历史
3. [ ] 实现历史查询 API
4. [ ] 前端展示历史记录
5. [ ] 实现回滚功能

**验收标准**:
- ✅ 每次调整都有记录
- ✅ 可查看完整历史
- ✅ 支持回滚操作

---

### 测试任务

#### 任务 6.5: 单元测试

**测试范围**:
- CalibrationService（强制分布校验）
- DistributionRuleService（规则管理）
- CalibrationHistoryService（历史记录）

**目标**:
- 单元测试覆盖率 ≥ 85%
- 所有测试用例通过

---

#### 任务 6.6: 集成测试

**测试场景**:
1. 完整校准流程
   - HR 查看评分 → 拖拽调整 → 校验规则 → 批量保存
   
2. 强制分布校验
   - 正常调整（符合规则）
   - 违规调整（超出比例）
   
3. 历史记录
   - 调整记录生成
   - 历史查询
   - 回滚操作

**目标**:
- 集成测试通过率 100%
- 无阻塞性 Bug

---

## 🎯 Sprint 6 交付物

### 后端
- [ ] CalibrationService.java
- [ ] CalibrationController.java
- [ ] DistributionRuleService.java
- [ ] CalibrationHistoryService.java
- [ ] 单元测试（≥ 15个测试用例）

### 前端
- [ ] CalibrationPage.tsx
- [ ] DistributionRuleConfigPage.tsx
- [ ] CalibrationHistoryModal.tsx
- [ ] 拖拽组件封装

### 数据库
- [ ] V19__create_calibration_tables.sql
  - calibration_records
  - distribution_rules
  - calibration_history

### 文档
- [ ] API 文档更新（Swagger）
- [ ] 校准功能使用说明
- [ ] 强制分布规则配置指南

---

## ⚠️ 风险与应对

### 风险 1: 拖拽性能问题

**风险等级**: 中  
**影响**: 大量员工时拖拽卡顿

**应对措施**:
1. ✅ 使用虚拟滚动（react-window）
2. ✅ 防抖处理（debounce 300ms）
3. ✅ 懒加载员工数据

---

### 风险 2: 强制分布规则复杂

**风险等级**: 低  
**影响**: 不同部门可能有不同规则

**应对措施**:
1. ✅ 规则配置化，支持自定义
2. ✅ 支持按组织/周期设置不同规则
3. ✅ 提供默认规则模板

---

### 风险 3: 数据一致性

**风险等级**: 中  
**影响**: 批量保存时部分失败

**应对措施**:
1. ✅ 使用事务保证原子性
2. ✅ 失败时全部回滚
3. ✅ 详细的错误提示

---

## 📅 时间安排

| 任务 | 预计工时 | 负责人 | 开始时间 | 结束时间 |
|------|---------|--------|---------|---------|
| 6.1 校准工作台后端 | 3天 | Backend | Week 14 Day 1 | Week 14 Day 3 |
| 6.2 校准工作台前段 | 3天 | Frontend | Week 14 Day 2 | Week 14 Day 4 |
| 6.3 规则配置 | 2天 | Full-stack | Week 14 Day 4 | Week 15 Day 1 |
| 6.4 历史记录 | 2天 | Full-stack | Week 15 Day 1 | Week 15 Day 2 |
| 6.5 单元测试 | 1天 | All | Week 15 Day 2 | Week 15 Day 3 |
| 6.6 集成测试 | 1天 | All | Week 15 Day 3 | Week 15 Day 4 |
| **缓冲时间** | **1天** | - | Week 15 Day 5 | Week 15 Day 5 |

---

## 🔗 相关文档

- [SPRINT-PLAN-ADJUSTMENT-V3.md](./SPRINT-PLAN-ADJUSTMENT-V3.md) - Sprint 计划调整方案
- [TECHNICAL-DEBT.md](./TECHNICAL-DEBT.md) - 技术债务清单
- [SPRINT-DETAILS.md](./SPRINT-DETAILS.md) - Sprint 详细任务分解
- [development-plan.md](./development-plan.md) - 项目开发执行计划书

---

## ✅ 准备工作检查清单

### 开发环境
- [ ] 本地数据库已更新到最新版本（Flyway 迁移）
- [ ] 后端服务可正常启动
- [ ] 前端服务可正常运行
- [ ] Docker 容器运行正常

### 代码仓库
- [ ] develop 分支最新代码已拉取
- [ ] 从 develop 创建 feature/calibration 分支
- [ ] CI/CD 流水线配置正常

### 团队协作
- [ ] Sprint 6 任务分配完成
- [ ] 每日站会时间确认
- [ ] Code Review 人员安排

### 测试数据
- [ ] 准备校准测试数据（至少50个员工）
- [ ] 准备不同分布规则的测试场景
- [ ] 准备边界条件测试数据

---

**最后更新**: 2026-05-06  
**维护人**: JXKH Team
