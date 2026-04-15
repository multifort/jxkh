# UI Design Rules for Cursor

## 1. 色彩规范 (Color Rules)

### 1.1 主色系统
```typescript
// 必须使用设计系统主色，禁止使用其他蓝色系
primary-color: #1677ff → 主色 #2196F3 (Blue 500)
primary-hover: #4096ff → 主色悬停 #1E88E5
primary-active: #1890ff → 主色激活 #1976D2

// 色阶必须使用语义化命名
primary-100: #E3F2FD
primary-200: #BBDEFB
primary-300: #90CAF9
primary-400: #64B5F6
primary-500: #42A5F5
primary-600: #4096ff
primary-700: #1976D2
primary-800: #1565C0
primary-900: #0D47A1
```

### 1.2 功能色
```typescript
// 成功色固定值
success: #52c41a (禁止使用 #4CAF50)

// 警告色固定值
warning: #fa8c16 (禁止使用 #FF9800)

// 错误色固定值
error: #f5222d (禁止使用 #F44336)

// 信息色固定值
info: #1677ff
```

### 1.3 中性色
```typescript
// 使用语义化命名
text-primary: #000000 (gray-900)
text-secondary: #606266 (gray-700)
text-placeholder: #A0AEC0 (gray-500)
text-disabled: #B0B3B8 (gray-400)

// 背景色
bg-page: #F5F7FA (gray-50)
bg-container: #FFFFFF (white)
bg-hover: #F5F5F5 (gray-100)
bg-border: #EBEEF5 (gray-200)
```

### 1.4 禁止项
- ❌ 禁止直接在 JSX 中使用 hex 颜色值（除调试外）
- ❌ 禁止混用多种蓝色系
- ❌ 禁止使用非设计系统色值

---

## 2. 排版规范 (Typography Rules)

### 2.1 字体栈
```typescript
// 必须使用系统字体栈
font-family-sans: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Helvetica', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'sans-serif'
font-family-mono: 'SF Mono', 'SFMono-Regular', 'Menlo', 'Monaco', 'Consolas', 'Ubuntu Mono', 'monospace'
```

### 2.2 字号规范
```typescript
// 禁止使用 px 单位，必须使用 rem
font-size-xs: 0.75rem (12px)
font-size-sm: 0.875rem (14px)
font-size-base: 1rem (16px)
font-size-lg: 1.125rem (18px)
font-size-xl: 1.25rem (20px)
font-size-2xl: 1.5rem (24px)
font-size-3xl: 1.75rem (28px)
font-size-4xl: 2rem (32px)

// 标题层次
h1: 2rem / 500
h2: 1.5rem / 600
h3: 1.25rem / 600
h4: 1rem / 600
```

### 2.3 字重规范
```typescript
font-weight-normal: 400
font-weight-medium: 500
font-weight-semibold: 600
font-weight-bold: 700
```

### 2.4 行高规范
```typescript
line-height-tight: 1.25
line-height-snug: 1.375
line-height-normal: 1.5
line-height-relaxed: 1.625
```

### 2.5 禁止项
- ❌ 禁止使用 px 单位定义字体大小
- ❌ 禁止使用非标准字号（如 13px, 15px）
- ❌ 禁止在同一层级混用多套字号

---

## 3. 间距规范 (Spacing Rules)

### 3.1 间距阶梯
```typescript
// 基于 4px 基数的间距系统
space-0: 0
space-px: 1px
space-0-5: 0.125rem (2px)
space-1: 0.25rem (4px)
space-1-5: 0.375rem (6px)
space-2: 0.5rem (8px)
space-2-5: 0.625rem (10px)
space-3: 0.75rem (12px)
space-3-5: 0.875rem (14px)
space-4: 1rem (16px)
space-5: 1.25rem (20px)
space-6: 1.5rem (24px)
space-8: 2rem (32px)
space-10: 2.5rem (40px)
space-12: 3rem (48px)
```

### 3.2 使用场景
```typescript
// 组件内部间距
component-padding: space-2 (8px)
component-padding-lg: space-3 (12px)

// 组件外部间距
element-gap: space-3 (12px)
section-gap: space-6 (24px)
page-section-gap: space-8 (32px)

// 卡片内边距
card-padding: space-4 (16px)
card-padding-lg: space-6 (24px)
```

