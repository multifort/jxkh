-- 创建评分表
CREATE TABLE `scores` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分ID',
  `plan_id` BIGINT NOT NULL COMMENT '绩效计划ID',
  `indicator_instance_id` BIGINT NOT NULL COMMENT '指标实例ID',
  `evaluator_id` BIGINT NOT NULL COMMENT '评分人ID',
  `score` DECIMAL(5,2) NOT NULL COMMENT '分数（0-100）',
  `comment` TEXT COMMENT '评语',
  `type` VARCHAR(20) NOT NULL COMMENT '评分类型：SELF-自评，MANAGER-上级评',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：DRAFT-草稿，SUBMITTED-已提交',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_indicator_evaluator_type` (`plan_id`, `indicator_instance_id`, `evaluator_id`, `type`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_evaluator_id` (`evaluator_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分表';

-- 添加 performance_plans 表的评分相关字段
ALTER TABLE `performance_plans`
  ADD COLUMN `final_score` DECIMAL(5,2) DEFAULT NULL COMMENT '最终得分',
  ADD COLUMN `performance_level` VARCHAR(10) DEFAULT NULL COMMENT '绩效等级（A/B/C/D）',
  ADD COLUMN `calculated_at` DATETIME DEFAULT NULL COMMENT '分数计算时间';

-- 创建索引
ALTER TABLE `performance_plans`
  ADD KEY `idx_final_score` (`final_score`),
  ADD KEY `idx_performance_level` (`performance_level`);
