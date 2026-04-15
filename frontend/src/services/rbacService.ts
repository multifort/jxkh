import request from './api';
import type { Role, Permission, Org, OrgTreeNode } from '../types/auth';

// API 响应包装器类型
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

/**
 * 角色管理服务
 */
export const roleService = {
  /**
   * 获取所有活跃角色
   */
  getAllActiveRoles: async (): Promise<Role[]> => {
    const response = await request.get<ApiResponse<Role[]>>('/roles');
    return response.data.data || [];
  },

  /**
   * 获取角色详情
   */
  getRoleById: async (id: number): Promise<Role> => {
    const response = await request.get<ApiResponse<Role>>(`/roles/${id}`);
    return response.data.data;
  },

  /**
   * 创建角色
   */
  createRole: async (data: Partial<Role>): Promise<Role> => {
    const response = await request.post<ApiResponse<Role>>('/roles', data);
    return response.data.data;
  },

  /**
   * 更新角色
   */
  updateRole: async (id: number, data: Partial<Role>): Promise<Role> => {
    const response = await request.put<ApiResponse<Role>>(`/roles/${id}`, data);
    return response.data.data;
  },

  /**
   * 删除角色
   */
  deleteRole: async (id: number): Promise<void> => {
    await request.delete<ApiResponse<void>>(`/roles/${id}`);
  },
};

/**
 * 权限管理服务
 */
export const permissionService = {
  /**
   * 获取所有活跃权限
   */
  getAllActivePermissions: async (): Promise<Permission[]> => {
    const response = await request.get<ApiResponse<Permission[]>>('/permissions');
    return response.data.data || [];
  },

  /**
   * 检查用户权限
   */
  checkPermission: async (code: string): Promise<boolean> => {
    const response = await request.get<ApiResponse<{ code: string; hasPermission: boolean }>>(`/permissions/check`, { params: { code } });
    return response.data.data?.hasPermission || false;
  },
};

/**
 * 组织管理服务
 */
export const orgService = {
  /**
   * 获取组织树
   */
  getOrgTree: async (): Promise<OrgTreeNode[]> => {
    const response = await request.get<ApiResponse<OrgTreeNode[]>>('/orgs/tree');
    return response.data.data || [];
  },

  /**
   * 获取组织详情
   */
  getOrgById: async (id: number): Promise<Org> => {
    const response = await request.get<ApiResponse<Org>>(`/orgs/${id}`);
    return response.data.data;
  },

  /**
   * 创建组织
   */
  createOrg: async (data: Partial<Org>): Promise<Org> => {
    const response = await request.post<ApiResponse<Org>>('/orgs', data);
    return response.data.data;
  },

  /**
   * 更新组织
   */
  updateOrg: async (id: number, data: Partial<Org>): Promise<Org> => {
    const response = await request.put<ApiResponse<Org>>(`/orgs/${id}`, data);
    return response.data.data;
  },

  /**
   * 删除组织
   */
  deleteOrg: async (id: number): Promise<void> => {
    await request.delete<ApiResponse<void>>(`/orgs/${id}`);
  },
};
