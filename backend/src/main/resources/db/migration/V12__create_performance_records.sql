-- Sprint 4: 绩效记录表
CREATE TABLE IF NOT EXISTS `performance_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `plan_id` BIGINT NOT NULL COMMENT '绩效计划ID',
  `user_id` BIGINT NOT NULL COMMENT '员工ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：WEEKLY_REPORT-周报，MONTHLY_REPORT-月报，MILESTONE-里程碑，ACHIEVEMENT-成果',
  `content` TEXT NOT NULL COMMENT '内容',
  `progress` DECIMAL(5,2) DEFAULT NULL COMMENT '进度',
  `attachments` JSON DEFAULT NULL COMMENT '附件URL列表',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `ai_summary` TEXT COMMENT 'AI总结',
  `ai_suggestions` JSON DEFAULT NULL COMMENT 'AI建议',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_record_date` (`record_date`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效记录表';
