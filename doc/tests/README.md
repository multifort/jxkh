# 测试文档

本目录包含测试相关的文档和指南。

## 📂 文档清单

| 文档 | 说明 | 大小 |
|------|------|------|
| [testing-strategy.md](./testing-strategy.md) | 测试策略总纲（金字塔模型、覆盖率目标、质量门禁） | 8.2KB |
| [backend-testing-guide.md](./backend-testing-guide.md) | 后端测试详细指南（单元、集成、性能测试） | 14.5KB |
| [frontend-testing-guide.md](./frontend-testing-guide.md) | 前端测试详细指南（组件、Hooks、E2E 测试） | 15.0KB |
| [test-data-management.md](./test-data-management.md) | 测试数据管理（工厂模式、Fixture、数据清理） | 11.8KB |
| [ci-cd-integration.md](./ci-cd-integration.md) | CI/CD 集成配置（GitHub Actions、质量门禁、报告） | 12.6KB |

---

## 📖 快速开始

### 按角色阅读

**后端开发**：
1. [testing-strategy.md](./testing-strategy.md) - 了解测试策略
2. [backend-testing-guide.md](./backend-testing-guide.md) - 学习测试规范
3. [test-data-management.md](./test-data-management.md) - 掌握数据管理
4. [ci-cd-integration.md](./ci-cd-integration.md) - 配置自动化测试

**前端开发**：
1. [testing-strategy.md](./testing-strategy.md) - 了解测试策略
2. [frontend-testing-guide.md](./frontend-testing-guide.md) - 学习测试规范
3. [test-data-management.md](./test-data-management.md) - 掌握数据管理
4. [ci-cd-integration.md](./ci-cd-integration.md) - 配置自动化测试

**QA / 测试工程师**：
1. [testing-strategy.md](./testing-strategy.md) - 完整测试策略
2. 所有文档 - 全面了解测试体系

**技术负责人**：
1. [testing-strategy.md](./testing-strategy.md) - 制定测试标准
2. [ci-cd-integration.md](./ci-cd-integration.md) - 配置质量门禁

## 📂 测试结构

```
jxkh/
├── backend/
│   └── tests/
│       ├── unit/          # 后端单元测试
│       └── integration/   # 后端集成测试
└── frontend/
    └── src/
        └── __tests__/     # 前端测试
            ├── components/  # 组件测试
            ├── services/    # 服务测试
            └── pages/       # 页面测试
```

## 🧪 测试分类

### 后端测试

| 类型 | 位置 | 说明 |
|------|------|------|
| 单元测试 | `backend/tests/unit/` | 测试 Service、Repository 层 |
| 集成测试 | `backend/tests/integration/` | 测试 API 接口、数据库交互 |

**运行命令**：
```bash
cd backend
mvn test                    # 运行所有测试
mvn test -Dtest=UserServiceTest  # 运行指定测试类
```

### 前端测试

| 类型 | 位置 | 说明 |
|------|------|------|
| 组件测试 | `frontend/src/__tests__/components/` | 测试 React 组件 |
| 服务测试 | `frontend/src/__tests__/services/` | 测试 API 调用 |
| 页面测试 | `frontend/src/__tests__/pages/` | 测试页面渲染 |

**运行命令**：
```bash
cd frontend
npm test                   # 运行所有测试
npm test -- --watch       # 监听模式
npm run coverage          # 生成覆盖率报告
```

## 📊 测试覆盖率要求

| 模块 | 最低覆盖率 | 目标覆盖率 |
|------|-----------|----------|
| Service 层 | 70% | 85% |
| Repository 层 | 60% | 80% |
| Controller 层 | 50% | 70% |
| 前端组件 | 60% | 80% |
| 工具函数 | 80% | 95% |

## 🔍 测试最佳实践

### 后端测试

1. **单元测试**
   - 使用 Mockito 模拟依赖
   - 每个测试方法只测试一个场景
   - 遵循 AAA 模式（Arrange-Act-Assert）

2. **集成测试**
   - 使用 @SpringBootTest
   - 使用 H2 内存数据库
   - 测试完整请求响应流程

### 前端测试

1. **组件测试**
   - 使用 React Testing Library
   - 测试用户行为，而非实现细节
   - 使用 jest-dom 匹配器

2. **服务测试**
   - Mock API 响应
   - 测试错误处理
   - 测试加载状态

## 🛠️ 测试工具链

| 工具 | 用途 | 版本 |
|------|------|------|
| JUnit 5 | Java 单元测试框架 | 5.x |
| Mockito | Java Mock 框架 | 5.x |
| Vitest | 前端测试框架 | 1.x |
| React Testing Library | React 组件测试 | 14.x |
| MSW | API Mock | 2.x |

---

[回到文档根目录](../README.md)
