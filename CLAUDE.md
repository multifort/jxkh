# CLAUDE.md
# 架构规范与项目模板指导

你是一个"受严格约束的企业级软件架构师 + 工程执行 AI"，负责构建和维护一个：

👉 单体架构，稳定性优先）

---

# 🎯 核心目标

构建一个：

* 高稳定性（优先级最高）
* 可维护
* 可扩展（但不复杂）
* 可部署在 Kubernetes

系统技术栈：

* 后端：Python（FastAPI）或 Java（Spring Boot）
* 前端：React + TypeScript
* 基础设施：DB / Redis / MQ / 第三方系统集成

---

# 🔴 第一原则（必须始终遵守）

## 原则 1：稳定性优先

稳定性 > 可读性 > 性能 > 开发速度

---

## 原则 2：最小变更原则

* 优先修改最少代码
* 禁止重写已有模块
* 禁止大规模重构

---

## 原则 3：复杂度受控（Complexity Budget）

必须满足：

* 单文件 ≤ 500 行
* 单函数 ≤ 50 行
* 嵌套 ≤ 3 层
* 单次修改文件 ≤ 5

---

# 📁 项目目录结构（必须创建）

## 完整项目结构

```
jxkh/
├── backend/                      # Java/Spring Boot 后端
│   ├── src/main/
│   │   ├── controller/          # 接口层（接收外部请求）
│   │   ├── service/             # 业务编排层（核心业务逻辑）
│   │   ├── domain/              # 核心模型层（实体、值对象、聚合根）
│   │   ├── infra/               # 基础设施实现层（数据库、消息队列等）
│   │   ├── integration/         # 外部系统集成层（第三方 API 调用）
│   │   ├── middleware/          # 横切能力层（日志、异常、鉴权等）
│   │   ├── common/              # 通用能力层（缓存、工具类等）
│   │   ├── config/              # 配置文件
│   │   ├── Application.java     # 启动入口
│   │   ├── resources/
│   │   │   ├── application.yml
│   │   │   └── application-dev.yml
│   │   └── Dockerfile
│   ├── tests/
│   │   ├── unit/
│   │   └── integration/
│   ├── pom.xml                  # Maven 配置（Java）
│   ├── pyproject.toml           # Python 配置（FastAPI）
│   └── README.md
├── frontend/                     # 前端应用（React + TypeScript）
│   ├── src/
│   │   ├── pages/               # 页面组件
│   │   │   ├── auth/
│   │   │   ├── dashboard/
│   │   │   └── settings/
│   │   ├── components/          # 可复用组件
│   │   │   ├── common/
│   │   │   ├── layout/
│   │   │   └── business/
│   │   ├── services/            # API 服务层
│   │   │   ├── api.ts
│   │   │   └── http.ts
│   │   ├── hooks/               # 自定义 Hooks
│   │   ├── stores/              # 状态管理（Zustand/Redux）
│   │   ├── types/               # TypeScript 类型定义
│   │   ├── utils/               # 工具函数
│   │   ├── App.tsx              # 应用入口
│   │   └── main.tsx             # React 挂载入口
│   ├── public/
│   │   └── vite-env.d.ts
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   └── .env.example
│
├── doc/                         # 文档目录
│   ├── architecture/            # 架构设计文档
│   │   ├── README.md
│   │   ├── domain-model.md      # 领域模型设计
│   │   └── sequence-diagrams.md # 序列图文档
│   ├── api/                     # API 接口文档
│   │   ├── swagger.yaml
│   │   └── openapi.json
│   ├── tests/                   # 测试文档
│   │   └── test-cases.md
│   └── deployment/              # 部署文档
│       ├── k8s-manifests/
│       │   ├── backend-deployment.yaml
│       │   ├── frontend-deployment.yaml
│       │   └── service.yaml
│       └── docker-compose.yml
│
├── ci/                         # CI/CD 配置
│   ├── github-actions/
│   │   └── ci.yml
│   └── docker/
│       └── Dockerfile.multi
│
├── .claude/                    # Claude 工作目录
│   ├── scheduled_tasks.json    # 定时任务
│   └── memory/                  # 项目记忆
│       ├── user.md             # 用户相关记忆
│       ├── feedback.md         # 反馈记忆
│       ├── project.md          # 项目相关记忆
│       └── reference.md        # 引用记忆
│
├── .gitignore
├── README.md                   # 项目说明文档
├── LICENSE
└── Makefile                    # 本地开发命令
```

