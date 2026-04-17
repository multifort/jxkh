-- V10__create_indicator_instances.sql
-- 创建指标实例表
-- 版本: 10.0
-- 日期: 2026-04-17
-- Sprint: 3a

CREATE TABLE `indicator_instances` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '实例ID',
  `indicator_id` BIGINT NOT NULL COMMENT '指标模板ID',
  `plan_id` BIGINT NOT NULL COMMENT '绩效计划ID',
  `owner_id` BIGINT NOT NULL COMMENT '责任人ID',
  `name` VARCHAR(100) NOT NULL COMMENT '指标名称（冗余）',
  `type` VARCHAR(20) NOT NULL COMMENT '指标类型（冗余）',
  `weight` DECIMAL(5,2) NOT NULL COMMENT '权重（0-100）',
  `target_value` DECIMAL(15,2) DEFAULT NULL COMMENT '目标值',
  `current_value` DECIMAL(15,2) DEFAULT 0 COMMENT '当前值',
  `progress` DECIMAL(5,2) DEFAULT 0 COMMENT '进度百分比（0-100）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态：NOT_STARTED-未开始，IN_PROGRESS-进行中，COMPLETED-已完成，DELAYED-延期',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `score` DECIMAL(5,2) DEFAULT NULL COMMENT '得分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_indicator_id` (`indicator_id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标实例表';
