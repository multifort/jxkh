# 前端测试指南

## 📋 概述

本文档提供 React + TypeScript 前端项目的测试规范和最佳实践，包括组件测试、Hooks 测试和 E2E 测试。

---

## 🏗️ 测试结构

```
frontend/
├── src/
│   ├── components/
│   │   └── LoginForm.tsx
│   ├── hooks/
│   │   └── usePerformancePlan.ts
│   ├── services/
│   │   └── apiService.ts
│   └── __tests__/
│       ├── components/        # 组件测试
│       │   └── LoginForm.test.tsx
│       ├── hooks/             # Hooks 测试
│       │   └── usePerformancePlan.test.ts
│       └── services/          # 服务层测试
│           └── apiService.test.ts
├── vitest.config.ts
└── package.json
```

---

## 🔧 依赖配置

### package.json

```json
{
  "devDependencies": {
    "vitest": "^1.2.0",
    "@testing-library/react": "^14.2.0",
    "@testing-library/jest-dom": "^6.4.0",
    "@testing-library/user-event": "^14.5.0",
    "msw": "^2.1.0",
    "jsdom": "^24.0.0"
  }
}
```

### vitest.config.ts

```typescript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/__tests__/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/__tests__/',
        '**/*.d.ts',
        '**/*.config.ts',
      ],
    },
  },
});
```

---

## ✅ 组件测试规范

### 1. 表单组件测试

**原则**：
- 测试用户交互行为（点击、输入、提交）
- 验证表单验证逻辑
- 测试加载状态和错误提示

**示例**：

```tsx
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from '../LoginForm';

describe('LoginForm', () => {
  const mockOnSubmit = vi.fn();
  
  beforeEach(() => {
    mockOnSubmit.mockClear();
  });
  
  test('should render login form', () => {
    render(<LoginForm onSubmit={mockOnSubmit} />);
    
    expect(screen.getByLabelText(/用户名/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/密码/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /登录/i })).toBeInTheDocument();
  });
  
  test('should show error when username is empty', async () => {
    const user = userEvent.setup();
    render(<LoginForm onSubmit={mockOnSubmit} />);
    
    await user.click(screen.getByRole('button', { name: /登录/i }));
    
    expect(await screen.findByText(/用户名不能为空/i)).toBeInTheDocument();
  });
  
  test('should call onSubmit with credentials', async () => {
    const user = userEvent.setup();
    render(<LoginForm onSubmit={mockOnSubmit} />);
    
    await user.type(screen.getByLabelText(/用户名/i), 'admin');
    await user.type(screen.getByLabelText(/密码/i), 'password123');
    await user.click(screen.getByRole('button', { name: /登录/i }));
    
    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        username: 'admin',
        password: 'password123',
      });
    });
  });
  
  test('should show loading state during submission', async () => {
    const user = userEvent.setup();
    mockOnSubmit.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
    
    render(<LoginForm onSubmit={mockOnSubmit} />);
    
    await user.type(screen.getByLabelText(/用户名/i), 'admin');
    await user.type(screen.getByLabelText(/密码/i), 'password123');
    await user.click(screen.getByRole('button', { name: /登录/i }));
    
    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByText(/登录中/i)).toBeInTheDocument();
  });
});
```

---

### 2. 展示型组件测试

**示例**：

```tsx
import { render, screen } from '@testing-library/react';
import { PerformanceCard } from '../PerformanceCard';

describe('PerformanceCard', () => {
  test('should display performance data', () => {
    const props = {
      name: '张三',
      score: 85.5,
      level: 'A',
      department: '技术部',
    };
    
    render(<PerformanceCard {...props} />);
    
    expect(screen.getByText('张三')).toBeInTheDocument();
    expect(screen.getByText('85.5')).toBeInTheDocument();
    expect(screen.getByText('A')).toBeInTheDocument();
    expect(screen.getByText('技术部')).toBeInTheDocument();
  });
  
  test('should apply correct color based on level', () => {
    const { container } = render(
      <PerformanceCard name="李四" score={90} level="A" department="销售部" />
    );
    
    const card = container.querySelector('.performance-card');
    expect(card).toHaveClass('level-a');
  });
  
  test('should show warning icon when score is low', () => {
    render(
      <PerformanceCard name="王五" score={65} level="C" department="运营部" />
    );
    
    expect(screen.getByTestId('warning-icon')).toBeInTheDocument();
  });
});
```