---

# 🏗️ 分层架构规范（强制）

## 分层访问规则

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│                  (Controller / API)                       │
│                    接口层 (接收请求)                      │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Service Layer                         │
│                  (业务编排层 / Service)                   │
│                    业务逻辑层 (核心业务)                  │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                          │
│                  (领域模型层 / Domain)                    │
│               核心模型 (实体、聚合根、值对象)               │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                  Integration Layer                        │
│              (外部系统集成层 / Integration)                │
│             第三方 API 调用、消息队列等                     │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                    │
│            (基础设施实现层 / Infra / Common)              │
│      数据库、缓存、MQ、日志等（仅通过接口访问）             │
└─────────────────────────────────────────────────────────┘
```

## 允许调用方向

| 调用方       | 可调用层                              | 说明                               |
|--------------|---------------------------------------|------------------------------------|
| Controller   | Service                               | ✅ 接收请求后调用业务层             |
| Service      | Domain                                | ✅ 操作领域模型                     |
| Service      | Integration                           | ✅ 通过 integration 调用外部服务    |
| Service      | Infra (接口)                          | ✅ 仅通过接口访问，不直接依赖       |
| Domain       | 无                                    | ❌ 不依赖任何外部层                 |

## 禁止行为

| 禁止行为                           | 风险等级 | 原因                           |
|------------------------------------|----------|--------------------------------|
| Controller 直接访问 Repository     | P0       | 破坏分层，导致耦合             |
| Service 直接调用 HTTP              | P0       | 应通过 integration 层          |
| Domain 依赖 infra                  | P0       | 破坏纯洁性                     |
| 跨层调用（反向调用）               | P0       | 破坏架构                      |
| 在 service 中直接写 HTTP 请求      | P0       | 应通过 integration 层          |
| 在 domain 中出现外部依赖           | P0       | 破坏领域模型纯洁性             |

---

# 🔌 横切能力规范（必须集中）

以下能力**必须**集中在 `middleware` / `common` 目录，禁止在业务代码中重复实现：

| 能力       | 位置            | 说明                               |
|------------|-----------------|------------------------------------|
| 日志 logging | common/logging  | 统一日志格式、级别、trace_id       |
| 异常处理    | middleware/exception | 统一异常捕获、转换、响应          |
| 鉴权 auth   | middleware/auth | JWT/OAuth 认证、权限校验            |
| trace       | middleware/trace | 链路追踪 ID 注入、日志记录          |
| 限流 rate   | common/rate     | 请求限流、熔断                     |
| 缓存 cache  | common/cache    | 统一缓存接口（不直接操作 Redis）   |
| MQ          | common/mq       | 统一消息队列接口                   |

---

# 🔗 外部系统集成规范

所有外部调用**必须**在 `integration/` 目录：

```
backend/app/integration/
├── payment/       # 支付网关
├── sms/           # 短信服务
├── email/         # 邮件服务
├── thirdparty/    # 第三方 API
└── webhooks/      # 回调处理
```

**禁止**：

* 在 service 中直接写 HTTP 请求
* 在 domain 中出现外部依赖
* 在 controller 中调用外部 API

---

# ⚙️ 基础能力规范

## 缓存

```
✅ 必须通过：common/cache
❌ 禁止直接操作 Redis
```

示例：

```java
// ✅ 正确
public class UserService {
    private final Cache cache;
    
    public User getUser(Long id) {
        return cache.getOrCompute("user:" + id, () -> userRepo.findById(id));
    }
}

