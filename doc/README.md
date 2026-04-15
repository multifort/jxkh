# 绩效管理系统 - 设计文档索引

## 📚 文档概览

本目录包含绩效管理系统的完整设计文档，作为后续开发的权威依据。

---

## 📖 文档清单

### 1. 架构设计（doc/architecture/）

#### 1.1 [技术栈详细设计](./architecture/tech-stack.md)
- 前端技术栈（React、TypeScript、Ant Design 等）
- 后端技术栈（Spring Boot、JPA、Redis 等）
- 数据库技术栈（MySQL、ClickHouse 等）
- DevOps 技术栈（Docker、Kubernetes 等）
- 技术选型理由和版本兼容性

**适用人群**：技术负责人、架构师、开发人员

---

#### 1.2 [系统架构设计](./architecture/system-architecture.md)
- 总体架构图
- 分层架构设计（表现层、业务层、数据层）
- 核心模块设计（认证、权限、绩效引擎、数据分析）
- 集成层设计（外部系统、AI 能力）
- 安全架构
- 性能优化策略
- 部署架构（开发、测试、生产环境）

**适用人群**：架构师、技术负责人、运维人员

---

#### 1.3 [领域模型设计](./architecture/domain-model-detail.md)
- 核心领域模型（组织域、用户域、绩效域、分析域）
- 实体定义和关系
- 领域服务和业务规则
- 领域事件
- 仓储接口
- 状态机设计

**适用人群**：后端开发人员、领域专家

---

#### 1.4 [权限与安全设计](./architecture/security-design.md)
- 认证机制（JWT、密码安全、登录安全）
- 授权机制（RBAC 模型、功能权限、数据权限）
- API 安全（HTTPS、CORS、限流、输入验证）
- 数据安全（加密、脱敏、备份、归档）
- 日志与审计
- 网络安全
- 应急响应流程
- 合规性要求

**适用人群**：安全工程师、后端开发人员、运维人员

---

#### 1.5 [交互序列图](./architecture/sequences/README.md)
- 用户认证流程（登录、Token 刷新、退出）
- 绩效计划创建流程（指标选择、权重校验、提交审批）
- 绩效审批流程（主管审批、通知员工）
- 绩效评估流程（自评、上级评、分数计算）
- 绩效校准流程（HR 校准、强制分布调整）
- 进度跟踪流程（周报填报、AI 总结、风险预警）
- 数据看板查询流程（数据聚合、缓存策略、图表渲染）

**适用人群**：所有开发人员、测试人员、新人培训

---

### 2. 数据库设计（doc/database/）

#### 2.1 [数据库 schema 设计](./database/schema-design.md)
- 数据库概述和命名规范
- 核心数据表设计（20+ 张表）
  - 组织管理模块（orgs）
  - 用户管理模块（users、roles、permissions）
  - 绩效管理模块（cycles、plans、indicators、scores 等）
  - 系统配置模块（scoring_models、level_mappings）
  - 通知模块（notifications）
  - 审计日志模块（operation_logs）
- 视图设计
- 存储过程和触发器
- 索引优化建议
- 数据归档策略
- 备份方案

**适用人群**：DBA、后端开发人员

---

### 3. API 接口设计（doc/api/）

#### 3.1 [API 接口设计](./api/api-design.md)
- 接口规范（RESTful、统一响应格式、分页、认证）
- 认证授权接口（登录、刷新 Token、退出）
- 用户管理接口（CRUD、批量导入）
- 组织管理接口（组织树、CRUD）
- 绩效周期接口（创建、查询、启动、结束）
- 绩效计划接口（CRUD、提交、审批）
- 指标管理接口（CRUD、分类管理）
- 进度跟踪接口（记录、进度更新）
- 绩效评估接口（评分、360 评价）
- 绩效校准接口（校准、建议）
- 数据分析接口（看板、报表、趋势）
- 通知接口
- 系统配置接口
- AI 功能接口（总结、评分建议、风险预警）
- 错误码定义
- 接口限流规则

**适用人群**：前端开发人员、后端开发人员、测试人员