### 3.3 禁止项
- ❌ 禁止使用非 4px 倍数的间距
- ❌ 禁止混用 px 和 rem
- ❌ 禁止硬编码间距值

---

## 4. 圆角规范 (Border Radius)

### 4.1 圆角系统
```typescript
radius-none: 0
radius-sm: 2px
radius: 6px (默认圆角)
radius-md: 8px
radius-lg: 12px
radius-xl: 16px
radius-2xl: 24px
radius-full: 9999px
```

### 4.2 使用场景
```typescript
// 按钮圆角
button-radius: radius (6px)

// 卡片圆角
card-radius: radius-md (8px)

// 输入框圆角
input-radius: radius (6px)

// 弹窗圆角
modal-radius: radius-lg (12px)
```

---

## 5. 阴影规范 (Shadow)

### 5.1 阴影系统
```typescript
shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05)  // 轻微阴影
shadow: 0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06)  // 默认阴影
shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1)  // 中等阴影
shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1)  // 大阴影
shadow-xl: 0 20px 25px rgba(0, 0, 0, 0.15)  // 超大阴影
```

### 5.2 使用场景
```typescript
// 卡片阴影
card-shadow: shadow-sm

// 弹窗阴影
modal-shadow: shadow-lg
```

---

## 6. 边框规范 (Border)

### 6.1 边框规范
```typescript
// 边框颜色统一使用
border-color: #EBEEF5

// 边框宽度
border-width-0: 0
border-width: 1px
border-width-2: 2px

// 使用场景
card-border: border
input-border: border
divider-border: border-t
```

---

## 7. 交互规范 (Interaction)

### 7.1 动画时长
```typescript
// 禁止使用 ms 单位，必须使用 ms
duration-fast: 150ms
duration-normal: 200ms
duration-slow: 300ms

// 禁止项
// ❌ 点击反馈 8-12ms → 必须是 150ms 或更长
// ❌ 悬停效果 150-300ms → 统一使用 200ms
```

### 7.2 过渡函数
```typescript
transition-ease: ease-in-out  // 默认缓动
transition-ease-in: ease-in
transition-ease-out: ease-out
transition-ease-in-back: ease-in-back
transition-bounce: bounce
```

---

## 8. 布局规范 (Layout)

### 8.1 栅格系统
```typescript
// 12 列栅格
columns: 12
gutter: 24px  // 必须使用 space-6

// 断点
breakpoint-xs: 0  // < 576px
breakpoint-sm: 576px  // ≥ 576px
breakpoint-md: 768px  // ≥ 768px
breakpoint-lg: 992px  // ≥ 992px
breakpoint-xl: 1200px  // ≥ 1200px
breakpoint-2xl: 1600px  // ≥ 1600px
```

### 8.2 容器宽度
```typescript
container-sm: 540px
container-md: 720px
container-lg: 960px
container-xl: 1200px
container-2xl: 1440px
```

### 8.3 页面结构
```typescript
// 标准页面布局
header-height: 60px  // 固定高度
main-padding: space-4 (16px)
footer-height: 48px  // 固定高度
sidebar-width: 240px  // 固定宽度
```

---

## 9. 组件规范 (Components)

### 9.1 按钮 (Button)
```typescript
// 类型
type-primary: 主操作
type-default: 次操作
type-text: 文本按钮
type-ghost: 幽灵按钮

// 尺寸
size-sm: 高度 24px
size: 高度 32px (默认)
size-lg: 高度 40px

// 禁用状态
disabled-opacity: 0.5
disabled-cursor: not-allowed
```

### 9.2 输入框 (Input)
```typescript
// 高度
input-height-min: 40px  // 最小高度
input-height: 32px  // 内边距计算

// 圆角
input-radius: radius (6px)

// 聚焦边框
input-focus-border: primary-color
input-focus-shadow: shadow
```

### 9.3 卡片 (Card)
```typescript
// 内边距
card-padding: space-4 (16px)
card-padding-lg: space-6 (24px)

// 圆角
card-radius: radius-md (8px)

// 阴影
card-shadow: shadow-sm

// 背景
card-bg: white
```

### 9.4 表格 (Table)
```typescript
// 行高
table-row-height: 40px
table-row-hover-bg: #F5F7FA

// 边框
table-border-color: #EBEEF5

// 分页
pagination-size: 20  // 默认每页 20 条
pagination-options: [10, 20, 50, 100]
```