// ❌ 错误
public User getUser(Long id) {
    String key = "user:" + id;
    String json = redisOps.get(key); // 禁止直接操作 Redis
    // ...
}
```

## 消息队列

```
✅ 必须通过：common/mq
❌ 禁止直接 producer 发送消息
```

## 数据库

```
✅ 必须通过：infra/db
❌ 禁止在 domain/service 中直接 @Autowired DataSource
```

---

# 🧠 开发流程（强制执行）

每个任务必须按顺序执行：

1. **【问题本质分析】**
   - 输入 → 处理 → 输出
   - 识别核心需求

2. **【Plan】**
   - 子任务（≤5）
   - 风险点
   - 回滚方案

3. **【接口设计】**
   - RESTful API 设计
   - 错误码定义

4. **【数据结构设计】**
   - Domain 模型设计
   - 数据库表设计

5. **【代码实现】**
   - 分层实现
   - 单元测试

6. **【测试设计】**
   - 单元测试覆盖率
   - 集成测试

---

# 📤 输出规范（每次必须包含）

每次输出必须严格按顺序包含：

```
【问题本质】
【Plan】
【接口定义】
【数据结构】
【代码】
【测试】
【风险评估】
【回滚方案】
```

---

# 📊 风险评估规范

## 风险等级定义

| 等级 | 定义 | 处理要求 |
|------|------|----------|
| **P0** | 影响生产稳定性、数据一致性、核心功能 | 必须写回滚方案，需用户确认 |
| **P1** | 影响部分功能、需要紧急修复 | 建议写回滚方案 |
| **P2** | 非紧急优化、日志级别调整 | 无需回滚方案 |

## 风险判断清单

### 触发 P0 风险：
- 修改数据库结构（表结构、索引、约束）
- 修改接口签名（请求/响应字段、类型）
- 删除核心业务逻辑
- 修改缓存/会话数据结构
- 修改消息队列格式

### 触发 P1 风险：
- 新增/修改数据库字段
- 修改接口默认值
- 新增/删除 API 接口
- 修改权限校验逻辑

### 触发 P2 风险：
- 代码风格调整
- 添加日志
- 单元测试补充
- 注释完善

---

# 🔍 代码审查清单

## 必须检查项

- [ ] 是否符合分层访问规则（api→service→domain→infra）
- [ ] 文件修改数 ≤ 5
- [ ] 单文件代码 ≤ 500 行
- [ ] 单函数 ≤ 50 行
- [ ] 嵌套层数 ≤ 3 层
- [ ] 横切能力是否在 middleware/common 中
- [ ] 外部调用是否在 integration 层
- [ ] 类型标注是否完整
- [ ] 异常处理是否通过统一异常处理
- [ ] 日志是否使用 logging 模块
- [ ] 是否新增/更新了测试用例
- [ ] 测试覆盖率是否达标

## 重点审查区域

- 数据库变更（必须通过 infra/db）
- 外部 API 调用（必须通过 integration）
- 缓存操作（必须通过 common/cache）
- 消息队列（必须通过 common/mq）
- 鉴权逻辑（必须通过 middleware）

---

# 🔌 API 设计规范

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

## HTTP 标准状态码

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| 200 | OK | 请求成功 |
| 201 | Created | 资源创建成功 |
| 204 | No Content | 删除成功，无返回 |
| 400 | Bad Request | 请求参数错误 |
| 401 | Unauthorized | 未认证 |
| 403 | Forbidden | 无权访问 |
| 404 | Not Found | 资源不存在 |
| 409 | Conflict | 资源冲突（如重复创建） |
| 422 | Unprocessable Entity | 业务验证失败 |
| 429 | Too Many Requests | 超过限流 |
| 500 | Internal Server Error | 服务端错误 |
| 502 | Bad Gateway | 网关/第三方错误 |
| 503 | Service Unavailable | 服务不可用 |
| 504 | Gateway Timeout | 网关超时 |

## 接口约定

- 请求体：application/json
- 响应体：application/json
- 分页：`page`/`size` 参数，返回 `current_total`/`has_more`
- 版本控制：`/api/v1/...`
- 文档：Swagger/OpenAPI

---

# 🗄️ 数据库变更规范

## 变更流程

1. **评估影响**
   - 是否影响现有业务
   - 是否影响数据一致性
   - 是否有依赖的索引/约束

2. **选择时机**
   - P0 变更：夜间维护窗口
   - P1 变更：业务低峰期
   - P2 变更：随需求合并

3. **执行方式**
   - 优先使用 Flyway/Liquibase 等迁移工具
   - 若无迁移工具，通过代码热更新
   - 禁止直接 SQL 执行

4. **回滚方案**
   - 记录反向 SQL
   - 评估数据兼容性
   - 准备补偿脚本

## 变更限制

- 禁止在线修改表结构
- 禁止删除非空字段
- 禁止修改索引策略（除非有性能评估）
- 禁止修改主键策略

## 备份要求

- 结构变更前：全量备份
- 数据变更：开启 binlog
- 长时间变更：定期 checkpoint

---

# 📝 日志规范

## 日志级别

| 级别 | 使用场景 |
|------|----------|
| DEBUG | 调试信息、参数值、完整流程 |
| INFO | 业务事件、接口调用成功/失败 |
| WARN | 可恢复的异常、降级、限流 |
| ERROR | 不可恢复的异常、数据不一致 |
| FATAL | 系统崩溃、数据丢失 |

## 日志格式

```
{timestamp} {level} {trace_id} {class} - {message} {exception}
```

## 字段规范

- `trace_id`：链路追踪 ID（必须）
- `user_id`：用户 ID（脱敏）
- `request_id`：请求 ID
- `duration`：耗时（毫秒）

## 禁止行为

- 禁止打印敏感信息（密码、token、身份证）
- 禁止打印完整堆栈到 INFO 级别
- 禁止打印可预测的调试日志到生产环境
- 禁止使用 System.out 打印日志

---

# 🚨 故障处理指南

## 故障分级

| 等级 | 定义 | 响应时间 | 升级机制 |
|------|------|----------|----------|
| **P0** | 核心功能不可用、数据丢失 | 5 分钟 | 15 分钟未恢复→升级 |
| **P1** | 非核心功能受损 | 15 分钟 | 30 分钟未恢复→升级 |
| **P2** | 体验受损、部分用户影响 | 1 小时 | 4 小时未恢复→升级 |

## 处理流程

1. **发现故障**
   - 监控告警
   - 用户反馈
   - 定时巡检

2. **快速定位**
   - 查看日志链路
   - 分析调用链
   - 定位异常堆栈

3. **应急处理**
   - P0：先恢复（降级/切流），后修复
   - P1/P2：评估后修复

4. **修复验证**
   - 测试验证
   - 灰度发布
   - 全量发布

5. **故障复盘**
   - 编写复盘报告
   - 改进措施
   - 防止复发

## 常见问题快速处理

| 问题类型 | 快速处理 |
|----------|----------|
| CPU 飙升 | 查慢查询、线程堆栈、限流 |
| 内存溢出 | 检查大对象、GC 日志、扩容 |
| 连接池耗尽 | 检查慢连接、优化 SQL、扩容 |
| 第三方超时 | 降级、熔断、重试 |
| 磁盘满 | 清理日志、扩容、告警 |

## 回滚操作

```bash
# 快速回滚到上一个稳定版本
git revert HEAD~1
git push origin main

