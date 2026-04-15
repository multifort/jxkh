# UI/UX 设计规范

## 📋 概述

本文档定义了企业绩效考核系统的 UI/UX 设计规范，用于指导前端开发。

---

## 🎨 设计原则

### 核心原则

| 原则 | 说明 |
|------|------|
| **一致性** | 统一的视觉语言、交互模式和术语 |
| **简洁性** | 减少用户认知负荷，聚焦核心功能 |
| **可访问性** | 遵循 WCAG 2.1 AA 标准 |
| **响应性** | 适配多端设备，提供流畅体验 |

### 用户体验目标

- 首次访问学习成本 < 5 分钟
- 核心操作流程 < 3 步
- 页面加载时间 < 2 秒
- 错误恢复时间 < 10 秒

---

## 📁 设计系统结构

```
frontend/
├── src/
│   ├── design/              # 设计系统
│   │   ├── theme/          # 主题配置
│   │   │   ├── colors.ts   # 色彩系统
│   │   │   ├── typography.ts  # 排版系统
│   │   │   ├── spacing.ts  # 间距系统
│   │   │   ├── shadows.ts  # 阴影系统
│   │   │   └── breakpoints.ts  # 断点系统
│   │   ├── components/     # 基础组件
│   │   │   ├── Button.tsx  # 按钮
│   │   │   ├── Input.tsx   # 输入框
│   │   │   ├── Card.tsx    # 卡片
│   │   │   ├── Modal.tsx   # 模态框
│   │   │   ├── Table.tsx   # 表格
│   │   │   └── Empty.tsx   # 空状态
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

---

## 🎨 色彩系统

### 品牌色

```typescript
// design/theme/colors.ts
export const colors = {
  // 主色
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
  success: '#52c41a',
  warning: '#fa8c16',
  error: '#f5222d',
  info: '#1677ff',
};
```

### 中性色

```typescript
// 使用语义化命名
export const gray = {
  50: '#F5F7FA',   // 页面背景
  100: '#F5F5F5',  // hover 背景
  200: '#EBEEF5',  // 边框颜色
  300: '#D9D9D9',
  400: '#A0AEC0',  // 占位符
  500: '#8A8F98',
  600: '#606266',  // 次要文字
  700: '#404040',
  800: '#333333',
  900: '#000000',  // 主要文字
};
```

### 语义色

```typescript
export const semantic = {
  white: '#FFFFFF',
  black: '#000000',
};
```

### 使用规范

| 用途 | 色值 | 说明 |
|--|--|--|
| 页面背景 | gray[50] | #F5F7FA |
| 卡片背景 | white | #FFFFFF |
| 主要文字 | gray[900] | #000000 |
| 次要文字 | gray[600] | #606266 |
| 占位符 | gray[400] | #A0AEC0 |
| 边框 | gray[200] | #EBEEF5 |
| 主操作按钮 | primary[500] | #2196F3 |
| 次操作按钮 | gray[100]/gray[600] | #F5F5F5/#606266 |
| 成功消息 | success | #52c41a |
| 错误消息 | error | #f5222d |
| 警告消息 | warning | #fa8c16 |
| 信息消息 | info | #1677ff |

**禁止项**：
- ❌ 禁止直接在 JSX 中使用 hex 颜色值（除调试外）
- ❌ 禁止混用多种蓝色系
- ❌ 禁止使用非设计系统色值

---

## 🔤 排版系统

### 字体配置

```typescript
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

### 文本层次

| 层级 | 字体大小 | 字重 | 使用场景 |
|--|--|--|--|
| h1 | 2rem | 500 | 页面标题 |
| h2 | 1.5rem | 600 | 区块标题 |
| h3 | 1.25rem | 600 | 子区块标题 |
| h4 | 1rem | 600 | 卡片标题 |
| body | 1rem | 400 | 正文内容 |
| caption | 0.75rem | 400 | 标签、辅助文字 |

