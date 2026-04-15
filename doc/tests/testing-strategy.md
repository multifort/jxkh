# 测试策略总纲

## 📋 概述

本文档定义绩效管理系统的整体测试策略，包括测试原则、覆盖率目标、执行流程和质量管理标准。

---

## 🎯 测试金字塔模型

```
        /\
       /E2E\          端到端测试 (10%)
      /------\
     /Integration\    集成测试 (20%)
    /--------------\
   /    Unit Tests   \  单元测试 (70%)
  /------------------\
```

### 各层级说明

| 层级 | 占比 | 速度 | 成本 | 覆盖范围 |
|------|------|------|------|---------|
| **单元测试** | 70% | 快（毫秒级） | 低 | 单个类/方法 |
| **集成测试** | 20% | 中（秒级） | 中 | 模块间交互 |
| **E2E 测试** | 10% | 慢（分钟级） | 高 | 完整业务流程 |

---

## 📊 测试覆盖率目标

### 代码覆盖率要求

| 模块类型 | 最低覆盖率 | 目标覆盖率 | 考核标准 |
|---------|-----------|----------|---------|
| Service 层 | 70% | 85% | 分支覆盖率 ≥ 60% |
| Repository 层 | 60% | 80% | SQL 语句全覆盖 |
| Controller 层 | 50% | 70% | API 端点全覆盖 |
| Utility 工具类 | 80% | 95% | 边界条件全覆盖 |
| 前端组件 | 60% | 80% | 用户交互全覆盖 |
| 前端 Hooks | 70% | 90% | 状态变化全覆盖 |

### 覆盖率计算规则

- **行覆盖率**：执行的代码行数 / 总代码行数
- **分支覆盖率**：执行的分支数 / 总分支数
- **方法覆盖率**：执行的方法数 / 总方法数

**注意**：Getter/Setter、DTO、配置类等简单代码可排除在覆盖率统计外。

---

## 🧪 测试分类与职责

### 1. 单元测试（Unit Tests）

**目标**：验证单个类或方法的正确性

**测试对象**：
- Service 业务逻辑
- Utility 工具方法
- Domain 领域对象
- Validator 验证器

**特点**：
- ✅ 独立运行，无外部依赖
- ✅ 使用 Mock 隔离依赖
- ✅ 执行速度快（< 100ms）
- ✅ 每个测试只验证一个场景

**示例**：
```java
@Test
void should_calculateTotalScore_when_allIndicatorsProvided() {
    // Arrange
    List<Indicator> indicators = createTestIndicators();
    
    // Act
    BigDecimal totalScore = scoreCalculator.calculate(indicators);
    
    // Assert
    assertEquals(new BigDecimal("85.5"), totalScore);
}
```

---

### 2. 集成测试（Integration Tests）

**目标**：验证多个组件协同工作的正确性

**测试对象**：
- Controller + Service + Repository 完整链路
- 数据库交互（JPA Query）
- Redis 缓存操作
- 外部 API 调用

**特点**：
- ✅ 使用真实数据库（H2 / TestContainers）
- ✅ 测试事务边界
- ✅ 验证数据持久化
- ✅ 执行速度中等（1-5 秒）

**示例**：
```java
@SpringBootTest
@AutoConfigureMockMvc
class PlanControllerIntegrationTest {
    
    @Test
    void should_createPlan_when_validRequest() throws Exception {
        mockMvc.perform(post("/api/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.planId").exists());
    }
}
```

---

### 3. E2E 测试（End-to-End Tests）

**目标**：验证完整业务流程的用户体验

**测试对象**：
- 关键用户旅程（User Journey）
- 跨页面交互
- 浏览器兼容性

**特点**：
- ✅ 模拟真实用户操作
- ✅ 覆盖 UI 渲染和交互
- ✅ 执行速度慢（10-60 秒）
- ✅ 数量少但价值高

**示例**：
```typescript
test('should complete performance plan workflow', async () => {
  await page.goto('/login');
  await page.fill('#username', 'employee01');
  await page.fill('#password', 'password123');
  await page.click('button[type="submit"]');
  
  await page.click('text=创建绩效计划');
  await page.fill('#indicator-name', '销售额');
  await page.fill('#target-value', '1000000');
  await page.click('text=提交');
  
  await expect(page.locator('.success-message')).toBeVisible();
});
```

---

## 🔒 测试环境策略

### 环境隔离