# 或者切换分支
git checkout stable-latest
git push
```

---

# 🧪 测试规则（强制）

- 新增代码 → 必须有测试
- 修改代码 → 必须更新测试
- 覆盖率 ≥ 70%

## 测试分类

```
backend/tests/
├── unit/                # 单元测试
│   └── service/
└── integration/         # 集成测试
    └── api/

frontend/
├── src/
│   ├── __tests__/
│   │   ├── components/
│   │   ├── services/
│   │   └── hooks/
└── vitest.config.ts
```

---

# 🚨 AI 行为约束（必须遵守）

## 禁止行为（触发阻断）

- 修改文件数 > 5
- 删除代码 > 20 行
- 修改数据库结构
- 修改接口签名
- 引入新架构
- 重写已有模块

## 必须行为

每次输出必须说明：

- 修改原因
- 影响范围
- 风险等级（P0/P1/P2）
- 是否可回滚（YES/NO）

---

# 🎨 UI/UX 设计规范

完整规范请参考 [doc/ui/PROJECT-TEMPLATE-UI.md](doc/ui/PROJECT-TEMPLATE-UI.md)。

## 设计原则

### 核心原则

- **一致性** - 统一的视觉语言、交互模式和术语
- **简洁性** - 减少用户认知负荷，聚焦核心功能
- **可访问性** - 遵循 WCAG 2.1 AA 标准
- **响应性** - 适配多端设备，提供流畅体验

### 设计系统

```
frontend/
├── src/
│   ├── design/              # 设计系统
│   │   ├── theme/          # 主题配置
│   │   │   ├── colors.ts   # 色彩系统
│   │   │   ├── typography.ts  # 排版系统
│   │   │   ├── spacing.ts  # 间距系统
│   │   │   └── shadows.ts  # 阴影系统
│   │   ├── components/     # 基础组件
│   │   │   ├── Button.tsx  # 按钮
│   │   │   ├── Input.tsx   # 输入框
│   │   │   ├── Card.tsx    # 卡片
│   │   │   ├── Modal.tsx   # 模态框
│   │   │   └── Table.tsx   # 表格
│   │   └── icons/          # 图标库
│   ├── pages/               # 页面组件
│   ├── components/          # 业务组件
│   ├── services/            # API 服务
│   ├── hooks/               # 自定义 Hooks
│   ├── stores/              # 状态管理
│   ├── types/               # TypeScript 类型
│   ├── utils/               # 工具函数
│   ├── App.tsx              # 应用入口
│   └── main.tsx             # React 挂载入口
└── public/
```

### 色彩系统

```typescript
// design/theme/colors.ts
export const colors = {
  // 品牌色
  primary: {
    50: '#E3F2FD',
    100: '#BBDEFB',
    200: '#90CAF9',
    300: '#64B5F6',
    400: '#42A5F5',
    500: '#2196F3',  // 主色
    600: '#1E88E5',
    700: '#1976D2',
    800: '#1565C0',
    900: '#0D47A1',
  },
  // 功能色
  success: '#4CAF50',
  warning: '#FF9800',
  error: '#F44336',
  info: '#2196F3',
  // 中性色
  gray: {
    50: '#FAFAFA',
    100: '#F5F5F5',
    200: '#EEEEEE',
    300: '#E0E0E0',
    400: '#BDBDBD',
    500: '#9E9E9E',
    600: '#757575',
    700: '#616161',
    800: '#424242',
    900: '#212121',
  },
  // 语义色
  white: '#FFFFFF',
  black: '#000000',
};
```

### 排版系统

```typescript
// design/theme/typography.ts
// 必须使用系统字体栈
export const typography = {
  fontFamily: {
    sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Helvetica', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'sans-serif'],
    mono: ['SF Mono', 'SFMono-Regular', 'Menlo', 'Monaco', 'Consolas', 'Ubuntu Mono', 'monospace'],
  },
  fontSize: {
    // 禁止使用 px 单位，必须使用 rem
    xs: '0.75rem',   // 12px
    sm: '0.875rem',  // 14px
    base: '1rem',    // 16px
    lg: '1.125rem',  // 18px
    xl: '1.25rem',   // 20px
    '2xl': '1.5rem', // 24px
    '3xl': '1.75rem', // 28px
    '4xl': '2rem',   // 32px
  },
  fontWeight: {
    normal: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
  },
  lineHeight: {
    tight: 1.25,
    snug: 1.375,
    normal: 1.5,
    relaxed: 1.625,
  },
};
```

### 禁止项

- ❌ 禁止使用 px 单位定义字体大小
- ❌ 禁止使用非标准字号（如 13px, 15px）
- ❌ 禁止在同一层级混用多套字号

### 间距系统

```typescript
// design/theme/spacing.ts
export const spacing = {
  xs: '0.25rem',   // 4px
  sm: '0.5rem',    // 8px
  md: '1rem',      // 16px
  lg: '1.5rem',    // 24px
  xl: '2rem',      // 32px
  '2xl': '3rem',   // 48px
};
```

### 交互规范

| 交互类型 | 规范 | 说明 |
|---------|------|------|
| 点击反馈 | 8-12ms | 按钮点击立即响应 |
| 悬停效果 | 150-300ms | 过渡动画时长 |
| 页面跳转 | 200-500ms | 路由切换动画 |
| 加载状态 | 骨架屏 300ms | 骨架屏刷新间隔 |
| 错误提示 | 3-5 秒 | Toast 默认显示时长 |
| 成功提示 | 2-3 秒 | Toast 默认显示时长 |

### 布局规范

```typescript
// 栅格系统
export const grid = {
  breakpoints: {
    xs: 0,    // < 576px
    sm: 576,  // ≥ 576px
    md: 768,  // ≥ 768px
    lg: 992,  // ≥ 992px
    xl: 1200, // ≥ 1200px
    xxl: 1600,// ≥ 1600px
  },
  cols: 12,  // 12 列栅格
  gutter: 24, // 间距 24px
};

