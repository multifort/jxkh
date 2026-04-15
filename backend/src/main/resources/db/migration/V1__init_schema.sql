-- V1__init_schema.sql
-- 初始化数据库表结构
-- 版本: 1.0
-- 日期: 2026-04-15

-- ============================================
-- 组织管理模块
-- ============================================

CREATE TABLE IF NOT EXISTS `orgs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `name` VARCHAR(100) NOT NULL COMMENT '组织名称',
  `code` VARCHAR(50) NOT NULL COMMENT '组织编码',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父组织ID',
  `level` INT NOT NULL DEFAULT 1 COMMENT '组织层级',
  `leader_id` BIGINT DEFAULT NULL COMMENT '负责人ID',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_leader_id` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织表';

-- ============================================
-- 用户管理模块
-- ============================================

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `employee_no` VARCHAR(50) NOT NULL COMMENT '工号',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `org_id` BIGINT NOT NULL COMMENT '所属组织ID',
  `position_id` BIGINT DEFAULT NULL COMMENT '岗位ID',
  `manager_id` BIGINT DEFAULT NULL COMMENT '直属上级ID',
  `role` VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE' COMMENT '角色：EMPLOYEE-员工，MANAGER-主管，HR-人力资源，ADMIN-管理员',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-正常，INACTIVE-禁用，LOCKED-锁定',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
  `locked_at` DATETIME DEFAULT NULL COMMENT '锁定时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_manager_id` (`manager_id`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：MENU-菜单，BUTTON-按钮，DATA-数据',
  `resource` VARCHAR(200) DEFAULT NULL COMMENT '资源标识',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父权限ID',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `user_roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `role_permissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================
-- 绩效管理模块
-- ============================================

CREATE TABLE IF NOT EXISTS `performance_cycles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '周期ID',
  `name` VARCHAR(100) NOT NULL COMMENT '周期名称（如：2026年Q1）',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：QUARTERLY-季度，ANNUAL-年度，MONTHLY-月度',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `end_date` DATE NOT NULL COMMENT '结束日期',
  `status` VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态：NOT_STARTED-未开始，IN_PROGRESS-进行中，EVALUATING-评估中，COMPLETED-已完成',
  `created_by` BIGINT NOT NULL COMMENT '创建人ID',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效周期表';

CREATE TABLE IF NOT EXISTS `performance_plans` (
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

CREATE TABLE IF NOT EXISTS `indicators` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `name` VARCHAR(100) NOT NULL COMMENT '指标名称',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：KPI-关键绩效指标，OKR-目标与关键结果，BSC-平衡计分卡',
  `category` VARCHAR(20) DEFAULT NULL COMMENT '分类：FINANCIAL-财务，CUSTOMER-客户，INTERNAL-内部流程，LEARNING-学习与成长',
  `description` TEXT COMMENT '描述',
  `calculation_rule` TEXT COMMENT '计算规则',
  `calc_type` VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '计算方式：AUTO-自动，MANUAL-手动',
  `data_source` VARCHAR(20) DEFAULT NULL COMMENT '数据来源：API/MES/ERP/MANUAL',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父指标ID',
  `default_weight` DECIMAL(5,2) DEFAULT NULL COMMENT '默认权重',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_category` (`category`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标模板表';

CREATE TABLE IF NOT EXISTS `indicator_instances` (
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

CREATE TABLE IF NOT EXISTS `scores` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分ID',
  `plan_id` BIGINT NOT NULL COMMENT '绩效计划ID',
  `evaluator_id` BIGINT NOT NULL COMMENT '评估人ID',
  `score_type` VARCHAR(20) NOT NULL COMMENT '评分类型：SELF-自评，MANAGER-上级评价，PEER-同事评价，SUBORDINATE-下级评价，AUTO-自动评分',
  `score_value` DECIMAL(5,2) NOT NULL COMMENT '分数（0-100）',
  `comment` TEXT COMMENT '评语',
  `dimensions` JSON DEFAULT NULL COMMENT '维度评分详情',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_evaluator_type` (`plan_id`, `evaluator_id`, `score_type`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_evaluator_id` (`evaluator_id`),
  KEY `idx_score_type` (`score_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分表';

CREATE TABLE IF NOT EXISTS `calibrations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '校准ID',
  `cycle_id` BIGINT NOT NULL COMMENT '周期ID',
  `org_id` BIGINT NOT NULL COMMENT '组织ID',
  `plan_id` BIGINT NOT NULL COMMENT '绩效计划ID',
  `before_score` DECIMAL(5,2) NOT NULL COMMENT '校准前分数',
  `after_score` DECIMAL(5,2) NOT NULL COMMENT '校准后分数',
  `before_level` VARCHAR(10) DEFAULT NULL COMMENT '校准前等级',
  `after_level` VARCHAR(10) DEFAULT NULL COMMENT '校准后等级',
  `adjust_reason` TEXT NOT NULL COMMENT '调整原因',
  `calibrated_by` BIGINT NOT NULL COMMENT '校准人ID',
  `calibrated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '校准时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_cycle_id` (`cycle_id`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_calibrated_by` (`calibrated_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校准记录表';

-- ============================================
-- 系统配置模块
-- ============================================

CREATE TABLE IF NOT EXISTS `scoring_models` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模型ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模型名称',
  `code` VARCHAR(50) NOT NULL COMMENT '模型编码',
  `kpi_weight` DECIMAL(5,2) NOT NULL DEFAULT 60 COMMENT 'KPI权重',
  `okr_weight` DECIMAL(5,2) NOT NULL DEFAULT 30 COMMENT 'OKR权重',
  `peer_weight` DECIMAL(5,2) NOT NULL DEFAULT 10 COMMENT '360评价权重',
  `applicable_roles` JSON DEFAULT NULL COMMENT '适用角色',
  `applicable_orgs` JSON DEFAULT NULL COMMENT '适用组织',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分模型配置表';

CREATE TABLE IF NOT EXISTS `level_mappings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `model_id` BIGINT NOT NULL COMMENT '评分模型ID',
  `level` VARCHAR(10) NOT NULL COMMENT '等级：A/B/C/D',
  `min_score` DECIMAL(5,2) NOT NULL COMMENT '最低分数',
  `max_score` DECIMAL(5,2) NOT NULL COMMENT '最高分数',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `distribution_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT '分布比例（%）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='等级映射配置表';

CREATE TABLE IF NOT EXISTS `system_configs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_type` VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '类型：STRING/NUMBER/BOOLEAN/JSON',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `editable` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可编辑',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================
-- 通知模块
-- ============================================

CREATE TABLE IF NOT EXISTS `notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收人ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：SYSTEM-系统通知，TASK-任务提醒，APPROVAL-审批通知',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `related_type` VARCHAR(50) DEFAULT NULL COMMENT '关联类型',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联ID',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `read_at` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================
-- 审计日志模块
-- ============================================

CREATE TABLE IF NOT EXISTS `operation_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
  `operation` VARCHAR(100) NOT NULL COMMENT '操作类型',
  `module` VARCHAR(50) NOT NULL COMMENT '模块',
  `method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
  `url` VARCHAR(200) NOT NULL COMMENT '请求URL',
  `request_params` JSON DEFAULT NULL COMMENT '请求参数',
  `response_data` JSON DEFAULT NULL COMMENT '响应数据',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `duration` INT DEFAULT NULL COMMENT '耗时（毫秒）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS/FAILURE',
  `error_message` TEXT COMMENT '错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
