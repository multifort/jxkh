import api from './api';
import { Score, ScoreSubmitRequest, ScoreProgress } from '../types/score';

/**
 * 评分服务
 */
export const scoreService = {
  /**
   * 提交评分
   */
  submitScore: (data: ScoreSubmitRequest) => {
    return api.post<number>('/scores', data);
  },

  /**
   * 查询待评分计划列表
   */
  getPendingPlans: (type: 'SELF' | 'MANAGER') => {
    return api.get<number[]>('/scores/pending', { params: { type } });
  },

  /**
   * 查询计划的评分详情
   */
  getScoresByPlan: (planId: number) => {
    return api.get<Score[]>(`/scores/plan/${planId}`);
  },

  /**
   * 查询评分进度
   */
  getScoreProgress: (planId: number) => {
    return api.get<ScoreProgress>(`/scores/progress/${planId}`);
  },

  /**
   * 计算分数（管理员）
   */
  calculateScore: (planId: number) => {
    return api.post<void>('/scores/calculate', null, { params: { planId } });
  },
};
