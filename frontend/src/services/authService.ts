import apiClient from './api';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface UserInfo {
  id: number;
  username: string;
  name: string;
  email: string;
  role: string;
  orgId: number;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;  // 可选，现在通过 Cookie 传输
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

export const authService = {
  /**
   * 用户登录
   */
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post('/auth/login', data);
    return response.data.data;
  },

  /**
   * 刷新 Token（浏览器自动携带 Cookie）
   */
  refreshToken: async (): Promise<LoginResponse> => {
    const response = await apiClient.post('/auth/refresh');
    return response.data.data;
  },

  /**
   * 退出登录（浏览器自动清除 Cookie）
   */
  logout: async (userId: number): Promise<void> => {
    await apiClient.post('/auth/logout', null, {
      headers: { 'X-User-Id': userId },
    });
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser: async (userId: number): Promise<UserInfo> => {
    const response = await apiClient.get('/auth/me', {
      headers: { 'X-User-Id': userId },
    });
    return response.data.data;
  },
};
