import request from './api';
import type { Role, Permission, Org, OrgTreeNode, User } from '../types/auth';

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
 * 用户管理服务
 */
export const userService = {
  /**
   * 分页查询用户列表
   */
  getUsers: async (params: {
    page?: number;
    size?: number;
    keyword?: string;
    orgId?: number;
    role?: string;
  }): Promise<any> => {
    const response = await request.get<any>('/users', { params });
    console.log('用户列表原始响应:', response);
    
    // 如果返回的是数组（没有包装在 ApiResponse 中）
    if (Array.isArray(response.data)) {
      return {
        content: response.data,
        totalElements: response.data.length,
        totalPages: 1,
        number: params.page || 0,
        size: params.size || 10,
      };
    }
    
    // 如果已经是分页格式
    if (response.data && response.data.content) {
      return response.data;
    }
    
    // 其他情况
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: params.page || 0,
      size: params.size || 10,
    };
  },

  /**
   * 获取用户详情
   */
  getUserById: async (id: number): Promise<User> => {
    const response = await request.get<ApiResponse<User>>(`/users/${id}`);
    return response.data.data;
  },

  /**
   * 创建用户
   */
  createUser: async (data: Partial<User>): Promise<User> => {
    const response = await request.post<ApiResponse<User>>('/users', data);
    return response.data.data;
  },

  /**
   * 更新用户
   */
  updateUser: async (id: number, data: Partial<User>): Promise<User> => {
    const response = await request.put<ApiResponse<User>>(`/users/${id}`, data);
    return response.data.data;
  },

  /**
   * 删除用户
   */
  deleteUser: async (id: number): Promise<void> => {
    await request.delete<ApiResponse<void>>(`/users/${id}`);
  },

  /**
   * 启用/禁用用户
   */
  toggleUserStatus: async (id: number): Promise<User> => {
    const response = await request.patch<ApiResponse<User>>(`/users/${id}/toggle-status`);
    return response.data.data;
  },

  /**
   * 重置密码
   */
  resetPassword: async (id: number, newPassword: string): Promise<void> => {
    await request.post<ApiResponse<void>>(`/users/${id}/reset-password`, null, {
      params: { newPassword },
    });
  },

  /**
   * 解锁用户
   */
  unlockUser: async (id: number): Promise<User> => {
    const response = await request.post<ApiResponse<User>>(`/users/${id}/unlock`);
    return response.data.data;
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
   * 获取所有活跃组织
   */
  getAllActiveOrgs: async (): Promise<Org[]> => {
    const response = await request.get<ApiResponse<Org[]>>('/orgs');
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

  /**
   * 获取组织下的用户列表
   */
  getOrgUsers: async (orgId: number): Promise<User[]> => {
    const response = await request.get<ApiResponse<User[]>>(`/orgs/${orgId}/users`);
    return response.data.data || [];
  },
};
