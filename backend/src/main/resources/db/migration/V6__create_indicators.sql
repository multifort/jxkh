-- 创建指标分类表
CREATE TABLE `indicator_categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `code` VARCHAR(50) NOT NULL COMMENT '分类编码',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID（NULL表示根分类）',
  `level` INT NOT NULL DEFAULT 1 COMMENT '层级（1-根，2-二级，3-三级）',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `org_id` BIGINT DEFAULT NULL COMMENT '组织ID（NULL表示全公司）',
  `created_by` BIGINT NOT NULL COMMENT '创建人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_org` (`code`, `org_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标分类表';

-- 创建指标表
CREATE TABLE `indicators` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `name` VARCHAR(100) NOT NULL COMMENT '指标名称',
  `code` VARCHAR(50) NOT NULL COMMENT '指标编码',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：QUANTITATIVE-定量，QUALITATIVE-定性',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位（如：分、%、元）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '指标说明',
  `calculation_method` TEXT DEFAULT NULL COMMENT '计算方法',
  `data_source` VARCHAR(200) DEFAULT NULL COMMENT '数据来源',
  `target_type` VARCHAR(20) DEFAULT NULL COMMENT '目标类型：MINIMUM-最低值，MAXIMUM-最高值，RANGE-范围值，EXACT-精确值',
  `default_weight` DECIMAL(5,2) DEFAULT NULL COMMENT '默认权重（0-100）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-禁用',
  `org_id` BIGINT DEFAULT NULL COMMENT '组织ID（NULL表示全公司）',
  `created_by` BIGINT NOT NULL COMMENT '创建人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_org` (`code`, `org_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标表';
