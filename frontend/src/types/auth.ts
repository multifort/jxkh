/**
 * 角色类型
 */
export interface Role {
  id: number;
  code: string;
  name: string;
  description?: string;
  sort: number;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * 权限类型
 */
export interface Permission {
  id: number;
  code: string;
  name: string;
  type: 'MENU' | 'BUTTON' | 'DATA';
  resource?: string;
  parentId?: number;
  sort: number;
  icon?: string;
  path?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 组织类型
 */
export interface Org {
  id: number;
  name: string;
  code: string;
  parentId?: number;
  level: number;
  leaderId?: number;
  orgType?: string;
  description?: string;
  sort: number;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * 组织树节点
 */
export interface OrgTreeNode extends Org {
  children?: OrgTreeNode[];
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  email?: string;
  phone?: string;
  avatar?: string;
  orgId?: number;
  roles: Role[];
  permissions: string[];
}

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string;
  userInfo: UserInfo;
}
