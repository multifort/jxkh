# 企业绩效考核系统 - 项目模板生成指南

## 📋 概述

本文档定义了企业绩效考核系统的标准项目模板结构，用于指导后续项目的创建和开发。

---

## 🎯 项目目标

构建一个：
- ✅ 高稳定性（优先级最高）
- ✅ 可维护
- ✅ 可扩展（但不复杂）
- ✅ 可部署在 Kubernetes

---

## 📁 标准项目结构

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
│
├── frontend/                     # React + TypeScript 前端
│   ├── src/
│   │   ├── pages/
│   │   │   ├── auth/
│   │   │   ├── dashboard/
│   │   │   └── settings/
│   │   ├── components/
│   │   │   ├── common/
│   │   │   ├── layout/
│   │   │   └── business/
│   │   ├── services/
│   │   ├── hooks/
│   │   ├── stores/
│   │   ├── types/
│   │   ├── utils/
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── public/
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   └── .env.example
│
├── doc/                         # 文档
│   ├── architecture/
│   │   └── README.md
│   ├── api/
│   │   └── openapi.yaml
│   ├── tests/
│   │   └── test-cases.md
│   └── deployment/
│       ├── k8s-manifests/
│       │   ├── backend-deployment.yaml
│       │   ├── frontend-deployment.yaml
│       │   └── service.yaml
│       └── docker-compose.yml
│
├── ci/                         # CI/CD
│   └── github-actions/
│       └── ci.yml
│
├── .claude/                    # Claude 工作目录
│   └── memory/
│       ├── user.md
│       ├── feedback.md
│       ├── project.md
│       └── reference.md
│
├── .gitignore
├── README.md
├── LICENSE
└── Makefile
```

---

## 🚀 快速开始

### 1. 创建项目目录

```bash
mkdir -p jxkh/{backend,frontend,doc,ci,.claude/memory}
cd jxkh
```

### 2. 创建后端目录结构

```bash
mkdir -p backend/app/{controller,service,domain,infra,integration,middleware,common,config}
mkdir -p backend/tests/{unit,integration}
mkdir -p backend/src/main/resources
```

### 3. 创建前端目录结构

```bash
mkdir -p frontend/src/{pages,components,services,hooks,stores,types,utils}
mkdir -p frontend/src/pages/{auth,dashboard,settings}
mkdir -p frontend/src/components/{common,layout,business}
```

### 4. 创建文档目录

```bash
mkdir -p doc/{architecture,api,tests,deployment}
mkdir -p doc/deployment/k8s-manifests
```

### 5. 创建 CI 目录

```bash
mkdir -p ci/github-actions
```

### 6. 创建 .claude 目录

```bash
mkdir -p .claude/memory
```

---

## 📝 核心文件清单

### 后端核心文件

| 文件 | 说明 |
|------|------|
| `backend/pom.xml` | Maven 配置 |
| `backend/app/Application.java` | Spring Boot 启动入口 |
| `backend/app/src/main/resources/application.yml` | 应用配置 |
| `backend/app/Dockerfile` | 容器化配置 |
| `backend/README.md` | 后端说明 |

### 前端核心文件

| 文件 | 说明 |
|------|------|
| `frontend/package.json` | 依赖配置 |
| `frontend/tsconfig.json` | TypeScript 配置 |
| `frontend/vite.config.ts` | Vite 配置 |
| `frontend/.env.example` | 环境变量示例 |
| `frontend/src/App.tsx` | 应用入口 |

### 文档核心文件

| 文件 | 说明 |
|------|------|
| `doc/README.md` | 文档总览 |
| `doc/api/openapi.yaml` | API 接口文档 |
| `doc/deployment/docker-compose.yml` | 本地开发环境 |
| `doc/deployment/k8s-manifests/backend-deployment.yaml` | K8s 部署配置 |

### CI 核心文件

| 文件 | 说明 |
|------|------|
| `ci/github-actions/ci.yml` | GitHub Actions 配置 |

---

## 🎨 UI/UX 设计规范

完整的 UI/UX 设计规范请参考 [doc/ui/PROJECT-TEMPLATE-UI.md](../ui/PROJECT-TEMPLATE-UI.md)。

### 核心规范

- **色彩系统** - 品牌色、中性色、语义色的统一规范
- **排版系统** - 字体、字号、字重、行高
- **间距系统** - 基于 4px 的标准化间距
- **圆角/阴影** - 统一的视觉层次
- **交互规范** - 动画时长、反馈提示
- **布局规范** - 栅格系统、断点配置
- **组件库** - 基础组件和业务组件
- **表单规范** - 表单组件和验证
- **表格规范** - 表格组件和状态
- **模态框规范** - 模态框使用指南
- **空状态规范** - 空状态展示
- **响应式设计** - 多端适配
- **可访问性** - WCAG 2.1 AA 标准要求
- **图标规范** - 图标使用指南
- **设计令牌** - 设计令牌定义
- **禁止项** - 必须遵守的约束

### 关键配置

| 配置项 | 值 | 说明 |
|--|--|--|
| 栅格列数 | 12 | 响应式布局 |
| 栅格间距 | 24px | 默认间距 |
| 输入框高度 | 40px | 标准高度 |
| 按钮高度 | 32-40px | 操作按钮 |
| 容器最大宽度 | 1440px | 超大屏 |
| 最小字体 | 12px | 可读性底线 |
| 对比度 | ≥ 4.5:1 | WCAG 2.1 AA |
| 间距基准 | 4px | 4px 倍数 |

### 断点配置

| 断点 | 范围 | 设备类型 |
|--|--|--|
| mobile | < 576px | 手机 |
| tablet-portrait | 576px - 768px | 平板竖屏 |
| tablet-landscape | 768px - 992px | 平板横屏 |
| desktop | 992px - 1200px | 桌面 |
| large | ≥ 1200px | 大桌面 |

### 颜色配置

| 色值 | 用途 | 说明 |
|--|--|--|
| primary[500] | #2196F3 | 主色 |
| success | #52c41a | 成功 |
| warning | #fa8c16 | 警告 |
| error | #f5222d | 错误 |
| info | #1677ff | 信息 |
| gray[900] | #000000 | 主要文字 |
| gray[600] | #606266 | 次要文字 |
| gray[400] | #A0AEC0 | 占位符 |
| gray[200] | #EBEEF5 | 边框 |

### 组件结构

```typescript
frontend/src/design/
├── theme/
│   ├── colors.ts        # 色彩系统
│   ├── typography.ts    # 排版系统
│   ├── spacing.ts       # 间距系统
│   ├── radius.ts        # 圆角系统
│   ├── shadow.ts        # 阴影系统
│   └── breakpoints.ts   # 断点配置
├── components/          # 基础组件
│   ├── Button.tsx
│   ├── Input.tsx
│   ├── Select.tsx
│   ├── Table.tsx
│   ├── Modal.tsx
│   ├── Card.tsx
│   └── Empty.tsx
└── icons/               # 图标库
```

---

## 🎨 架构规范

完整的 UI/UX 设计规范请参考 [doc/ui/PROJECT-TEMPLATE-UI.md](../ui/PROJECT-TEMPLATE-UI.md)。

### 核心规范

- **设计系统** - 色彩、排版、间距、阴影的统一规范
- **组件库** - 基础组件和业务组件的使用指南
- **交互规范** - 动画时长、加载状态、反馈提示
- **响应式** - 多端适配的布局规范
- **可访问性** - WCAG 2.1 AA 标准要求

### 关键配置

| 配置项 | 值 | 说明 |
|--|--|--|
| 栅格列数 | 12 | 响应式布局 |
| 栅格间距 | 24px | 默认间距 |
| 输入框高度 | 40px | 标准高度 |
| 按钮高度 | 32-40px | 操作按钮 |
| 容器最大宽度 | 1440px | 超大屏 |
| 最小字体 | 12px | 可读性底线 |

### 断点配置

| 断点 | 范围 | 设备类型 |
|--|--|--|
| xs | < 576px | 手机 |
| sm | 576px - 768px | 平板竖屏 |
| md | 768px - 992px | 平板横屏 |
| lg | 992px - 1200px | 桌面 |
| xl | ≥ 1200px | 大桌面 |

### 组件结构

```typescript
frontend/src/design/
├── theme/
│   ├── colors.ts        # 色彩系统
│   ├── typography.ts    # 排版系统
│   ├── spacing.ts       # 间距系统
│   ├── shadows.ts       # 阴影系统
│   └── breakpoints.ts   # 断点配置
├── components/          # 基础组件
│   ├── Button.tsx
│   ├── Input.tsx
│   ├── Select.tsx
│   ├── Table.tsx
│   ├── Modal.tsx
│   ├── Card.tsx
│   └── Empty.tsx
└── icons/               # 图标库
```

---

## 🎨 架构规范

### 分层架构

```
Controller → Service → Domain
                  ↓
           Integration → Infrastructure
