# 数据库设计

## 1. 数据库概述

### 1.1 数据库选型
- **主数据库**：MySQL 8.0
- **字符集**：utf8mb4
- **排序规则**：utf8mb4_unicode_ci
- **存储引擎**：InnoDB

### 1.2 命名规范
- **表名**：小写字母 + 下划线，复数形式（如：`users`, `performance_plans`）
- **字段名**：小写字母 + 下划线（如：`user_name`, `created_at`）
- **索引名**：`idx_字段名` 或 `uk_字段名`（唯一索引）
- **外键名**：`fk_当前表_关联表`

### 1.3 通用字段
所有表包含以下审计字段：
```sql
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
```

---

## 2. 核心数据表设计

### 2.1 组织管理模块

#### 2.1.1 组织表（orgs）

```sql
CREATE TABLE `orgs` (
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
```

**索引说明**：
- `uk_code`：组织编码唯一索引
- `idx_parent_id`：查询子组织
- `idx_leader_id`：查询负责人管理的组织

---

### 2.2 用户管理模块

#### 2.2.1 用户表（users）

```sql
CREATE TABLE `users` (
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
```

**索引说明**：
- `uk_username`：用户名唯一索引
- `uk_employee_no`：工号唯一索引
- `idx_org_id`：查询组织下的用户
- `idx_manager_id`：查询下属员工
- `idx_role`：按角色筛选
- `idx_status`：按状态筛选

---

#### 2.2.2 角色表（roles）

```sql
CREATE TABLE `roles` (
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
```

---

#### 2.2.3 权限表（permissions）

```sql
CREATE TABLE `permissions` (
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
```

---

#### 2.2.4 用户角色关联表（user_roles）

```sql
CREATE TABLE `user_roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';
```

---

#### 2.2.5 角色权限关联表（role_permissions）

```sql
CREATE TABLE `role_permissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';
```

---

### 2.3 绩效管理模块

#### 2.3.1 绩效周期表（performance_cycles）

```sql
CREATE TABLE `performance_cycles` (
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
```

---

#### 2.3.2 绩效计划表（performance_plans）

```sql
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
```

**索引说明**：
- `uk_user_cycle`：同一用户同一周期只能有一个计划
- `idx_cycle_id`：查询周期下的所有计划
- `idx_org_id`：查询组织下的计划
- `idx_status`：按状态筛选
- `idx_evaluator_id`：查询待评估的计划

---

#### 2.3.3 指标模板表（indicators）

```sql
CREATE TABLE `indicators` (
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
```

---

#### 2.3.4 指标实例表（indicator_instances）

```sql
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
```

**业务约束**：
```sql
-- 同一计划下权重总和必须为 100
ALTER TABLE `indicator_instances` 
ADD CONSTRAINT `chk_weight` CHECK (`weight` >= 0 AND `weight` <= 100);
```

---

#### 2.3.5 绩效记录表（performance_records）

```sql
CREATE TABLE `performance_records` (
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
```

---

#### 2.3.6 评分表（scores）

```sql
CREATE TABLE `scores` (
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
```

**业务约束**：
```sql
-- 评分范围 0-100
ALTER TABLE `scores` 
ADD CONSTRAINT `chk_score_value` CHECK (`score_value` >= 0 AND `score_value` <= 100);
```

---

#### 2.3.7 校准记录表（calibrations）

```sql
CREATE TABLE `calibrations` (
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
```

---

#### 2.3.8 绩效面谈记录表（performance_interviews）