### 禁止项

- ❌ 禁止使用 px 单位定义字体大小
- ❌ 禁止使用非标准字号（如 13px, 15px）
- ❌ 禁止在同一层级混用多套字号

| 层级 | 字体大小 | 字重 | 使用场景 |
|------|----------|------|----------|
| h1 | 2rem | bold | 页面标题 |
| h2 | 1.5rem | semibold | 区块标题 |
| h3 | 1.25rem | semibold | 子区块标题 |
| body | 1rem | regular | 正文内容 |
| caption | 0.75rem | regular | 标签、辅助文字 |

---

## 📏 间距系统

```typescript
// 基于 4px 基数的间距系统
export const spacing = {
  // 必须使用 4px 倍数的间距
  xs: '0.25rem',   // 4px
  sm: '0.5rem',    // 8px
  md: '1rem',      // 16px
  lg: '1.5rem',    // 24px
  xl: '2rem',      // 32px
  '2xl': '3rem',   // 48px
};
```

### 使用场景

| 间距 | 使用场景 |
|------|------|--|
| space-1 (4px) | 图标与文字、小元素间距 |
| space-2 (8px) | 表单内元素、卡片内边距 |
| space-3 (12px) | 元素间距 |
| space-4 (16px) | 区块间距、卡片间距 |
| space-6 (24px) | 页面区块、主要内容区域 |
| space-8 (32px) | 页眉页脚、全屏内容 |
| space-10 (40px) | 大屏间距 |
| space-12 (48px) | 超大间距 |

### 禁止项

- ❌ 禁止使用非 4px 倍数的间距
- ❌ 禁止混用 px 和 rem
- ❌ 禁止硬编码间距值

---

## ⚡ 交互规范

### 动画时长

| 交互类型 | 时长 | 说明 |
|---------|------|------|
| 点击反馈 | 150ms | 必须≥100ms，禁止立即响应 |
| 悬停效果 | 200ms | 统一使用 200ms |
| 页面跳转 | 200-300ms | 路由切换动画 |
| 加载状态 | 300ms | 骨架屏刷新间隔 |
| 模态框开合 | 200-300ms | 淡入淡出动画 |
| Toast 出现 | 300ms | 淡入动画 |
| Toast 消失 | 300ms | 淡出动画 |

### 禁止项

- ❌ 禁止使用 ms 单位，必须使用 ms
- ❌ 禁止点击反馈小于 100ms
- ❌ 禁止混用多种动画时长

### 加载状态

#### 骨架屏

```typescript
// 骨架屏规范
export const skeleton = {
  duration: 150,  // 闪烁间隔 150ms
  gaps: 5,        // 闪烁周期 5 次
};
```

#### Loading Spinner

```typescript
// Loading 规范
export const loading = {
  size: 'sm': 16,
  size: 'md': 20,
  size: 'lg': 24,
  duration: 8,    // 旋转周期 8s
};
```

### Toast 规范

```typescript
// Toast 类型
export const toast = {
  types: ['success', 'error', 'warning', 'info'],
  duration: {
    success: 3000,
    error: 5000,
    warning: 5000,
    info: 4000,
  },
  position: 'top-right',
  maxVisible: 3,
};
```

### 禁止项

- ❌ 禁止使用 ms 单位，必须使用 ms
- ❌ 禁止点击反馈小于 100ms
- ❌ 禁止混用多种动画时长

---

## 📐 布局规范

### 栅格系统

```typescript
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
```

### 容器最大宽度

```typescript
export const containers = {
  sm: 540,
  md: 720,
  lg: 960,
  xl: 1200,
  xxl: 1440,
};
```

### 页面布局