---

### 4. 功能模块设计（doc/functional/）

#### 4.1 [功能模块详细设计](./functional/module-design.md)
- 首页/看板模块（Dashboard）
  - 概览卡片
  - 趋势图、分布图
  - 待办事项、风险预警
  - 部门排名、Top Performer
  
- 绩效计划管理模块
  - 计划列表、详情
  - 创建/编辑流程
  - 状态流转规则
  
- 指标管理模块
  - 指标库管理
  - 指标分类
  - 指标模板
  
- 进度跟踪模块
  - 周报/月报填报
  - 进度更新
  - 团队进度监控
  
- 绩效评估模块
  - 自评、上级评、360 评价
  - 评分页面设计
  - AI 评分建议
  
- 绩效校准模块
  - 校准工作台
  - 强制分布控制
  - 智能建议
  
- 数据分析模块
  - 绩效报表
  - 趋势分析
  - 部门对比
  - 个人画像
  
- 组织管理模块
- 系统配置模块
- 个人中心模块
- 移动端适配
- 国际化支持

**适用人群**：产品经理、UI/UX 设计师、前端开发人员

---

### 5. 测试文档（doc/tests/）

#### 5.1 [测试策略总纲](./tests/testing-strategy.md)
- 测试金字塔模型（单元 70%、集成 20%、E2E 10%）
- 测试覆盖率目标和质量门禁
- 测试环境策略和数据隔离
- 缺陷管理流程和回归测试
- 测试工具链和基础设施

**适用人群**：技术负责人、QA 经理、全体开发人员

---

#### 5.2 [后端测试指南](./tests/backend-testing-guide.md)
- Service 层单元测试（Mockito、断言）
- Repository 层测试（H2、TestContainers）
- Controller 层集成测试（MockMvc）
- 性能测试（JMH 基准测试）
- 测试最佳实践和常见陷阱

**适用人群**：后端开发人员

---

#### 5.3 [前端测试指南](./tests/frontend-testing-guide.md)
- 组件测试（React Testing Library）
- Hooks 测试（renderHook）
- API Mock（MSW）
- E2E 测试（Playwright）
- 测试最佳实践和常见陷阱

**适用人群**：前端开发人员

---

#### 5.4 [测试数据管理](./tests/test-data-management.md)
- TestDataFactory 模式
- Fixture 文件管理
- 数据库测试数据初始化
- 数据清理策略（事务回滚、@AfterEach）
- 边界数据和异常数据

**适用人群**：全体开发人员

---

#### 5.5 [CI/CD 集成配置](./tests/ci-cd-integration.md)
- GitHub Actions 工作流配置
- Maven/Vitest 测试配置
- 质量门禁（SonarQube、Codecov）
- 测试报告（Allure、Jacoco）
- 监控和告警

**适用人群**：DevOps、技术负责人

---

### 6. 原始设计参考（doc/）

#### 6.1 [系统设计说明书](./design.md)
- 项目背景与目标
- 系统总体架构
- 用户角色设计
- 核心业务流程
- 核心功能模块概述
- 数据模型概览
- 状态机设计
- 权限模型设计
- API 接口概览
- 评分计算引擎
- AI 能力设计

**适用人群**：所有项目成员（入门必读）

---

## 🗺️ 文档使用指南

### 按角色阅读

#### 👨‍💼 项目经理
1. [系统设计说明书](./design.md) - 了解项目全貌
2. [功能模块详细设计](./functional/module-design.md) - 掌握功能细节
3. [系统架构设计](./architecture/system-architecture.md) - 理解技术方案

#### 👨‍💻 前端开发
1. [API 接口设计](./api/api-design.md) - 接口对接
2. [功能模块详细设计](./functional/module-design.md) - 页面实现
3. [技术栈详细设计](./architecture/tech-stack.md) - 技术选型

#### 👨‍💻 后端开发
1. [领域模型设计](./architecture/domain-model-detail.md) - 业务逻辑
2. [数据库 schema 设计](./database/schema-design.md) - 数据持久化
3. [API 接口设计](./api/api-design.md) - 接口实现
4. [权限与安全设计](./architecture/security-design.md) - 安全控制

