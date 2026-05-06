import api from './api';

/**
 * 通知类型
 */
export interface Notification {
  id: number;
  userId: number;
  type: 'SYSTEM' | 'TASK' | 'APPROVAL' | 'RISK_WARNING';
  title: string;
  content: string;
  relatedType?: string;
  relatedId?: number;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
  updatedAt: string;
  isDeleted: boolean;
}

/**
 * 通知 API 服务
 */
export const notificationService = {
  /**
   * 查询通知列表（分页）
   */
  getNotifications: (page: number = 0, size: number = 20) => {
    return api.get<any>('/notifications', {
      params: { page, size },
    });
  },

  /**
   * 查询未读通知数量
   */
  getUnreadCount: () => {
    return api.get<number>('/notifications/unread-count');
  },

  /**
   * 标记通知为已读
   */
  markAsRead: (id: number) => {
    return api.put(`/notifications/${id}/read`);
  },

  /**
   * 全部标记为已读
   */
  markAllAsRead: () => {
    return api.put('/notifications/mark-all-read');
  },
};

