-- 创建权重方案表
CREATE TABLE `weight_schemes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '方案ID',
  `name` VARCHAR(100) NOT NULL COMMENT '方案名称',
  `code` VARCHAR(50) NOT NULL COMMENT '方案编码',
  `cycle_id` BIGINT DEFAULT NULL COMMENT '适用周期ID',
  `org_id` BIGINT DEFAULT NULL COMMENT '适用组织ID（NULL表示全公司）',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '方案说明',
  `total_weight` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '权重总和',
  `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
  `published_by` BIGINT DEFAULT NULL COMMENT '发布人ID',
  `created_by` BIGINT NOT NULL COMMENT '创建人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_version_org` (`code`, `version`, `org_id`),
  KEY `idx_cycle_id` (`cycle_id`),
  KEY `idx_status` (`status`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权重方案表';

-- 创建权重方案明细表
CREATE TABLE `weight_scheme_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `scheme_id` BIGINT NOT NULL COMMENT '方案ID',
  `indicator_id` BIGINT NOT NULL COMMENT '指标ID',
  `weight` DECIMAL(5,2) NOT NULL COMMENT '权重值（0-100）',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scheme_indicator` (`scheme_id`, `indicator_id`),
  KEY `idx_scheme_id` (`scheme_id`),
  KEY `idx_indicator_id` (`indicator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权重方案明细表';
