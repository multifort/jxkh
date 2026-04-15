# 企业绩效考核系统

> 一个高稳定性、可维护、可扩展的企业级绩效考核系统

## 📚 目录结构

```
jxkh/
├── backend/              # Java/Spring Boot 后端服务
├── frontend/             # React + TypeScript 前端应用
├── doc/                  # 项目文档
│   ├── architecture/     # 架构设计文档
│   ├── api/              # API 接口文档
│   ├── tests/            # 测试文档
│   ├── deployment/       # 部署文档
│   └── PROJECT-TEMPLATE.md  # 项目模板指南
├── ci/                   # CI/CD 配置
├── .claude/              # Claude 工作目录
│   └── memory/           # 项目记忆
├── .gitignore
├── CLAUDE.md             # 架构规范与 AI 行为约束
└── README.md             # 项目说明（此处）
```

## 🎯 核心目标

- **高稳定性** - 稳定性 > 可读性 > 性能 > 开发速度
- **可维护** - 清晰的代码结构和文档
- **可扩展** - 但不复杂，遵循单一职责原则
- **K8s 就绪** - 可直接部署到 Kubernetes

## 🛠️ 技术栈

### 后端

- **语言**: Java 21
- **框架**: Spring Boot 3.5.0
- **数据库**: PostgreSQL / MySQL 8.0+
- **缓存**: Redis 7.x (通过 common/cache 抽象)
- **消息队列**: Kafka / RabbitMQ (通过 common/mq 抽象)

### 前端

- **框架**: React 18 + TypeScript
- **构建工具**: Vite
- **状态管理**: Zustand / Redux
- **HTTP 客户端**: Axios

### 基础设施

- **容器化**: Docker
- **编排**: Kubernetes
- **CI/CD**: GitHub Actions

## 🏗️ 架构规范

### 分层架构

```
┌─────────────────────────────────────┐
│           Controller (API)          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│          Service (业务逻辑)         │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│         Domain (核心模型)           │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│    Integration (外部系统集成)       │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│    Infra (基础设施实现)             │
│    - 数据库 | 缓存 | MQ | 日志      │
└─────────────────────────────────────┘
```

### 分层访问规则

| 调用方 | 可调用层 | 说明 |
|--------|----------|------|
| Controller | Service | ✅ 接收请求后调用业务层 |
| Service | Domain | ✅ 操作领域模型 |
| Service | Integration | ✅ 调用外部服务 |
| Service | Infra (接口) | ✅ 仅通过接口访问 |
| Domain | 无 | ❌ 不依赖外部层 |

**禁止行为**:
- ❌ Controller 直接访问 Repository
- ❌ Service 直接调用 HTTP
- ❌ Domain 依赖 infra
- ❌ 跨层调用

## 📊 复杂度预算

| 限制 | 说明 |
|------|------|
| 单文件 | ≤ 500 行 |
| 单函数 | ≤ 50 行 |
| 嵌套 | ≤ 3 层 |
| 单次修改文件 | ≤ 5 个 |

## 🔌 横切能力

以下能力**必须**集中在 `middleware` / `common`:

- **日志** (`common/logging`)
- **异常处理** (`middleware/exception`)
- **鉴权** (`middleware/auth`)
- **链路追踪** (`middleware/trace`)
- **限流** (`common/rate`)
- **缓存** (`common/cache`)
- **消息队列** (`common/mq`)

## 🚀 快速开始

### 环境要求

- JDK 21
- Node.js 18+
- Docker 20+

### 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 本地开发环境

```bash
cd doc/deployment
docker-compose up -d
```

## 🧪 测试

```bash
# 后端单元测试
cd backend
mvn test

# 前端测试
cd frontend
npm test
```

## 📦 部署

### Docker

```bash
docker build -t enterprise-performance:latest ./backend
docker build -t enterprise-performance:latest ./frontend
```

### Kubernetes

```bash
cd doc/deployment/k8s-manifests
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml
kubectl apply -f service.yaml
```

## 📖 文档

- [架构规范](CLAUDE.md) - 完整的架构规范和 AI 行为约束
- [项目模板指南](doc/PROJECT-TEMPLATE.md) - 项目创建和开发指南
- [UI/UX 设计规范](doc/ui/PROJECT-TEMPLATE-UI.md) - 前端设计系统和交互规范
- [API 文档](doc/api/openapi.yaml) - RESTful API 接口定义
- [部署文档](doc/deployment/README.md) - 部署和运维指南

## 🚨 重要规范

### 风险等级

| 等级 | 触发条件 | 回滚方案 |
|------|----------|----------|
| P0 | 修改数据库结构、接口签名、核心逻辑 | 必须 |
| P1 | 新增/修改字段、默认值、API 接口 | 建议 |
| P2 | 代码风格、日志、单元测试 | 无需 |

### AI 行为约束

每次输出必须包含：

- 修改原因
- 影响范围
- 风险等级 (P0/P1/P2)
- 是否可回滚 (YES/NO)

**禁止行为**:
- 修改文件数 > 5
- 删除代码 > 20 行
- 修改数据库结构
- 修改接口签名
- 引入新架构
- 重写已有模块

## ✅ 代码审查清单

- [ ] 符合分层架构
- [ ] 文件修改 ≤ 5
- [ ] 单文件 ≤ 500 行
- [ ] 单函数 ≤ 50 行
- [ ] 嵌套 ≤ 3 层
- [ ] 横切能力在 middleware/common
- [ ] 外部调用在 integration
- [ ] 类型标注完整
- [ ] 统一异常处理
- [ ] 日志规范
- [ ] 测试覆盖率 ≥ 70%

## 📝 License

MIT

---

**构建一个长期稳定运行的企业级系统**
