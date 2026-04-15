# 前端细粒度权限控制使用指南

## 📚 概述

本系统提供了三种方式进行前端权限控制：

1. **PermissionGuard 组件** - 声明式权限控制（推荐）
2. **usePermission Hook** - 编程式权限检查
3. **路由级别保护** - 页面级权限控制

---

## 🎯 1. PermissionGuard 组件（推荐）

### 基本用法

```tsx
import PermissionGuard from '@/components/common/PermissionGuard';

// 单个权限检查
<PermissionGuard permission="user:create">
  <Button type="primary">创建用户</Button>
</PermissionGuard>

// 无权限时不显示任何内容
```

### 任一权限（OR 逻辑）

```tsx
// 用户拥有 user:create 或 user:edit 任一权限即可看到按钮
<PermissionGuard anyOf={['user:create', 'user:edit']}>
  <Button>编辑或创建</Button>
</PermissionGuard>
```

### 所有权限（AND 逻辑）

```tsx
// 用户必须同时拥有两个权限才能看到按钮
<PermissionGuard allOf={['user:view', 'user:export']}>
  <Button>导出数据</Button>
</PermissionGuard>
```

### 自定义无权限提示

```tsx
<PermissionGuard 
  permission="admin:only" 
  fallback={<div>您没有权限执行此操作</div>}
>
  <Button>管理员功能</Button>
</PermissionGuard>
```

### 组合使用

```tsx
// 需要 user:view 权限，并且还需要 create 或 edit 任一权限
<PermissionGuard 
  permission="user:view"
  anyOf={['user:create', 'user:edit']}
>
  <Button>操作</Button>
</PermissionGuard>
```

---

## 🔧 2. usePermission Hook

### 基本用法

```tsx
import { usePermission } from '@/hooks/usePermission';

function MyComponent() {
  const canCreate = usePermission('user:create');
  
  return (
    <div>
      {canCreate && <Button>创建用户</Button>}
    </div>
  );
}
```

### 条件渲染

```tsx
import { usePermission } from '@/hooks/usePermission';

function UserTable({ record }) {
  const canEdit = usePermission('user:edit');
  const canDelete = usePermission('user:delete');
  
  return (
    <Space>
      {canEdit && <Button onClick={() => handleEdit(record)}>编辑</Button>}
      {canDelete && <Button danger onClick={() => handleDelete(record)}>删除</Button>}
    </Space>
  );
}
```

---

## 🛡️ 3. 多权限检查 Hooks

### useAnyPermission - 任一权限

```tsx
import { useAnyPermission } from '@/hooks/usePermission';

function MyComponent() {
  // 用户拥有任一权限即返回 true
  const hasPermission = useAnyPermission(['user:create', 'user:edit', 'user:delete']);
  
  return hasPermission ? <AdminPanel /> : null;
}
```

### useAllPermissions - 所有权限

```tsx
import { useAllPermissions } from '@/hooks/usePermission';

function MyComponent() {
  // 用户必须拥有所有权限才返回 true
  const hasPermission = useAllPermissions(['user:view', 'user:export']);
  
  return hasPermission ? <ExportButton /> : null;
}
```

---

## 📝 实际应用示例

### 示例 1：用户管理页面按钮权限

```tsx
import PermissionGuard from '@/components/common/PermissionGuard';

function UserManagePage() {
  return (
    <Card title="用户管理">
      {/* 工具栏 */}
      <div style={{ marginBottom: 16 }}>
        <PermissionGuard permission="user:create">
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增用户
          </Button>
        </PermissionGuard>
      </div>
      
      {/* 表格操作列 */}
      <Table
        columns={[
          {
            title: '操作',
            render: (_, record) => (
              <Space>
                <PermissionGuard permission="user:edit">
                  <Button size="small" onClick={() => handleEdit(record)}>
                    编辑
                  </Button>
                </PermissionGuard>
                
                <PermissionGuard permission="user:reset-password">
                  <Button size="small" onClick={() => handleResetPassword(record)}>
                    重置密码
                  </Button>
                </PermissionGuard>
                
                <PermissionGuard permission="user:delete">
                  <Popconfirm onConfirm={() => handleDelete(record.id)}>
                    <Button size="small" danger>删除</Button>
                  </Popconfirm>
                </PermissionGuard>
              </Space>
            ),
          },
        ]}
      />
    </Card>
  );
}
```