---

### 3. 条件渲染测试

**示例**：

```tsx
test('should show empty state when no data', () => {
  render(<PlanList plans={[]} />);
  
  expect(screen.getByText(/暂无绩效计划/i)).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /创建计划/i })).toBeInTheDocument();
});

test('should show plan list when data exists', () => {
  const plans = [
    { id: 1, name: 'Q1计划' },
    { id: 2, name: 'Q2计划' },
  ];
  
  render(<PlanList plans={plans} />);
  
  expect(screen.getByText('Q1计划')).toBeInTheDocument();
  expect(screen.getByText('Q2计划')).toBeInTheDocument();
});
```

---

## 🎣 Hooks 测试规范

### 1. 自定义 Hooks 测试

**原则**：
- 使用 `renderHook` 测试 Hooks
- 测试初始状态
- 测试状态变化
- 测试副作用（useEffect）

**示例**：

```tsx
import { renderHook, act } from '@testing-library/react';
import { usePerformancePlan } from '../usePerformancePlan';

describe('usePerformancePlan', () => {
  test('should initialize with default values', () => {
    const { result } = renderHook(() => usePerformancePlan());
    
    expect(result.current.plan).toBeNull();
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
  });
  
  test('should fetch plan successfully', async () => {
    const mockPlan = { id: 1, name: 'Q1计划' };
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve(mockPlan),
      })
    ) as any;
    
    const { result } = renderHook(() => usePerformancePlan());
    
    await act(async () => {
      await result.current.fetchPlan(1);
    });
    
    expect(result.current.plan).toEqual(mockPlan);
    expect(result.current.loading).toBe(false);
  });
  
  test('should handle fetch error', async () => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: false,
        status: 404,
      })
    ) as any;
    
    const { result } = renderHook(() => usePerformancePlan());
    
    await act(async () => {
      await result.current.fetchPlan(999);
    });
    
    expect(result.current.error).toBe('Plan not found');
    expect(result.current.loading).toBe(false);
  });
  
  test('should update plan', async () => {
    const { result } = renderHook(() => usePerformancePlan());
    
    await act(async () => {
      result.current.updatePlan({ name: 'Updated Plan' });
    });
    
    expect(result.current.plan?.name).toBe('Updated Plan');
  });
});
```

---

### 2. useEffect 测试

**示例**：

```tsx
test('should fetch data on mount', async () => {
  const mockData = [{ id: 1, name: 'Item 1' }];
  global.fetch = vi.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve(mockData),
    })
  ) as any;
  
  const { result } = renderHook(() => useDataFetcher('/api/items'));
  
  await waitFor(() => {
    expect(result.current.data).toEqual(mockData);
  });
  
  expect(global.fetch).toHaveBeenCalledWith('/api/items');
});
```

---

## 🌐 服务层测试（API Mock）

### 使用 MSW 模拟 API

**安装**：
```bash
npm install msw --save-dev
```

**配置 handlers**：

```typescript
// src/__tests__/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('/api/plans/:id', ({ params }) => {
    return HttpResponse.json({
      id: params.id,
      name: 'Q1绩效计划',
      status: 'IN_PROGRESS',
    });
  }),
  
  http.post('/api/plans', async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json(
      { id: 1, ...body },
      { status: 201 }
    );
  }),
  
  http.get('/api/users', () => {
    return HttpResponse.json([
      { id: 1, name: '张三' },
      { id: 2, name: '李四' },
    ]);
  }),
];
```

**测试示例**：

```typescript
import { setupServer } from 'msw/node';
import { handlers } from './mocks/handlers';
import { fetchPlan } from '../apiService';

const server = setupServer(...handlers);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('apiService', () => {
  test('should fetch plan by id', async () => {
    const plan = await fetchPlan(1);
    
    expect(plan).toEqual({
      id: '1',
      name: 'Q1绩效计划',
      status: 'IN_PROGRESS',
    });
  });
  
  test('should handle 404 error', async () => {
    server.use(
      http.get('/api/plans/:id', () => {
        return new HttpResponse(null, { status: 404 });
      })
    );
    
    await expect(fetchPlan(999)).rejects.toThrow('Plan not found');
  });
});
```

---

## 🎭 E2E 测试（Playwright）

