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

/**
 * 绩效计划状态枚举
 */
export enum PlanStatus {
  DRAFT = 'DRAFT',
  PENDING_SUBMIT = 'PENDING_SUBMIT',
  PENDING_APPROVE = 'PENDING_APPROVE',
  IN_PROGRESS = 'IN_PROGRESS',
  PENDING_EVAL = 'PENDING_EVAL',
  EVALUATED = 'EVALUATED',
  CALIBRATED = 'CALIBRATED',
  ARCHIVED = 'ARCHIVED'
}

/**
 * 绩效等级枚举
 */
export enum PerformanceLevel {
  A = 'A',
  B = 'B',
  C = 'C',
  D = 'D'
}

/**
 * 指标实例状态枚举
 */
export enum InstanceStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  DELAYED = 'DELAYED'
}

/**
 * 指标实例
 */
export interface IndicatorInstance {
  id?: number;
  indicatorId: number;
  planId?: number;
  ownerId: number;
  name: string;
  type: string;
  weight: number;
  targetValue?: number | null;
  currentValue?: number;
  progress?: number;
  status?: InstanceStatus;
  unit?: string | null;
  remark?: string | null;
  score?: number | null;
  createdBy?: number;
  createdAt?: string;
  updatedBy?: number;
  updatedAt?: string;
}

/**
 * 绩效计划
 */
export interface PerformancePlan {
  id: number;
  userId: number;
  cycleId: number;
  orgId: number;
  status: PlanStatus;
  totalScore?: number | null;
  finalLevel?: PerformanceLevel | null;
  evaluatorId?: number | null;
  comment?: string | null;
  submittedAt?: string | null;
  approvedAt?: string | null;
  evaluatedAt?: string | null;
  calibratedAt?: string | null;
  archivedAt?: string | null;
  createdBy?: number;
  createdAt: string;
  updatedBy?: number;
  updatedAt: string;
  indicators?: IndicatorInstance[];
}

/**
 * 创建计划请求
 */
export interface PlanCreateRequest {
  userId: number;
  cycleId: number;
  indicators: IndicatorItemRequest[];
}

/**
 * 更新计划请求
 */
export interface PlanUpdateRequest {
  indicators: IndicatorItemRequest[];
}

/**
 * 指标项请求
 */
export interface IndicatorItemRequest {
  indicatorId: number;
  ownerId: number;
  name: string;
  type: string;
  weight: number;
  targetValue?: number | null;
  unit?: string | null;
  remark?: string | null;
}
