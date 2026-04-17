import api from './api';
import type {
  PerformancePlan,
  PlanCreateRequest,
  PlanUpdateRequest,
} from '../types/performance';

/**
 * 绩效计划 API 服务
 */
export const planService = {
  /**
   * 创建绩效计划
   */
  createPlan: (data: PlanCreateRequest) => {
    return api.post<number>('/plans', data);
  },

  /**
   * 根据ID查询计划详情
   */
  getPlanById: (id: number) => {
    return api.get<PerformancePlan>(`/plans/${id}`);
  },

  /**
   * 分页查询计划列表
   */
  listPlans: (params: {
    page?: number;
    size?: number;
    cycleId?: number;
    status?: string;
  }) => {
    return api.get('/plans', { params });
  },

  /**
   * 更新计划草稿
   */
  updatePlanDraft: (id: number, data: PlanUpdateRequest) => {
    return api.put(`/plans/${id}/draft`, data);
  },
};
