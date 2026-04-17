import request from './api';
import type { WeightScheme, WeightSchemeItem } from '../types/performance';

// API 响应类型 - 后端统一返回 ApiResponse<T> { code, message, data }
type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

/**
 * 权重方案API
 */
export const weightSchemeApi = {
  /**
   * 分页查询方案列表
   */
  list(keyword?: string, cycleId?: number, status?: string, page = 0, size = 20) {
    return request.get<ApiResponse<{ content: WeightScheme[]; totalElements: number }>>('/weight-schemes', {
      params: { keyword, cycleId, status, page, size }
    });
  },

  /**
   * 根据ID获取方案
   */
  getById(id: number) {
    return request.get<ApiResponse<WeightScheme>>(`/weight-schemes/${id}`);
  },

  /**
   * 获取方案明细列表
   */
  getItems(id: number) {
    return request.get<ApiResponse<WeightSchemeItem[]>>(`/weight-schemes/${id}/items`);
  },

  /**
   * 创建方案
   */
  create(data: Partial<WeightScheme>) {
    return request.post<ApiResponse<WeightScheme>>('/weight-schemes', data);
  },

  /**
   * 更新方案基本信息
   */
  update(id: number, data: Partial<WeightScheme>) {
    return request.put<ApiResponse<WeightScheme>>(`/weight-schemes/${id}`, data);
  },

  /**
   * 保存方案明细
   */
  saveItems(id: number, items: WeightSchemeItem[]) {
    return request.put<ApiResponse<void>>(`/weight-schemes/${id}/items`, items);
  },

  /**
   * 发布方案
   */
  publish(id: number) {
    return request.post<ApiResponse<WeightScheme>>(`/weight-schemes/${id}/publish`);
  },

  /**
   * 归档方案
   */
  archive(id: number) {
    return request.post<ApiResponse<WeightScheme>>(`/weight-schemes/${id}/archive`);
  },

  /**
   * 删除方案
   */
  delete(id: number) {
    return request.delete<ApiResponse<void>>(`/weight-schemes/${id}`);
  },

  /**
   * 复制方案
   */
  copy(id: number) {
    return request.post<ApiResponse<WeightScheme>>(`/weight-schemes/${id}/copy`);
  }
};
