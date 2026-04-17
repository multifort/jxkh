/**
 * 绩效周期类型枚举
 */
export enum CycleType {
  MONTHLY = 'MONTHLY',
  QUARTERLY = 'QUARTERLY',
  ANNUAL = 'ANNUAL'
}

/**
 * 绩效周期状态枚举
 */
export enum CycleStatus {
  DRAFT = 'DRAFT',
  IN_PROGRESS = 'IN_PROGRESS',
  ENDED = 'ENDED'
}

/**
 * 绩效周期接口
 */
export interface PerformanceCycle {
  id: number;
  name: string;
  type: CycleType;
  startDate: string; // LocalDate 序列化为字符串
  endDate: string;
  status: CycleStatus;
  orgId?: number;
  remark?: string;
  createdBy?: number;
  updatedBy?: number;
  createdAt: string;
  updatedAt: string;
  isDeleted?: boolean;
}

/**
 * 周期查询参数
 */
export interface CycleQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  status?: CycleStatus;
  orgId?: number;
}

/**
 * 指标分类
 */
export interface IndicatorCategory {
  id: number;
  name: string;
  code: string;
  parentId?: number | null;
  level: number;
  sortOrder: number;
  description?: string | null;
  orgId?: number | null;
  createdBy: number;
  createdAt: string;
  updatedBy?: number | null;
  updatedAt: string;
}

/**
 * 指标类型枚举
 */
export enum IndicatorType {
  QUANTITATIVE = 'QUANTITATIVE',
  QUALITATIVE = 'QUALITATIVE'
}

/**
 * 目标类型枚举
 */
export enum TargetType {
  MINIMUM = 'MINIMUM',
  MAXIMUM = 'MAXIMUM',
  RANGE = 'RANGE',
  EXACT = 'EXACT'
}

/**
 * 指标状态枚举
 */
export enum IndicatorStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE'
}

/**
 * 指标
 */
export interface Indicator {
  id: number;
  name: string;
  code: string;
  categoryId: number;
  type: IndicatorType;
  unit?: string | null;
  description?: string | null;
  calculationMethod?: string | null;
  dataSource?: string | null;
  targetType?: TargetType | null;
  defaultWeight?: number | null;
  status: IndicatorStatus;
  orgId?: number | null;
  createdBy: number;
  createdAt: string;
  updatedBy?: number | null;
  updatedAt: string;
}

/**
 * 权重方案状态枚举
 */
export enum WeightSchemeStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED'
}

/**
 * 权重方案
 */
export interface WeightScheme {
  id: number;
  name: string;
  code: string;
  cycleId?: number | null;
  orgId?: number | null;
  version: number;
  status: WeightSchemeStatus;
  description?: string | null;
  totalWeight: number;
  publishedAt?: string | null;
  publishedBy?: number | null;
  createdBy: number;
  createdAt: string;
  updatedBy?: number | null;
  updatedAt: string;
}

/**
 * 权重方案明细
 */
export interface WeightSchemeItem {
  id?: number;
  schemeId: number;
  indicatorId: number;
  weight: number;
  sortOrder: number;
  createdAt?: string;
  updatedAt?: string;
}
