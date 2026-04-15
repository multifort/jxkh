# 架构设计文档

本目录包含系统架构设计相关的文档。

## 📂 文档清单

### 架构设计文档

| 文档 | 说明 | 大小 |
|------|------|------|
| [system-architecture.md](./system-architecture.md) | 系统架构设计（分层架构、模块划分、部署架构） | 12.5KB |
| [domain-model-detail.md](./domain-model-detail.md) | 领域模型设计（实体、值对象、聚合根、领域服务） | 18.0KB |
| [tech-stack.md](./tech-stack.md) | 技术栈详细设计（前端、后端、数据库、DevOps） | 11.2KB |
| [security-design.md](./security-design.md) | 权限与安全设计（认证、授权、数据安全、应急响应） | 20.8KB |

### 交互序列图

| 文档 | 说明 | 业务场景 |
|------|------|----------|
| [sequences/README.md](./sequences/README.md) | 序列图索引和阅读指南 | - |
| [sequences/auth-sequence.md](./sequences/auth-sequence.md) | 用户认证流程 | 登录、Token 刷新、退出 |
| [sequences/plan-create-sequence.md](./sequences/plan-create-sequence.md) | 绩效计划创建 | 指标选择、权重校验、提交审批 |
| [sequences/plan-approval-sequence.md](./sequences/plan-approval-sequence.md) | 绩效审批流程 | 主管审批、通知员工 |
| [sequences/evaluation-sequence.md](./sequences/evaluation-sequence.md) | 绩效评估流程 | 自评、上级评、分数计算 |
| [sequences/calibration-sequence.md](./sequences/calibration-sequence.md) | 绩效校准流程 | HR 校准、强制分布调整 |
| [sequences/progress-tracking-sequence.md](./sequences/progress-tracking-sequence.md) | 进度跟踪流程 | 周报填报、AI 总结、风险预警 |
| [sequences/dashboard-query-sequence.md](./sequences/dashboard-query-sequence.md) | 数据看板查询 | 数据聚合、缓存策略、图表渲染 |

## 📖 阅读指南

### 按角色阅读

- **架构师**: system-architecture.md → domain-model-detail.md → tech-stack.md
- **后端开发**: domain-model-detail.md → security-design.md → tech-stack.md
- **前端开发**: tech-stack.md → security-design.md
- **运维人员**: system-architecture.md → tech-stack.md → security-design.md

### 按阶段阅读

1. **系统设计阶段**: system-architecture.md + domain-model-detail.md
2. **技术选型阶段**: tech-stack.md
3. **安全加固阶段**: security-design.md
4. **开发实现阶段**: sequences/（参考核心业务流程序列图）

### 序列图使用场景

- **后端开发**: 理解业务逻辑和异常处理，实现 Service 层
- **前端开发**: 了解 API 调用顺序和数据流转，实现页面交互
- **测试人员**: 根据序列图编写集成测试用例
- **新人培训**: 快速掌握系统核心业务流程

---

[回到文档根目录](../README.md)