```
┌─────────────────────────────────────────────────────┐
│                    Header (60px)                      │
├─────────────────────────────────────────────────────┤
│                                                       │
│               Main Content (min-height)              │
│  ┌─────────────┐    ┌───────────────────────┐       │
│  │   Sidebar    │    │       Content         │       │
│  │   (240px)    │    │                       │       │
│  └─────────────┘    │   ┌───────────────┐   │       │
│                     │   │    Card       │   │       │
│                     │   │               │   │       │
│                     │   └───────────────┘   │       │
│                     │                       │       │
│                     └───────────────────────┘       │
│                                                       │
├─────────────────────────────────────────────────────┤
│                    Footer (48px)                      │
└─────────────────────────────────────────────────────┘
```

---

## 📝 表单规范

### 表单组件规范

```typescript
export const formSpecs = {
  // 输入框
  input: {
    label: '必填',
    placeholder: '请输入...',
    error: '请输入有效信息',
    helper: '提示文字说明',
    min-height: 40,   // 输入框最小高度 40px
    padding: 12,      // 内边距 12px
  },
  // 选项卡
  tab: {
    maxTabs: 5,  // 单页最多 5 个选项卡
    defaultActive: 0,
    border: true,  // 显示分割线
  },
  // 下拉选择
  select: {
    maxOptions: 10,  // 单列最多 10 个选项
    searchable: true,  // 支持搜索
    allowClear: true,  // 支持清除
  },
  // 分页
  pagination: {
    defaultPageSize: 20,
    pageSizeOptions: [10, 20, 50, 100],
    showSizeChanger: true,
    showQuickJumper: false,
  },
};
```

### 表单验证

```typescript
// 验证规则
export const validationRules = {
  email: /^\S+@\S+\.\S+$/,
  phone: /^1[3-9]\d{9}$/,
  idCard: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
};
```

---

## 🪟 模态框规范

```typescript
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
  confirmDisabled: false,  // 默认禁用确认按钮
};
```

### 模态框使用规范

| 场景 | 建议 |
|------|------|
| 简单确认 | 使用 Alert 组件 |
| 编辑内容 < 50 行 | 使用 Modal 模态框 |
| 编辑内容 > 50 行 | 使用新页面 |
| 复杂表单 | 使用新页面 |

---

## 📋 表格规范

```typescript
export const tableSpecs = {
  defaultPagination: 20,
  pageSizeOptions: [10, 20, 50, 100],
  scroll: {
    x: 1400,
    y: 300,
  },
  column: {
    minWidth: 80,
    resizable: true,
  },
  actions: {
    align: 'center',
    fixed: 'right',
  },
};
```

### 表格状态

| 状态 | 说明 | 展示方式 |
|------|------|----------|
| 有数据 | 正常展示 | 表格 + 分页 |
| 空数据 | 无数据 | Empty 组件 |
| 加载中 | 加载中 | Loading + 骨架屏 |
| 错误 | 请求失败 | Error 组件 + 重试 |

---

## 🎭 空状态规范

```typescript
export const emptyState = {
  icon: 'inbox',  // 图标
  title: '暂无数据',  // 标题
  description: '暂无数据，请先创建',  // 描述
  action: 'create',  // 操作按钮
};
```

### 空状态使用场景

- 列表无数据
- 搜索无结果
- 功能未使用
- 首次进入页面

---

## 📱 响应式设计

### 断点配置

```typescript
export const breakpoints = {
  mobile: 576,   // 手机
  tablet: 768,   // 平板
  desktop: 992,  // 桌面
  large: 1200,   // 大桌面
};
```

### 响应式策略

| 断点 | 布局 | 说明 |
|------|------|------|
| < 576px | 单列 | 手机横竖屏统一 |
| 576px - 768px | 单列 | 平板竖屏 |
| 768px - 992px | 双列 | 平板横屏 |
| ≥ 992px | 多列 + 侧边栏 | 桌面端 |

---

## 🔒 可访问性规范

### WCAG 2.1 AA 标准

