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
  refreshToken: string;
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
   * 刷新 Token
   */
  refreshToken: async (refreshToken: string): Promise<LoginResponse> => {
    const response = await apiClient.post('/auth/refresh', { refreshToken });
    return response.data.data;
  },

  /**
   * 退出登录
   */
  logout: async (userId: number, refreshToken: string): Promise<void> => {
    await apiClient.post('/auth/logout', null, {
      headers: { 'X-User-Id': userId },
      params: { refreshToken },
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