| 环境 | 用途 | 数据源 | 触发条件 |
|------|------|--------|---------|
| **本地开发** | 开发调试 | H2 内存数据库 | 手动执行 |
| **CI 测试** | 持续集成 | TestContainers | Git Push / PR |
| **预发布** | 回归测试 | 脱敏生产数据 | 发布前 |
| **生产监控** | 健康检查 | 真实数据 | 定时任务 |

### 数据隔离原则

1. **每个测试用例独立数据**：避免测试间相互影响
2. **事务自动回滚**：测试结束后清理数据
3. **唯一标识符**：使用时间戳或 UUID 避免冲突
4. **并行安全**：支持多线程并行执行测试

---

## ⚡ 测试执行策略

### 执行频率

| 测试类型 | 执行时机 | 超时限制 | 失败处理 |
|---------|---------|---------|---------|
| 单元测试 | 每次代码保存 | 5 分钟 | 阻止提交 |
| 集成测试 | Git Push / PR | 15 分钟 | 阻止合并 |
| E2E 测试 | 每日夜间 / 发布前 | 30 分钟 | 通知团队 |
| 性能测试 | 每周 / 重大变更后 | 60 分钟 | 生成报告 |

### 并行执行

**后端**：
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

**前端**：
```json
// vitest.config.ts
export default defineConfig({
  test: {
    threads: true,
    maxThreads: 4,
  }
});
```

---

## 🐛 缺陷管理流程

### 缺陷分类

| 级别 | 说明 | 响应时间 | 修复时限 |
|------|------|---------|---------|
| **P0 - 致命** | 系统崩溃、数据丢失 | 立即 | 24 小时 |
| **P1 - 严重** | 核心功能不可用 | 2 小时 | 3 天 |
| **P2 - 一般** | 次要功能异常 | 1 天 | 1 周 |
| **P3 - 轻微** | UI 瑕疵、文案错误 | 3 天 | 下个版本 |

### 回归测试策略

1. **自动化回归**：CI 自动执行全部测试用例
2. **手动回归**：QA 验证关键业务流程
3. **选择性回归**：仅测试受影响模块（基于代码变更分析）

---

## 📈 质量门禁（Quality Gate）

### CI/CD 门禁规则

| 检查项 | 阈值 | 失败后果 |
|--------|------|---------|
| 单元测试通过率 | 100% | 阻止合并 |
| 代码覆盖率 | ≥ 70% | 警告但不阻止 |
| 集成测试通过率 | 100% | 阻止合并 |
| 代码异味（SonarQube） | ≤ 5 个 Blocker | 阻止合并 |
| 重复代码率 | ≤ 3% | 警告 |
| 安全漏洞 | 0 个 Critical | 阻止合并 |

### 代码审查清单

- [ ] 新增代码有对应的单元测试
- [ ] 测试命名清晰（should_xxx_when_yyy）
- [ ] 覆盖了正常和异常场景
- [ ] 没有硬编码的测试数据
- [ ] Mock 使用合理，不过度 Mock

---

## 🛠️ 测试工具链

### 后端工具

| 工具 | 用途 | 版本 |
|------|------|------|
| JUnit 5 | 单元测试框架 | 5.10.x |
| Mockito | Mock 框架 | 5.10.x |
| AssertJ | 流式断言库 | 3.25.x |
| TestContainers | 容器化集成测试 | 1.19.x |
| WireMock | HTTP API Mock | 3.4.x |
| Jacoco | 代码覆盖率 | 0.8.11 |

### 前端工具

| 工具 | 用途 | 版本 |
|------|------|------|
| Vitest | 单元测试框架 | 1.2.x |
| React Testing Library | 组件测试 | 14.2.x |
| MSW | API Mock | 2.1.x |
| Playwright | E2E 测试 | 1.41.x |
| Cypress | E2E 测试（备选） | 13.6.x |

### 基础设施

| 工具 | 用途 |
|------|------|
| GitHub Actions | CI/CD 自动化 |
| SonarQube | 代码质量分析 |
| Codecov | 覆盖率可视化 |
| Allure | 测试报告 |

---

## 📚 相关文档

- [后端测试指南](./backend-testing-guide.md)
- [前端测试指南](./frontend-testing-guide.md)
- [测试数据管理](./test-data-management.md)
- [CI/CD 集成配置](./ci-cd-integration.md)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: QA 团队 & 技术负责人
