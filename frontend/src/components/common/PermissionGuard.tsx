import React from 'react';
import { usePermission, useAnyPermission, useAllPermissions } from '@/hooks/usePermission';

interface PermissionGuardProps {
  /** 权限代码 */
  permission?: string;
  /** 任一权限代码数组 */
  anyOf?: string[];
  /** 所有权限代码数组 */
  allOf?: string[];
  /** 无权限时显示的内容 */
  fallback?: React.ReactNode;
  /** 子组件 */
  children: React.ReactNode;
}

/**
 * 权限守卫组件
 * 
 * 使用示例：
 * 1. 单个权限：<PermissionGuard permission="user:create"><Button>创建</Button></PermissionGuard>
 * 2. 任一权限：<PermissionGuard anyOf={["user:create", "user:edit"]}><Button>操作</Button></PermissionGuard>
 * 3. 所有权限：<PermissionGuard allOf={["user:view", "user:export"]}><Button>导出</Button></PermissionGuard>
 */
const PermissionGuard: React.FC<PermissionGuardProps> = ({
  permission,
  anyOf,
  allOf,
  fallback = null,
  children,
}) => {
  // 单个权限检查
  const hasSinglePermission = permission ? usePermission(permission) : true;
  
  // 任一权限检查
  const hasAnyPermission = anyOf ? useAnyPermission(anyOf) : true;
  
  // 所有权限检查
  const hasAllPermissions = allOf ? useAllPermissions(allOf) : true;

  // 所有条件都满足才显示
  const hasPermission = hasSinglePermission && hasAnyPermission && hasAllPermissions;

  if (!hasPermission) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};

export default PermissionGuard;