```sql
CREATE TABLE `performance_interviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '面谈ID',
  `plan_id` BIGINT NOT NULL COMMENT '绩效计划ID',
  `interviewer_id` BIGINT NOT NULL COMMENT '面谈人ID',
  `interviewee_id` BIGINT NOT NULL COMMENT '被面谈人ID',
  `interview_date` DATE NOT NULL COMMENT '面谈日期',
  `content` TEXT COMMENT '面谈内容',
  `improvement_plan` TEXT COMMENT '改进计划',
  `follow_up_date` DATE DEFAULT NULL COMMENT '跟进日期',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待进行，COMPLETED-已完成，CANCELLED-已取消',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_interviewer_id` (`interviewer_id`),
  KEY `idx_interviewee_id` (`interviewee_id`),
  KEY `idx_interview_date` (`interview_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效面谈记录表';
```

---

### 2.4 系统配置模块

#### 2.4.1 评分模型配置表（scoring_models）

```sql
CREATE TABLE `scoring_models` (
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
```

---

#### 2.4.2 等级映射配置表（level_mappings）

```sql
CREATE TABLE `level_mappings` (
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
```

---

#### 2.4.3 系统配置表（system_configs）

```sql
CREATE TABLE `system_configs` (
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
```

---

### 2.5 通知模块

#### 2.5.1 通知表（notifications）

```sql
CREATE TABLE `notifications` (
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
```

---

### 2.6 审计日志模块

#### 2.6.1 操作日志表（operation_logs）

```sql
CREATE TABLE `operation_logs` (
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
```

---

## 3. 视图设计

### 3.1 绩效看板视图

```sql
CREATE VIEW `v_performance_dashboard` AS
SELECT 
    pc.id AS cycle_id,
    pc.name AS cycle_name,
    COUNT(DISTINCT pp.user_id) AS total_employees,
    COUNT(DISTINCT CASE WHEN pp.status = 'COMPLETED' THEN pp.user_id END) AS completed_employees,
    ROUND(COUNT(DISTINCT CASE WHEN pp.status = 'COMPLETED' THEN pp.user_id END) * 100.0 / COUNT(DISTINCT pp.user_id), 2) AS completion_rate,
    ROUND(AVG(pp.total_score), 2) AS average_score,
    COUNT(DISTINCT CASE WHEN pp.total_score < 70 THEN pp.user_id END) AS risk_count
FROM performance_cycles pc
LEFT JOIN performance_plans pp ON pc.id = pp.cycle_id AND pp.is_deleted = 0
WHERE pc.is_deleted = 0
GROUP BY pc.id, pc.name;
```

---

### 3.2 部门绩效视图

```sql
CREATE VIEW `v_dept_performance` AS
SELECT 
    o.id AS org_id,
    o.name AS org_name,
    pc.id AS cycle_id,
    COUNT(pp.id) AS plan_count,
    ROUND(AVG(pp.total_score), 2) AS avg_score,
    MAX(pp.total_score) AS max_score,
    MIN(pp.total_score) AS min_score,
    COUNT(CASE WHEN pp.final_level = 'A' THEN 1 END) AS level_a_count,
    COUNT(CASE WHEN pp.final_level = 'B' THEN 1 END) AS level_b_count,
    COUNT(CASE WHEN pp.final_level = 'C' THEN 1 END) AS level_c_count,
    COUNT(CASE WHEN pp.final_level = 'D' THEN 1 END) AS level_d_count
FROM orgs o
LEFT JOIN performance_plans pp ON o.id = pp.org_id AND pp.is_deleted = 0
LEFT JOIN performance_cycles pc ON pp.cycle_id = pc.id
WHERE o.is_deleted = 0
GROUP BY o.id, o.name, pc.id;
```

---

## 4. 存储过程与函数

### 4.1 计算绩效计划总分

```sql
DELIMITER $$

CREATE PROCEDURE `calculate_plan_score`(IN p_plan_id BIGINT)
BEGIN
    DECLARE v_total_score DECIMAL(5,2);
    
    -- 计算加权总分
    SELECT SUM(ii.score * ii.weight / 100) INTO v_total_score
    FROM indicator_instances ii
    WHERE ii.plan_id = p_plan_id AND ii.is_deleted = 0;
    
    -- 更新计划总分
    UPDATE performance_plans 
    SET total_score = v_total_score,
        final_level = CASE 
            WHEN v_total_score >= 90 THEN 'A'
            WHEN v_total_score >= 80 THEN 'B'
            WHEN v_total_score >= 70 THEN 'C'
            ELSE 'D'
        END
    WHERE id = p_plan_id;
END$$

DELIMITER ;
```

---

## 5. 触发器

### 5.1 自动更新计划状态

```sql
DELIMITER $$

CREATE TRIGGER `tr_after_score_insert`
AFTER INSERT ON scores
FOR EACH ROW
BEGIN
    DECLARE v_score_count INT;
    DECLARE v_self_score INT;
    DECLARE v_manager_score INT;
    
    -- 检查是否完成所有评分
    SELECT COUNT(*) INTO v_score_count
    FROM scores
    WHERE plan_id = NEW.plan_id AND is_deleted = 0;
    
    SELECT COUNT(*) INTO v_self_score
    FROM scores
    WHERE plan_id = NEW.plan_id AND score_type = 'SELF' AND is_deleted = 0;
    
    SELECT COUNT(*) INTO v_manager_score
    FROM scores
    WHERE plan_id = NEW.plan_id AND score_type = 'MANAGER' AND is_deleted = 0;
    
    -- 如果自评和上级评分都完成，更新状态为已评估
    IF v_self_score > 0 AND v_manager_score > 0 THEN
        UPDATE performance_plans 
        SET status = 'EVALUATED', evaluated_at = NOW()
        WHERE id = NEW.plan_id AND status = 'PENDING_EVAL';
    END IF;
END$$

DELIMITER ;
```

---

## 6. 索引优化建议

### 6.1 复合索引

```sql
-- 绩效计划查询优化
CREATE INDEX idx_plan_query ON performance_plans (cycle_id, status, org_id);

-- 指标实例查询优化
CREATE INDEX idx_instance_query ON indicator_instances (plan_id, status);

-- 评分查询优化
CREATE INDEX idx_score_query ON scores (plan_id, score_type);
```

---

### 6.2 覆盖索引

```sql
-- 看板查询覆盖索引
CREATE INDEX idx_dashboard ON performance_plans (cycle_id, total_score, final_level, status);
```

---

## 7. 数据归档策略

### 7.1 历史数据归档

```sql
-- 创建归档表
CREATE TABLE `performance_plans_archive` LIKE `performance_plans`;
CREATE TABLE `indicator_instances_archive` LIKE `indicator_instances`;
CREATE TABLE `scores_archive` LIKE `scores`;

-- 归档存储过程
DELIMITER $$

CREATE PROCEDURE `archive_old_plans`(IN p_before_date DATE)
BEGIN
    -- 归档绩效计划
    INSERT INTO performance_plans_archive
    SELECT * FROM performance_plans
    WHERE updated_at < p_before_date;
    
    -- 归档指标实例
    INSERT INTO indicator_instances_archive
    SELECT ii.* FROM indicator_instances ii
    INNER JOIN performance_plans pp ON ii.plan_id = pp.id
    WHERE pp.updated_at < p_before_date;
    
    -- 归档评分
    INSERT INTO scores_archive
    SELECT s.* FROM scores s
    INNER JOIN performance_plans pp ON s.plan_id = pp.id
    WHERE pp.updated_at < p_before_date;
    
    -- 删除已归档数据
    DELETE pp FROM performance_plans pp
    WHERE pp.updated_at < p_before_date;
END$$

DELIMITER ;
```

---

## 8. 数据库初始化脚本

### 8.1 初始数据

```sql
-- 插入默认角色
INSERT INTO `roles` (`code`, `name`, `description`, `sort`) VALUES
('EMPLOYEE', '员工', '普通员工角色', 1),
('MANAGER', '主管', '团队主管角色', 2),
('HR', '人力资源', 'HR管理员角色', 3),
('ADMIN', '系统管理员', '系统管理员角色', 4);

-- 插入默认权限
INSERT INTO `permissions` (`code`, `name`, `type`, `resource`, `sort`) VALUES
('dashboard:view', '查看首页', 'MENU', '/dashboard', 1),
('performance:plan:view', '查看绩效计划', 'MENU', '/performance/plans', 2),
('performance:plan:create', '创建绩效计划', 'BUTTON', 'plan:create', 3),
('performance:plan:edit', '编辑绩效计划', 'BUTTON', 'plan:edit', 4),
('performance:plan:submit', '提交绩效计划', 'BUTTON', 'plan:submit', 5),
('performance:plan:approve', '审批绩效计划', 'BUTTON', 'plan:approve', 6),
('performance:eval:self', '自评', 'BUTTON', 'eval:self', 7),
('performance:eval:manager', '上级评价', 'BUTTON', 'eval:manager', 8),
('performance:calibrate', '绩效校准', 'BUTTON', 'calibrate', 9),
('system:config', '系统配置', 'MENU', '/system/config', 10);

-- 分配管理员权限
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 4, id FROM permissions; -- ADMIN 拥有所有权限
```

---

## 9. 数据库性能监控

### 9.1 慢查询监控

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2; -- 超过2秒的查询记录
```

### 9.2 索引使用情况监控

```sql
-- 查看未使用的索引
SELECT * FROM sys.schema_unused_indexes;

-- 查看缺失索引
SELECT * FROM sys.schema_tables_with_full_table_scans;
```

---

## 10. 备份策略

### 10.1 备份计划
- **全量备份**：每天凌晨 2:00
- **增量备份**：每小时一次
- **保留策略**：保留 30 天

### 10.2 备份脚本

```bash
#!/bin/bash
# 全量备份
mysqldump -u root -p \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  jxkh_performance > /backup/jxkh_$(date +%Y%m%d_%H%M%S).sql

# 压缩备份文件
gzip /backup/jxkh_*.sql
```

---

**文档版本**: V1.0  
**最后更新**: 2026-04-14  
**维护者**: 数据库团队
