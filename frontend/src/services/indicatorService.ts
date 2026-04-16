import request from './api';
import type { IndicatorCategory, Indicator } from '../types/performance';

/**
 * 指标分类API
 */
export const indicatorCategoryApi = {
  /**
   * 分页查询分类列表
   */
  list(keyword?: string, parentId?: number, page = 0, size = 20) {
    return request.get<{ content: IndicatorCategory[]; totalElements: number }>('/indicator-categories', {
      params: { keyword, parentId, page, size }
    });
  },

  /**
   * 获取分类树
   */
  getTree() {
    return request.get<IndicatorCategory[]>('/indicator-categories/tree');
  },

  /**
   * 根据ID获取分类
   */
  getById(id: number) {
    return request.get<IndicatorCategory>(`/indicator-categories/${id}`);
  },

  /**
   * 创建分类
   */
  create(data: Partial<IndicatorCategory>) {
    return request.post<IndicatorCategory>('/indicator-categories', data);
  },

  /**
   * 更新分类
   */
  update(id: number, data: Partial<IndicatorCategory>) {
    return request.put<IndicatorCategory>(`/indicator-categories/${id}`, data);
  },

  /**
   * 删除分类
   */
  delete(id: number) {
    return request.delete(`/indicator-categories/${id}`);
  }
};

/**
 * 指标API
 */
export const indicatorApi = {
  /**
   * 分页查询指标列表
   */
  list(keyword?: string, categoryId?: number, type?: string, status?: string, page = 0, size = 20) {
    return request.get<{ content: Indicator[]; totalElements: number }>('/indicators', {
      params: { keyword, categoryId, type, status, page, size }
    });
  },

  /**
   * 根据ID获取指标
   */
  getById(id: number) {
    return request.get<Indicator>(`/indicators/${id}`);
  },

  /**
   * 创建指标
   */
  create(data: Partial<Indicator>) {
    return request.post<Indicator>('/indicators', data);
  },

  /**
   * 更新指标
   */
  update(id: number, data: Partial<Indicator>) {
    return request.put<Indicator>(`/indicators/${id}`, data);
  },

  /**
   * 删除指标
   */
  delete(id: number) {
    return request.delete(`/indicators/${id}`);
  },

  /**
   * 启用/禁用指标
   */
  toggleStatus(id: number) {
    return request.post<Indicator>(`/indicators/${id}/toggle-status`);
  }
};
