# UI 设计文档

本目录包含企业绩效考核系统的前端 UI/UX 设计规范和组件指南。

## 📂 文档列表

- [PROJECT-TEMPLATE-UI.md](PROJECT-TEMPLATE-UI.md) - UI/UX 设计规范（色彩、排版、组件、交互）

## 📋 文档说明

### PROJECT-TEMPLATE-UI.md

完整的 UI/UX 设计规范文档，包含：

- **设计原则** - 一致性、简洁性、可访问性、响应性
- **色彩系统** - 品牌色、中性色、语义色
- **排版系统** - 字体、字号、字重、行高
- **间距系统** - 标准化间距
- **交互规范** - 动画时长、反馈提示
- **布局规范** - 栅格系统、断点配置
- **组件库** - 基础组件和业务组件
- **表单规范** - 表单组件和验证
- **表格规范** - 表格组件和状态
- **模态框规范** - 模态框使用指南
- **空状态规范** - 空状态展示
- **响应式设计** - 多端适配
- **可访问性** - WCAG 2.1 AA 标准
- **图标规范** - 图标使用指南
- **设计令牌** - 设计令牌定义
- **检查清单** - 开发前检查项

## 🎨 快速参考

### 色彩

```typescript
import { colors } from '@/design/theme/colors';

// 主色
colors.primary[500]  // #2196F3
// 成功色
colors.success        // #4CAF50
// 错误色
colors.error          // #F44336
```

### 间距

```typescript
import { spacing } from '@/design/theme/spacing';

// 标准间距
spacing.md  // 1rem (16px)
spacing.lg  // 1.5rem (24px)
```

### 断点

```typescript
import { breakpoints } from '@/design/theme/breakpoints';

// 断点
breakpoints.mobile   // 576px
breakpoints.tablet   // 768px
breakpoints.desktop  // 992px
```

## 🚀 使用示例

### 按钮组件

```tsx
import { Button } from '@/design/components/Button';

<Button type="primary">主要操作</Button>
<Button type="default">次要操作</Button>
```

### 输入框组件

```tsx
import { Input } from '@/design/components/Input';

<Input label="用户名" placeholder="请输入用户名" />
```

## 📚 相关文档

- [项目模板指南](../PROJECT-TEMPLATE.md) - 项目创建和开发指南
- [架构规范](../../CLAUDE.md) - 架构设计和 AI 行为约束
- [部署文档](../deployment/README.md) - 部署和运维指南

---

[回到文档根目录](../README.md)