```

### 允许调用

- ✅ Controller → Service
- ✅ Service → Domain
- ✅ Service → Integration
- ✅ Service → Infra (接口)

### 禁止调用

- ❌ Controller → Repository
- ❌ Service → HTTP (直接)
- ❌ Domain → Infra
- ❌ 跨层反向调用

---

## 🔌 横切能力位置

| 能力 | 位置 |
|------|------|
| 日志 | `common/logging` |
| 异常 | `middleware/exception` |
| 鉴权 | `middleware/auth` |
| 链路追踪 | `middleware/trace` |
| 限流 | `common/rate` |
| 缓存 | `common/cache` |
| 消息队列 | `common/mq` |

---

## ⚙️ 技术栈版本

### 后端

- JDK: 21
- Spring Boot: 3.5
- Maven: 3.9+

### 前端

- Node.js: 18+
- React: 18+
- TypeScript: 5.3+
- Vite: 5.2+

---

## 📊 代码规范

### 复杂度限制

- 单文件 ≤ 500 行
- 单函数 ≤ 50 行
- 嵌套 ≤ 3 层
- 单次修改文件 ≤ 5

### 测试要求

- 新增代码 → 必须有测试
- 修改代码 → 必须更新测试
- 覆盖率 ≥ 70%

---

## 🚨 风险等级

| 等级 | 定义 | 回滚方案 |
|------|------|----------|
| P0 | 影响生产、数据一致性 | 必须 |
| P1 | 影响部分功能 | 建议 |
| P2 | 非紧急优化 | 无需 |

---

## 🔄 Git 规范

### 分支策略

```
main          # 生产
develop       # 开发
feature/*     # 功能
release/*     # 发布
hotfix/*      # 热修复
```

### Commit 格式

```
type: subject

body

footer
```

类型：feat, fix, docs, style, refactor, test, chore

---

## 📦 K8s 部署规范

### 必须配置

- ✅ resources limits/requests
- ✅ liveness probe
- ✅ readiness probe
- ✅ 具体版本号（禁止 latest）

### 示例

```yaml
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
readinessProbe:
  httpGet:
    path: /health/ready
    port: 8080
```

---

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

---

## 📞 联系与支持

- 架构问题 → 查看 `CLAUDE.md`
- API 文档 → `doc/api/openapi.yaml`
- 部署文档 → `doc/deployment/`