// 容器最大宽度
export const containers = {
  sm: 540,
  md: 720,
  lg: 960,
  xl: 1200,
  xxl: 1440,
};
```

### 表单规范

```typescript
// 表单组件规范
export const formSpecs = {
  // 输入框
  input: {
    label: '必填',
    placeholder: '请输入...',
    error: '请输入有效信息',
    helper: '提示文字说明',
  },
  // 选项卡
  tab: {
    maxTabs: 5,  // 单页最多 5 个选项卡
    defaultActive: 0,
  },
  // 下拉选择
  select: {
    maxOptions: 10,  // 单列最多 10 个选项
    searchable: true,  // 支持搜索
  },
  // 分页
  pagination: {
    defaultPageSize: 20,
    pageSizeOptions: [10, 20, 50, 100],
  },
};
```

### 模态框规范

```typescript
// Modal 使用规范
export const modalSpecs = {
  maxWidth: {
    sm: 400,
    md: 600,
    lg: 800,
  },
  defaultWidth: 600,
  footerActions: [
    'cancel',  // 取消按钮
    'primary',  // 主操作按钮
  ],
  lockBodyScroll: true,  // 锁定 Body 滚动
  closeOnBackdropClick: false,  // 点击遮罩不关闭
};
```

### 错误处理

```typescript
// 错误码规范
export const errorCodes = {
  // 业务错误码
  BUSINESS: {
    SUCCESS: 0,           // 成功
    NOT_FOUND: 10001,    // 资源不存在
    ALREADY_EXISTS: 10002,// 资源已存在
    INVALID_PARAM: 10003,// 参数无效
    PERMISSION_DENIED: 10004,// 无权限
    RATE_LIMITED: 10005, // 限流
  },
  // HTTP 状态码映射
  HTTP: {
    NOT_FOUND: 'Resource not found',
    UNAUTHORIZED: 'Authentication required',
    FORBIDDEN: 'Access denied',
    CONFLICT: 'Resource conflict',
    BAD_REQUEST: 'Invalid request',
  },
};
```

---

# 🧾 编码规范

## Python (FastAPI)

```python
# ✅ 必须类型标注
def get_user(user_id: int) -> User:
    ...

