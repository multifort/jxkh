# 前端页面路由映射表

本文档定义前端页面的路由结构和权限控制。

---

## 1. 路由结构总览

```
/
├── /login                          # 登录页（公开）
├── /dashboard                      # 首页看板（需认证）
├── /performance                    # 绩效管理
│   ├── /plans                     # 绩效计划列表
│   │   ├── /create                # 创建计划
│   │   └── /:id                   # 计划详情
│   │       ├── /edit              # 编辑计划
│   │       └── /evaluate          # 绩效评估
│   ├── /records                   # 进度记录
│   │   └── /:planId               # 计划进度详情
│   └── /calibration               # 绩效校准（HR/管理员）
├── /indicators                     # 指标库管理
├── /analytics                      # 数据分析
│   ├── /overview                  # 概览
│   ├── /department                # 部门分析
│   └── /trend                     # 趋势分析
├── /organization                   # 组织管理（管理员）
├── /users                          # 用户管理（管理员/HR）
└── /system                         # 系统配置（管理员）
    ├── /config                    # 系统配置
    ├── /roles                     # 角色管理
    └── /permissions               # 权限管理
```

---

## 2. 详细路由配置

### 2.1 认证模块

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/login` | `LoginPage` | 公开 | 登录页面 |
| `/register` | `RegisterPage` | 公开 | 注册页面（可选） |

---

### 2.2 首页看板

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/dashboard` | `DashboardPage` | 所有登录用户 | 个人绩效概览 |

**功能点**：
- 待办事项列表
- 绩效进度卡片
- 风险预警提示
- 快速操作入口

---

### 2.3 绩效管理模块

#### 2.3.1 绩效计划

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/performance/plans` | `PlanListPage` | 所有用户 | 计划列表（按角色过滤） |
| `/performance/plans/create` | `PlanCreatePage` | EMPLOYEE, MANAGER | 创建新计划 |
| `/performance/plans/:id` | `PlanDetailPage` | 相关用户 | 计划详情 |
| `/performance/plans/:id/edit` | `PlanEditPage` | 计划所有者 | 编辑草稿 |
| `/performance/plans/:id/evaluate` | `EvaluationPage` | 评估人 | 绩效评估 |

**员工视角**：
- 查看自己的计划
- 创建/编辑草稿
- 提交审批
- 查看审批状态
- 进行自评

**主管视角**：
- 查看下属的计划
- 审批计划
- 进行上级评价

**HR视角**：
- 查看所有计划
- 监控审批进度

---

#### 2.3.2 进度记录

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/performance/records` | `RecordListPage` | 所有用户 | 进度记录列表 |
| `/performance/records/:planId` | `RecordDetailPage` | 相关用户 | 计划进度详情 |
| `/performance/records/:planId/new` | `RecordCreatePage` | 计划所有者 | 新建周报/月报 |

**功能点**：
- 周报表单（富文本编辑器）
- 附件上传
- AI 智能总结
- 进度可视化

---

#### 2.3.3 绩效校准

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/performance/calibration` | `CalibrationPage` | HR, ADMIN | 绩效校准工作台 |

**功能点**：
- 部门绩效分布图
- 拖拽调整等级
- 强制分布校验
- 批量校准

---

### 2.4 指标库管理

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/indicators` | `IndicatorListPage` | 所有用户 | 指标库列表 |
| `/indicators/create` | `IndicatorCreatePage` | ADMIN, HR | 创建指标 |
| `/indicators/:id` | `IndicatorDetailPage` | 所有用户 | 指标详情 |
| `/indicators/:id/edit` | `IndicatorEditPage` | ADMIN, HR | 编辑指标 |

---

### 2.5 数据分析模块

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/analytics/overview` | `AnalyticsOverviewPage` | MANAGER, HR, ADMIN | 整体概览 |
| `/analytics/department` | `DepartmentAnalysisPage` | MANAGER, HR, ADMIN | 部门对比分析 |
| `/analytics/trend` | `TrendAnalysisPage` | 所有用户 | 个人趋势分析 |

**图表类型**：
- 等级分布饼图
- 部门排名柱状图
- 绩效趋势折线图
- 雷达图（多维度分析）

---

### 2.6 组织管理

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/organization` | `OrgManagementPage` | ADMIN, HR | 组织树管理 |

**功能点**：
- 组织树展示
- 新增/编辑/删除组织
- 设置负责人
- 组织排序

---

