import api from './api';

/**
 * 绩效记录 API 服务
 */
export const recordService = {
  /**
   * 创建记录
   */
  createRecord: (data: FormData) => {
    return api.post<number>('/records', data, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * 查询记录列表
   */
  getRecords: (params: {
    planId: number;
    type?: string;
    page?: number;
    size?: number;
  }) => {
    return api.get<any>('/records', { params });
  },

  /**
   * 查询记录详情
   */
  getRecordById: (id: number) => {
    return api.get<any>(`/records/${id}`);
  },

  /**
   * 更新记录
   */
  updateRecord: (id: number, data: FormData) => {
    return api.put(`/records/${id}`, data, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * 删除记录
   */
  deleteRecord: (id: number) => {
    return api.delete(`/records/${id}`);
  },

  /**
   * 删除附件
   */
  deleteAttachment: (recordId: number, fileUrl: string) => {
    return api.delete(`/records/${recordId}/attachments`, {
      params: { fileUrl },
    });
  },
};

/**
 * 文件管理 API 服务
 */
export const fileService = {
  /**
   * 上传单个文件
   */
  uploadFile: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<string>('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * 批量上传文件
   */
  uploadFiles: (files: File[]) => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });
    return api.post<string[]>('/files/upload/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  /**
   * 删除文件
   */
  deleteFile: (fileUrl: string) => {
    return api.delete('/files', { params: { fileUrl } });
  },

  /**
   * 获取下载链接
   */
  getDownloadUrl: (fileName: string) => {
    return api.get<string>('/files/download-url', {
      params: { fileName },
    });
  },
};

/**
 * AI 服务 API
 */
export const aiService = {
  /**
   * 生成周报智能总结
   */
  generateWeeklySummary: (content: string) => {
    return api.post<any>('/ai/weekly-summary', { content });
  },
};

/**
 * 计划风险 API
 */
export const riskService = {
  /**
   * 查询计划风险
   */
  getPlanRisks: (planId: number) => {
    return api.get<any>(`/plans/${planId}/risks`);
  },
  
  /**
   * 查询风险预警指标
   */
  getRiskIndicators: (planId: number) => {
    return api.get<any>('/records/risks', { params: { planId } });
  },
};