### 9.5 模态框 (Modal)
```typescript
// 宽度
modal-width-sm: 400px
modal-width: 600px  // 默认
modal-width-lg: 800px

// 圆角
modal-radius: radius-lg (12px)

// 遮罩
modal-backdrop: backdrop-blur-sm
modal-close-on-backdrop: false
```

---

## 10. 表单规范 (Form)

### 10.1 表单组件
```typescript
// 标签
label-margin-bottom: space-1 (4px)

// 输入框
input-margin-top: space-1 (4px)
input-placeholder-color: text-placeholder

// 错误提示
error-margin-top: space-0-5 (2px)
error-color: error
error-font-size: font-size-xs
```

### 10.2 验证
```typescript
// 同步验证
validate-on-blur: true
validate-on-change: false  // 避免频繁触发

// 异步验证
debounce-time: 500ms
```

---

## 11. 响应式规范 (Responsive)

### 11.1 断点策略
```typescript
// 移动优先
mobile-first: true

// 断点
xs: < 576px    // 手机
sm: 576px - 768px  // 平板竖屏
md: 768px - 992px  // 平板横屏
lg: 992px - 1200px  // 桌面
xl: ≥ 1200px  // 大桌面
```

### 11.2 响应式策略
```typescript
// 移动端默认隐藏的元素
mobile-hide: @media (min-width: 768px) { display: none }

// 桌面端默认隐藏的元素
desktop-hide: @media (max-width: 767px) { display: none }
```

---

## 12. 可访问性规范 (Accessibility)

### 12.1 对比度
```typescript
// 必须满足 WCAG 2.1 AA 标准
// 正常文本：对比度 ≥ 4.5:1
// 大号文本：对比度 ≥ 3:1

// 对比度检查
contrast-ratio-primary-on-white: 4.6:1  ✅
contrast-ratio-gray-900-on-white: 12.6:1  ✅
```

### 12.2 焦点可见
```typescript
// 焦点样式
focus-outline: 2px solid primary-color
focus-outline-offset: 2px
focus-ring: ring-2 ring-primary ring-opacity-50
```

### 12.3 键盘导航
```typescript
// 所有可交互元素必须支持键盘操作
tabindex-auto: true
keyboard-focus-visible: true
```

### 12.4 屏幕阅读器
```typescript
// 为图标添加语义标签
aria-label: "首页"
aria-hidden: false  // 可见的图标必须设置
```

---

## 13. 暗黑模式规范 (Dark Mode)

### 13.1 色彩映射
```typescript
// 浅色模式 → 深色模式
bg-page: #F5F7FA → #1F2329
bg-container: #FFFFFF → #1F2329
text-primary: #000000 → #EAECEF
text-secondary: #606266 → #A6A6AD

// 主色映射
primary: #2196F3 → #2196F3 (保持不变)
```

---

## 14. 禁止项汇总

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

## 15. 使用指南

### 15.1 导入设计系统
```typescript
// 必须从设计系统导入
import { colors } from '@/design/theme/colors'
import { spacing } from '@/design/theme/spacing'
import { radius } from '@/design/theme/radius'
import { shadow } from '@/design/theme/shadow'
```

### 15.2 样式编写
```typescript
// 推荐：使用 CSS 变量
.style {
  background-color: var(--color-primary-500);
  color: var(--text-primary);
  padding: var(--space-4);
  border-radius: var(--radius);
}

// 不推荐：硬编码值
.style {
  background-color: '#2196F3';  // ❌
  color: '#000000';  // ❌
  padding: '16px';  // ❌
}
```

### 15.3 JSX 中使用
```typescript
// 推荐：使用组件
<Button type="primary">提交</Button>

// 不推荐：使用内联样式
<button style={{
  backgroundColor: '#2196F3',  // ❌
  color: 'white',
  padding: '8px 16px',  // ❌
}}>提交</button>
```

---

## 16. 检查清单

开发前检查：

- [ ] 使用设计系统色值
- [ ] 使用标准化的间距
- [ ] 使用组件库组件
- [ ] 保证对比度达标
- [ ] 提供键盘导航支持
- [ ] 添加加载状态
- [ ] 处理空状态
- [ ] 响应式适配
- [ ] 无障碍访问
- [ ] 暗黑模式支持

---

[回到文档根目录](../../README.md)