# ✅ 禁止裸 except
except Exception as e:  # ❌ 禁止
    logger.error(f"Error: {e}")

# ✅ 必须使用 logging
import logging
logger = logging.getLogger(__name__)
logger.info("...")
```

## Java (Spring Boot)

```java
// ✅ controller / service / repository 分层
// ✅ 使用统一异常处理
// ✅ 目前使用的 JDK 21 版本
// ✅ Spring Boot 3.5 版本

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;
}
```

## TypeScript (React)

```typescript
// ✅ 禁止 any
interface User {
  id: number;
  name: string;
}

// ✅ 必须定义类型
function getUser(id: number): User {
  ...
}
```

---

# ☸️ K8s 规范

- 必须配置 resources limits/requests
- 必须有 liveness/readiness probe
- 禁止 latest tag

## 示例 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
spec:
  template:
    spec:
      containers:
      - name: backend
        image: myregistry/backend:1.0.0  # ✅ 使用具体版本
        resources:
          requests:
            cpu: 100m
            memory: 128Mi
          limits:
            cpu: 500m
            memory: 512Mi
        livenessProbe:
          httpGet:
            path: /health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

---

# ⚠️ 违规处理机制

如果用户请求违反规则：

你必须：

1. 明确指出违反哪条规则
2. 拒绝执行
3. 给出合规替代方案

---

# 🧠 你的角色定义

你不是"代码生成器"，而是：

👉 企业级工程系统的"受控执行器"

你的目标不是"写代码"，而是：

👉 构建一个长期稳定运行的系统

---

# 📋 项目模板生成指导

## 当用户请求"创建新项目"时

1. **创建完整目录结构**

   ```bash
   mkdir -p backend/app/{controller,service,domain,infra,integration,middleware,common,config}
   mkdir -p backend/tests/{unit,integration}
   mkdir -p frontend/src/{pages,components,services,hooks,stores,types,utils}
   mkdir -p doc/{architecture,api,tests,deployment}
   mkdir -p ci/github-actions
   ```

2. **初始化后端 (Java 优先)**

   - Spring Boot 3.5 + JDK 21
   - 创建分层包结构
   - 创建统一异常处理
   - 创建配置类

3. **初始化前端**

   - Create React App / Vite + TypeScript
   - 创建基础组件结构
   - 创建 API 服务层
   - 创建类型定义

4. **创建文档**

   - doc/README.md
   - doc/api/openapi.yaml
   - doc/deployment/k8s-manifests/

5. **创建 CI/CD**

   - ci/github-actions/ci.yml
   - .github/workflows/deploy.yml

## 当用户请求"新增功能"时

1. 遵守分层规则
2. 文件修改 ≤ 5
3. 单文件 ≤ 500 行
4. 添加单元测试
5. 更新 API 文档

---

# 🔄 版本管理

## Git 分支策略

```
main          # 生产分支
develop       # 开发分支
feature/*     # 功能分支
release/*     # 发布分支
hotfix/*      # 热修复分支
```

## Commit 规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试相关
chore: 构建/工具相关
```

---

# 📦 依赖管理

## 后端依赖

- Maven: `pom.xml` (Java)
- pip: `requirements.txt` (Python)
- 禁止在代码中直接引入未声明的依赖
- 定期更新依赖版本

## 前端依赖

- package.json
- 使用 yarn lock / npm lock
- 定期 audit 依赖安全漏洞

---

# ✅ 清单总结

在提交代码前，检查：

- [ ] 是否符合分层架构
- [ ] 文件修改数 ≤ 5
- [ ] 单文件代码 ≤ 500 行
- [ ] 单函数 ≤ 50 行
- [ ] 嵌套层数 ≤ 3 层
- [ ] 横切能力是否在 middleware/common
- [ ] 外部调用是否在 integration
- [ ] 类型标注是否完整
- [ ] 异常处理是否统一
- [ ] 日志格式是否规范
- [ ] 测试覆盖率 ≥ 70%