### 示例 2：角色管理页面

```tsx
function RoleManagePage() {
  return (
    <Card title="角色管理">
      <PermissionGuard permission="role:create">
        <Button type="primary" onClick={handleAdd}>新增角色</Button>
      </PermissionGuard>
      
      <Table
        columns={[
          {
            title: '操作',
            render: (_, record) => (
              <Space>
                <PermissionGuard permission="role:edit">
                  <Button onClick={() => handleEdit(record)}>编辑</Button>
                </PermissionGuard>
                
                <PermissionGuard permission="role:assign-permission">
                  <Button onClick={() => handleAssignPermissions(record)}>
                    分配权限
                  </Button>
                </PermissionGuard>
                
                <PermissionGuard permission="role:delete">
                  <Popconfirm onConfirm={() => handleDelete(record.id)}>
                    <Button danger>删除</Button>
                  </Popconfirm>
                </PermissionGuard>
              </Space>
            ),
          },
        ]}
      />
    </Card>
  );
}
```

### 示例 3：菜单权限控制

```tsx
// 在 MainLayout.tsx 中
import PermissionGuard from '@/components/common/PermissionGuard';

const menuItems = [
  {
    key: '/settings',
    label: '系统设置',
    children: [
      {
        key: '/settings/users',
        label: (
          <PermissionGuard permission="user:view">
            用户管理
          </PermissionGuard>
        ),
      },
      {
        key: '/settings/roles',
        label: (
          <PermissionGuard permission="role:view">
            角色管理
          </PermissionGuard>
        ),
      },
      {
        key: '/settings/permissions',
        label: (
          <PermissionGuard permission="permission:view">
            权限管理
          </PermissionGuard>
        ),
      },
    ],
  },
];
```

---

## ⚡ 性能优化建议

### 1. 避免频繁调用

```tsx
// ❌ 不好 - 每次渲染都检查权限
function BadExample() {
  return (
    <>
      {usePermission('user:create') && <Button>创建</Button>}
      {usePermission('user:edit') && <Button>编辑</Button>}
    </>
  );
}

// ✅ 好 - 在组件顶部统一检查
function GoodExample() {
  const canCreate = usePermission('user:create');
  const canEdit = usePermission('user:edit');
  
  return (
    <>
      {canCreate && <Button>创建</Button>}
      {canEdit && <Button>编辑</Button>}
    </>
  );
}
```

### 2. 缓存权限数据

系统已自动在 `PermissionService` 中实现了缓存，首次加载后会缓存用户的权限列表。

---

## 🔍 调试技巧

### 查看当前用户权限

```tsx
// 在浏览器控制台执行
fetch('/api/v1/permissions/my', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
})
.then(r => r.json())
.then(d => console.log('当前用户权限:', d.data));
```

### 检查特定权限

```tsx
// 在浏览器控制台执行
fetch('/api/v1/permissions/check?code=user:create', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
})
.then(r => r.json())
.then(d => console.log('是否有 user:create 权限:', d.data.hasPermission));
```

---

## 📋 权限代码规范

建议的权限代码命名规范：

```
{模块}:{操作}

示例：
- user:view         # 查看用户
- user:create       # 创建用户
- user:edit         # 编辑用户
- user:delete       # 删除用户
- user:reset-password  # 重置密码
- role:view         # 查看角色
- role:create       # 创建角色
- role:assign-permission  # 分配权限
- org:view          # 查看组织
- org:create        # 创建组织
```

---

## 🎓 最佳实践

1. **优先使用 PermissionGuard 组件** - 代码更清晰，易于维护
2. **在按钮级别应用权限控制** - 细粒度控制用户体验
3. **后端仍需验证权限** - 前端控制仅用于 UX，不能替代后端安全
4. **合理使用 fallback** - 给用户友好的无权限提示
5. **避免嵌套过深** - 保持组件结构扁平化

---

## ⚠️ 注意事项

1. **权限检查是异步的** - 首次加载时可能会有短暂闪烁
2. **权限变更后需刷新** - 修改角色权限后，用户需要重新登录或刷新页面
3. **不要依赖前端权限做安全控制** - 后端必须再次验证所有权限

---

**最后更新**: 2026-04-15