| 要求 | 说明 | 值 |
|------|------|--|
| 对比度 | 正常文本 ≥ 4.5:1<br>大号文本 ≥ 3:1 | 主色/白色：4.6:1 ✅<br>灰色 900/白色：12.6:1 ✅ |
| 键盘导航 | 所有功能可通过键盘操作 | tabindex-auto: true ✅ |
| 焦点可见 | 焦点状态有明显视觉提示 | focus-outline: 2px ✅ |
| 屏幕阅读器 | 为图片、图标添加 aria-label | aria-hidden: false ✅ |

### 对比度要求

```typescript
// 必须满足 WCAG 2.1 AA 标准
contrast-ratio-primary-on-white: 4.6:1  ✅
contrast-ratio-gray-900-on-white: 12.6:1  ✅
```

### 禁止项

- ❌ 禁止对比度低于 4.5:1
- ❌ 禁止不使用 aria-label 标注图标
- ❌ 禁止不使用焦点可见样式
- ❌ 禁止不验证键盘导航

---

## 📦 组件库

### 基础组件

| 组件 | 文件路径 | 说明 |
|------|----------|------|
| Button | design/components/Button.tsx | 按钮组件 |
| Input | design/components/Input.tsx | 输入框组件 |
| Select | design/components/Select.tsx | 下拉选择 |
| Table | design/components/Table.tsx | 表格组件 |
| Modal | design/components/Modal.tsx | 模态框组件 |
| Card | design/components/Card.tsx | 卡片组件 |
| Empty | design/components/Empty.tsx | 空状态组件 |
| Loading | design/components/Loading.tsx | 加载组件 |
| Toast | design/components/Toast.tsx | 消息提示 |

### 业务组件

```
src/components/business/
├── Dashboard/        # 仪表盘
├── User/             # 用户管理
├── Project/          # 项目管理
└── Settings/         # 设置页面
```

---

## 🎨 图标规范

### 图标使用

```typescript
// 图标组件库
import {
  HomeOutlined,
  SettingOutlined,
  UserOutlined,
  InboxOutlined,
} from '@ant-design/icons';

// 使用方式
<HomeOutlined style={{ fontSize: 16 }} />
```

### 图标大小

| 场景 | 大小 | 说明 |
|------|------|------|
| 导航栏 | 18-20px | 导航图标 |
| 操作按钮 | 14-16px | 小图标 |
| 状态图标 | 12-14px | 小状态 |

### 禁止项汇总

```typescript
// 禁止行为
❌ 直接使用 hex 颜色值（除调试外）
❌ 使用非 4px 倍数的间距
❌ 使用 px 单位定义 rem 可替代的值
❌ 混用多种蓝色系
❌ 使用非标准字号（13px, 15px）
❌ 点击反馈小于 100ms
❌ 不使用 aria-label 标注图标
❌ 对比度低于 4.5:1
❌ 不使用设计系统令牌
❌ 硬编码阴影和圆角
❌ 不使用语义化命名
```

---

## 📋 设计令牌

### 设计令牌定义

```typescript
// design/theme/tokens.ts
export const tokens = {
  // 色彩令牌
  colors: {
    primary: {
      50: '#E3F2FD',
      // ...
    },
  },
  // 排版令牌
  typography: {
    fontSize: {
      // ...
    },
  },
  // 间距令牌
  spacing: {
    // ...
  },
};
```

### 主题切换

```typescript
// 支持浅色/深色主题
export const themes = {
  light: {
    // 浅色主题配置
  },
  dark: {
    // 深色主题配置
  },
};
```

---

## ✅ 检查清单

在开发前检查：

- [ ] 使用设计系统中的色值
- [ ] 使用标准化的间距
- [ ] 遵循组件规范
- [ ] 保证对比度达标
- [ ] 提供键盘导航支持
- [ ] 添加加载状态
- [ ] 处理空状态
- [ ] 提供错误提示
- [ ] 响应式适配

---

[回到文档根目录](../README.md)