### 2.7 用户管理

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/users` | `UserListPage` | ADMIN, HR | 用户列表 |
| `/users/create` | `UserCreatePage` | ADMIN, HR | 创建用户 |
| `/users/:id` | `UserDetailPage` | ADMIN, HR | 用户详情 |
| `/users/:id/edit` | `UserEditPage` | ADMIN, HR | 编辑用户 |
| `/users/import` | `UserImportPage` | ADMIN, HR | 批量导入 |

---

### 2.8 系统配置

| 路径 | 组件 | 权限 | 说明 |
|------|------|------|------|
| `/system/config` | `SystemConfigPage` | ADMIN | 系统配置 |
| `/system/roles` | `RoleManagementPage` | ADMIN | 角色管理 |
| `/system/permissions` | `PermissionManagementPage` | ADMIN | 权限管理 |

---

## 3. 权限控制规则

### 3.1 路由守卫

```typescript
// 路由配置文件示例
const routes: RouteConfig[] = [
  {
    path: '/dashboard',
    component: DashboardPage,
    meta: {
      requiresAuth: true,
      permissions: [] // 所有登录用户可访问
    }
  },
  {
    path: '/performance/calibration',
    component: CalibrationPage,
    meta: {
      requiresAuth: true,
      permissions: ['performance:calibrate'] // 需要特定权限
    }
  },
  {
    path: '/system/config',
    component: SystemConfigPage,
    meta: {
      requiresAuth: true,
      roles: ['ADMIN'] // 仅管理员可访问
    }
  }
];
```

### 3.2 按钮级权限

```tsx
// 使用权限指令
<Button 
  type="primary"
  permission="performance:plan:create"
  onClick={handleCreate}
>
  创建计划
</Button>

// 或使用 Hook
const { hasPermission } = usePermission();

{hasPermission('performance:plan:approve') && (
  <Button onClick={handleApprove}>审批</Button>
)}
```

---

## 4. 布局结构

### 4.1 主布局（MainLayout）

```
┌─────────────────────────────────────┐
│            Header (60px)             │
│  Logo | 导航菜单 | 用户信息 | 退出   │
├──────────┬──────────────────────────┤
│          │                          │
│ Sidebar  │     Main Content         │
│ (240px)  │     (自适应宽度)          │
│          │                          │
└──────────┴──────────────────────────┘
```

### 4.2 侧边栏菜单结构

```typescript
const menuItems = [
  {
    key: 'dashboard',
    icon: <HomeOutlined />,
    label: '首页',
    path: '/dashboard'
  },
  {
    key: 'performance',
    icon: <TeamOutlined />,
    label: '绩效管理',
    children: [
      { key: 'plans', label: '绩效计划', path: '/performance/plans' },
      { key: 'records', label: '进度记录', path: '/performance/records' },
      { key: 'calibration', label: '绩效校准', path: '/performance/calibration', permission: 'performance:calibrate' }
    ]
  },
  {
    key: 'indicators',
    icon: <FundOutlined />,
    label: '指标库',
    path: '/indicators'
  },
  {
    key: 'analytics',
    icon: <BarChartOutlined />,
    label: '数据分析',
    path: '/analytics/overview'
  },
  {
    key: 'organization',
    icon: <ApartmentOutlined />,
    label: '组织管理',
    path: '/organization',
    roles: ['ADMIN', 'HR']
  },
  {
    key: 'users',
    icon: <UserOutlined />,
    label: '用户管理',
    path: '/users',
    roles: ['ADMIN', 'HR']
  },
  {
    key: 'system',
    icon: <SettingOutlined />,
    label: '系统配置',
    path: '/system/config',
    roles: ['ADMIN']
  }
];
```

---

## 5. 移动端适配

### 5.1 响应式断点

| 断点 | 宽度 | 布局策略 |
|------|------|----------|
| xs | < 576px | 单列，隐藏侧边栏，使用抽屉菜单 |
| sm | 576px - 768px | 单列，可折叠侧边栏 |
| md | 768px - 992px | 双列，固定侧边栏 |
| lg | ≥ 992px | 多列，完整布局 |

### 5.2 移动端特殊处理

- 表格 → 卡片列表
- 复杂表单 → 分步向导
- 图表 → 简化版或隐藏
- 操作按钮 → 下拉菜单

---

## 6. 页面加载策略

### 6.1 懒加载

```typescript
// 路由懒加载
const DashboardPage = lazy(() => import('@/pages/dashboard/DashboardPage'));
const PlanListPage = lazy(() => import('@/pages/performance/PlanListPage'));

// 带 Loading 状态
<Suspense fallback={<PageSkeleton />}>
  <Routes>
    <Route path="/dashboard" element={<DashboardPage />} />
  </Routes>
</Suspense>
```

### 6.2 预加载

```typescript
// 鼠标悬停时预加载
<Link 
  to="/performance/plans"
  onMouseEnter={() => import('@/pages/performance/PlanListPage')}
>
  绩效计划
</Link>
```

---

## 7. 错误页面

| 路径 | 组件 | 说明 |
|------|------|------|
| `/404` | `NotFoundPage` | 页面不存在 |
| `/403` | `ForbiddenPage` | 无权限访问 |
| `/500` | `ServerErrorPage` | 服务器错误 |
| `*` | `NotFoundPage` | 通配符路由 |

---

**文档版本**: V1.0  
**最后更新**: 2026-04-15  
**维护者**: JXKH Team
