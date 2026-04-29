import api from './api';
import type {
  PlanDetailDTO,
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
    return api.get<PlanDetailDTO>(`/plans/${id}`);
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
    return api.get<any>('/plans', { params });
  },

  /**
   * 更新计划草稿
   */
  updatePlanDraft: (id: number, data: PlanUpdateRequest) => {
    return api.put(`/plans/${id}/draft`, data);
  },

  /**
   * 提交计划审批
   */
  submitPlan: (id: number) => {
    return api.post(`/plans/${id}/submit`);
  },

  /**
   * 审批计划
   */
  approvePlan: (id: number, approved: boolean, comment?: string) => {
    return api.post(`/plans/${id}/approve`, null, {
      params: { approved, comment },
    });
  },
};
