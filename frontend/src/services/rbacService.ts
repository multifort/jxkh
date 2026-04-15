import request from './api';
import type { Role, Permission, Org, OrgTreeNode } from '../types/auth';

/**
 * 角色管理服务
 */
export const roleService = {
  /**
   * 获取所有活跃角色
   */
  getAllActiveRoles: () => 
    request.get<Role[]>('/roles'),

  /**
   * 获取角色列表（分页）
   */
  getRoleList: (params?: { page?: number; size?: number }) =>
    request.get<{ content: Role[]; totalElements: number }>('/roles', { params }),

  /**
   * 获取角色详情
   */
  getRoleById: (id: number) =>
    request.get<Role>(`/roles/${id}`),

  /**
   * 创建角色
   */
  createRole: (data: Omit<Role, 'id' | 'createdAt' | 'updatedAt'>) =>
    request.post<Role>('/roles', data),

  /**
   * 更新角色
   */
  updateRole: (id: number, data: Partial<Role>) =>
    request.put<Role>(`/roles/${id}`, data),

  /**
   * 删除角色
   */
  deleteRole: (id: number) =>
    request.delete<void>(`/roles/${id}`),
};

/**
 * 权限管理服务
 */
export const permissionService = {
  /**
   * 获取所有活跃权限
   */
  getAllActivePermissions: () =>
    request.get<Permission[]>('/permissions'),

  /**
   * 获取权限树
   */
  getPermissionTree: () =>
    request.get<Permission[]>('/permissions/tree'),

  /**
   * 检查用户权限
   */
  checkPermission: (code: string) =>
    request.get<{ code: string; hasPermission: boolean }>(`/permissions/check/${code}`),
};

/**
 * 组织管理服务
 */
export const orgService = {
  /**
   * 获取组织树
   */
  getOrgTree: () =>
    request.get<OrgTreeNode[]>('/orgs/tree'),

  /**
   * 获取组织列表
   */
  getOrgList: (params?: { page?: number; size?: number }) =>
    request.get<{ content: Org[]; totalElements: number }>('/orgs', { params }),

  /**
   * 获取组织详情
   */
  getOrgById: (id: number) =>
    request.get<Org>(`/orgs/${id}`),

  /**
   * 创建组织
   */
  createOrg: (data: Omit<Org, 'id' | 'createdAt' | 'updatedAt'>) =>
    request.post<Org>('/orgs', data),

  /**
   * 更新组织
   */
  updateOrg: (id: number, data: Partial<Org>) =>
    request.put<Org>(`/orgs/${id}`, data),

  /**
   * 删除组织
   */
  deleteOrg: (id: number) =>
    request.delete<void>(`/orgs/${id}`),
};
