-- V9__create_performance_plans.sql
-- 创建绩效计划表
-- 版本: 9.0
-- 日期: 2026-04-17
-- Sprint: 3a

CREATE TABLE `performance_plans` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `user_id` BIGINT NOT NULL COMMENT '员工ID',
  `cycle_id` BIGINT NOT NULL COMMENT '周期ID',
  `org_id` BIGINT NOT NULL COMMENT '组织ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING_SUBMIT-待提交，PENDING_APPROVE-待审批，IN_PROGRESS-执行中，PENDING_EVAL-待评估，EVALUATED-已评估，CALIBRATED-已校准，ARCHIVED-已归档',
  `total_score` DECIMAL(5,2) DEFAULT NULL COMMENT '总分',
  `final_level` VARCHAR(10) DEFAULT NULL COMMENT '最终等级：A/B/C/D',
  `evaluator_id` BIGINT DEFAULT NULL COMMENT '评估人ID',
  `comment` TEXT COMMENT '评语',
  `submitted_at` DATETIME DEFAULT NULL COMMENT '提交时间',
  `approved_at` DATETIME DEFAULT NULL COMMENT '审批时间',
  `evaluated_at` DATETIME DEFAULT NULL COMMENT '评估时间',
  `calibrated_at` DATETIME DEFAULT NULL COMMENT '校准时间',
  `archived_at` DATETIME DEFAULT NULL COMMENT '归档时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_cycle` (`user_id`, `cycle_id`),
  KEY `idx_cycle_id` (`cycle_id`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_status` (`status`),
  KEY `idx_evaluator_id` (`evaluator_id`),
  KEY `idx_final_level` (`final_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效计划表';