#### 🗄️ DBA
1. [数据库 schema 设计](./database/schema-design.md) - 建库建表
2. [系统架构设计](./architecture/system-architecture.md) - 部署架构

#### 🔒 安全工程师
1. [权限与安全设计](./architecture/security-design.md) - 安全加固
2. [API 接口设计](./api/api-design.md) - 接口安全

#### 🎨 UI/UX 设计师
1. [功能模块详细设计](./functional/module-design.md) - 页面设计
2. [系统设计说明书](./design.md) - 业务流程

#### 🧪 测试工程师
1. [测试策略总纲](./tests/testing-strategy.md) - 了解测试体系
2. [API 接口设计](./api/api-design.md) - 接口测试
3. [功能模块详细设计](./functional/module-design.md) - 功能测试
4. [后端测试指南](./tests/backend-testing-guide.md) / [前端测试指南](./tests/frontend-testing-guide.md) - 编写测试用例
5. [CI/CD 集成配置](./tests/ci-cd-integration.md) - 配置自动化测试

---

## 📋 开发阶段文档引用

### 阶段 1：需求分析
- [系统设计说明书](./design.md)
- [功能模块详细设计](./functional/module-design.md)

### 阶段 2：系统设计
- [系统架构设计](./architecture/system-architecture.md)
- [领域模型设计](./architecture/domain-model-detail.md)
- [数据库 schema 设计](./database/schema-design.md)
- [API 接口设计](./api/api-design.md)

### 阶段 3：技术选型
- [技术栈详细设计](./architecture/tech-stack.md)

### 阶段 4：开发实现
- 前端：[API 接口设计](./api/api-design.md) + [功能模块详细设计](./functional/module-design.md) + [序列图](./architecture/sequences/README.md)
- 后端：[领域模型设计](./architecture/domain-model-detail.md) + [数据库 schema 设计](./database/schema-design.md) + [API 接口设计](./api/api-design.md) + [序列图](./architecture/sequences/README.md)

### 阶段 5：安全加固
- [权限与安全设计](./architecture/security-design.md)

### 阶段 6：测试验收
- [测试策略总纲](./tests/testing-strategy.md)
- [后端测试指南](./tests/backend-testing-guide.md) / [前端测试指南](./tests/frontend-testing-guide.md)
- [CI/CD 集成配置](./tests/ci-cd-integration.md)

---

## 🔄 文档维护

### 版本管理
- 所有文档采用版本号管理（V1.0、V1.1...）
- 重大变更升级主版本号
- 小幅修改升级次版本号

### 变更记录
每次更新需在文档末尾记录：
```markdown
**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**更新内容**: 
- 初始版本创建
**维护者**: XXX
```

### 评审流程
1. 文档编写完成后需经过同行评审
2. 重大设计变更需召开设计评审会议
3. 评审通过后合并到主分支

---

## 📞 联系方式

如有疑问或需要补充文档，请联系：

- **技术负责人**：[姓名] - [邮箱]
- **架构师**：[姓名] - [邮箱]
- **产品经理**：[姓名] - [邮箱]

---

## 📅 文档更新时间线

| 日期 | 文档 | 版本 | 说明 |
|------|------|------|------|
| 2026-04-14 | tests/* | V1.0 | 新增完整测试文档体系（5个文档） |
| 2026-04-14 | architecture/sequences/* | V1.0 | 新增 7 个核心业务流程序列图 |
| 2026-04-14 | 所有文档 | V2.0 | 文档合并优化，移除冲突文件 |
| 2026-04-14 | 所有文档 | V1.0 | 初始版本创建 |

---

## ✨ 后续规划

### V1.5 计划补充
- 性能测试报告
- 压力测试方案
- 监控告警配置手册

### V2.0 计划补充
- 微服务拆分方案
- 多租户设计文档
- 高可用架构设计

---

**最后更新**: 2026-04-14  
**维护团队**: 绩效管理系统项目组
