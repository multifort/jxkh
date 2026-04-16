import apiClient from './api';
import { PerformanceCycle, CycleStatus } from '../types/performance';

/**
 * 绩效周期 API 服务
 */
export const cycleService = {
  /**
   * 分页查询周期列表
   */
  getCycles: async (
    page: number = 0,
    size: number = 10,
    keyword?: string,
    status?: CycleStatus,
    orgId?: number
  ) => {
    const params: any = { page, size };
    if (keyword) params.keyword = keyword;
    if (status) params.status = status;
    if (orgId) params.orgId = orgId;

    const response = await apiClient.get('/cycles', { params });
    return response.data;
  },

  /**
   * 根据ID查询周期详情
   */
  getCycleById: async (id: number) => {
    const response = await apiClient.get(`/cycles/${id}`);
    return response.data;
  },

  /**
   * 创建周期
   */
  createCycle: async (cycle: Omit<PerformanceCycle, 'id' | 'createdAt' | 'updatedAt'>) => {
    const response = await apiClient.post('/cycles', cycle);
    return response.data;
  },

  /**
   * 更新周期
   */
  updateCycle: async (id: number, cycle: Partial<PerformanceCycle>) => {
    const response = await apiClient.put(`/cycles/${id}`, cycle);
    return response.data;
  },

  /**
   * 删除周期
   */
  deleteCycle: async (id: number) => {
    const response = await apiClient.delete(`/cycles/${id}`);
    return response.data;
  },

  /**
   * 启动周期
   */
  startCycle: async (id: number) => {
    const response = await apiClient.post(`/cycles/${id}/start`);
    return response.data;
  },

  /**
   * 结束周期
   */
  endCycle: async (id: number) => {
    const response = await apiClient.post(`/cycles/${id}/end`);
    return response.data;
  },
};
