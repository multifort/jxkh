# 交互序列图文档

本目录包含绩效管理系统核心业务流程的交互序列图，用于指导后续开发和实现。

## 📂 序列图清单

| 序号 | 序列图 | 文件 | 业务场景 | 复杂度 |
|------|--------|------|---------|--------|
| 1 | 用户认证流程 | [auth-sequence.md](./auth-sequence.md) | 用户登录、Token 刷新、退出 | ⭐⭐⭐ |
| 2 | 绩效计划创建 | [plan-create-sequence.md](./plan-create-sequence.md) | 员工创建并提交绩效计划 | ⭐⭐⭐⭐ |
| 3 | 绩效审批流程 | [plan-approval-sequence.md](./plan-approval-sequence.md) | 主管审批绩效计划 | ⭐⭐⭐ |
| 4 | 绩效评估流程 | [evaluation-sequence.md](./evaluation-sequence.md) | 自评、上级评、360评价 | ⭐⭐⭐⭐⭐ |
| 5 | 绩效校准流程 | [calibration-sequence.md](./calibration-sequence.md) | HR 校准和强制分布调整 | ⭐⭐⭐⭐ |
| 6 | 进度跟踪流程 | [progress-tracking-sequence.md](./progress-tracking-sequence.md) | 周报填报、进度更新、风险预警 | ⭐⭐⭐ |
| 7 | 数据看板查询 | [dashboard-query-sequence.md](./dashboard-query-sequence.md) | 管理层查看绩效看板 | ⭐⭐⭐ |

## 📖 阅读指南

### 按开发阶段阅读

**后端开发优先**：
1. auth-sequence.md - 认证授权实现
2. plan-create-sequence.md - 核心业务逻辑
3. evaluation-sequence.md - 评分计算引擎

**前端开发优先**：
1. auth-sequence.md - 登录页面和 Token 管理
2. plan-create-sequence.md - 计划创建表单
3. dashboard-query-sequence.md - 数据可视化

**测试人员**：
1. 所有序列图 - 理解完整业务流程
2. 重点关注异常分支和边界条件

### 序列图说明

每个序列图包含：
- **参与者定义**：明确交互的各个对象
- **主流程**：正常业务路径（Happy Path）
- **异常流程**：错误处理和边界情况
- **关键说明**：技术实现要点和注意事项
- **相关文档**：关联的设计文档和 API 接口

## 🎨 图表规范

- 使用 **Mermaid** 语法绘制序列图
- 参与者命名：使用英文缩写 + 中文说明
- 消息类型：
  - `->>` 同步调用
  - `-->>` 异步返回
  - `->` 单向消息
- 激活框：标注对象活跃状态
- 注释：关键步骤添加 Note 说明

## 🔗 相关文档

- [系统架构设计](../system-architecture.md) - 整体架构
- [领域模型设计](../domain-model-detail.md) - 业务对象
- [API 接口设计](../../api/api-design.md) - 接口定义
- [数据库设计](../../database/schema-design.md) - 数据模型

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 架构团队
