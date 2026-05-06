/**
 * 评分类型
 */
export type ScoreType = 'SELF' | 'MANAGER';

/**
 * 评分状态
 */
export type ScoreStatus = 'DRAFT' | 'SUBMITTED';

/**
 * 评分记录
 */
export interface Score {
  id: number;
  planId: number;
  indicatorInstanceId: number;
  evaluatorId: number;
  score: number;
  comment?: string;
  type: ScoreType;
  status: ScoreStatus;
  createdAt: string;
  updatedAt: string;
}

/**
 * 评分提交请求
 */
export interface ScoreSubmitRequest {
  planId: number;
  indicatorInstanceId: number;
  score: number;
  comment?: string;
  type: ScoreType;
}

/**
 * 评分进度
 */
export interface ScoreProgress {
  planId: number;
  totalIndicators: number;
  selfScoredCount: number;
  managerScoredCount: number;
  selfCompleted: boolean;
  managerCompleted: boolean;
  allCompleted: boolean;
}
