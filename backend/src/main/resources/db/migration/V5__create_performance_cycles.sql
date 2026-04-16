-- 创建绩效周期表
CREATE TABLE `performance_cycles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '周期ID',
  `name` VARCHAR(100) NOT NULL COMMENT '周期名称（如：2026年Q1）',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：QUARTERLY-季度，ANNUAL-年度，MONTHLY-月度',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `end_date` DATE NOT NULL COMMENT '结束日期',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，IN_PROGRESS-进行中，ENDED-已结束',
  `org_id` BIGINT DEFAULT NULL COMMENT '组织ID（NULL表示全公司）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效周期表';