### 配置

```typescript
// playwright.config.ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 30000,
  use: {
    baseURL: 'http://localhost:3000',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { browserName: 'chromium' },
    },
    {
      name: 'firefox',
      use: { browserName: 'firefox' },
    },
  ],
});
```

### 测试示例

```typescript
// e2e/login.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Login Flow', () => {
  test('should login successfully with valid credentials', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('#username', 'admin');
    await page.fill('#password', 'password123');
    await page.click('button[type="submit"]');
    
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('.welcome-message')).toContainText('欢迎');
  });
  
  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('#username', 'invalid');
    await page.fill('#password', 'wrong');
    await page.click('button[type="submit"]');
    
    await expect(page.locator('.error-message')).toContainText('用户名或密码错误');
  });
});

// e2e/performance-plan.spec.ts
test('should create performance plan', async ({ page }) => {
  // 登录
  await page.goto('/login');
  await page.fill('#username', 'employee01');
  await page.fill('#password', 'password123');
  await page.click('button[type="submit"]');
  
  // 创建计划
  await page.click('text=创建绩效计划');
  await page.fill('#plan-name', 'Q1绩效计划');
  await page.fill('#indicator-name', '销售额');
  await page.fill('#target-value', '1000000');
  await page.click('text=提交');
  
  // 验证成功
  await expect(page.locator('.success-message')).toBeVisible();
  await expect(page.locator('.plan-name')).toContainText('Q1绩效计划');
});
```

---

## 🎯 测试最佳实践

### 1. 测试用户行为，而非实现细节

```tsx
// ❌ 错误：测试实现细节
test('should set state correctly', () => {
  const { container } = render(<Component />);
  expect(container.querySelector('.hidden')).toBeNull();
});

// ✅ 正确：测试用户可见的行为
test('should show success message after submit', async () => {
  render(<Component />);
  await userEvent.click(screen.getByRole('button', { name: /提交/i }));
  expect(await screen.findByText(/提交成功/i)).toBeInTheDocument();
});
```

### 2. 使用语义化查询

```tsx
// ❌ 避免使用 CSS 类名
screen.getByClassName('.btn-primary');

// ✅ 使用语义化查询
screen.getByRole('button', { name: /提交/i });
screen.getByLabelText(/用户名/i);
screen.getByPlaceholderText(/请输入邮箱/i);
```

### 3. 异步操作使用 waitFor

```tsx
// ❌ 不要使用 setTimeout
await new Promise(resolve => setTimeout(resolve, 100));

// ✅ 使用 waitFor
await waitFor(() => {
  expect(screen.getByText(/加载完成/i)).toBeInTheDocument();
});
```

### 4. 清理副作用

```tsx
afterEach(() => {
  vi.clearAllMocks();
  localStorage.clear();
});
```

---

## 🚫 常见陷阱

### 1. 测试过于脆弱

```tsx
// ❌ 依赖具体的文本内容
expect(screen.getByText('您有3条新消息')).toBeInTheDocument();

// ✅ 使用更稳定的选择器
expect(screen.getByTestId('notification-badge')).toHaveTextContent('3');
```

### 2. 忽略无障碍性

```tsx
// ❌ 按钮没有可访问的标签
<button onClick={handleClick}>✓</button>

// ✅ 添加 aria-label
<button onClick={handleClick} aria-label="确认">✓</button>
```

### 3. 过度 Mock

```tsx
// ❌ Mock 了太多内部实现
vi.mock('../utils', () => ({
  formatDate: vi.fn(),
  calculateScore: vi.fn(),
}));

// ✅ 只 Mock 外部依赖（API、第三方库）
global.fetch = vi.fn();
```

---

## 📊 测试覆盖率报告

### 生成报告

```bash
# 运行测试并生成覆盖率报告
npm test -- --coverage

# 查看 HTML 报告
open coverage/index.html
```

### 配置覆盖率阈值

```json
// package.json
{
  "vitest": {
    "coverage": {
      "thresholds": {
        "lines": 70,
        "branches": 60,
        "functions": 70,
        "statements": 70
      }
    }
  }
}
```

---

## 📚 相关文档

- [测试策略总纲](./testing-strategy.md)
- [测试数据管理](./test-data-management.md)
- [CI/CD 集成配置](./ci-cd-integration.md)

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 前端开发团队
