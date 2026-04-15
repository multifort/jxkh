-- V2__init_data.sql
-- 初始化基础数据
-- 版本: 2.0
-- 日期: 2026-04-15

-- ============================================
-- 插入默认角色
-- ============================================

INSERT INTO `roles` (`code`, `name`, `description`, `sort`, `enabled`) VALUES
('EMPLOYEE', '员工', '普通员工角色', 1, 1),
('MANAGER', '主管', '团队主管角色', 2, 1),
('HR', '人力资源', 'HR管理员角色', 3, 1),
('ADMIN', '系统管理员', '系统管理员角色', 4, 1);

-- ============================================
-- 插入默认权限
-- ============================================

INSERT INTO `permissions` (`code`, `name`, `type`, `resource`, `path`, `sort`) VALUES
-- 首页模块
('dashboard:view', '查看首页', 'MENU', 'dashboard:view', '/dashboard', 1),

-- 绩效管理模块
('performance:plan:view', '查看绩效计划', 'MENU', 'performance:plan:view', '/performance/plans', 2),
('performance:plan:create', '创建绩效计划', 'BUTTON', 'performance:plan:create', NULL, 3),
('performance:plan:edit', '编辑绩效计划', 'BUTTON', 'performance:plan:edit', NULL, 4),
('performance:plan:delete', '删除绩效计划', 'BUTTON', 'performance:plan:delete', NULL, 5),
('performance:plan:submit', '提交绩效计划', 'BUTTON', 'performance:plan:submit', NULL, 6),
('performance:plan:approve', '审批绩效计划', 'BUTTON', 'performance:plan:approve', NULL, 7),

-- 绩效评估模块
('performance:eval:self', '自评', 'BUTTON', 'performance:eval:self', NULL, 8),
('performance:eval:manager', '上级评价', 'BUTTON', 'performance:eval:manager', NULL, 9),
('performance:eval:peer', '同事评价', 'BUTTON', 'performance:eval:peer', NULL, 10),

-- 绩效校准模块
('performance:calibrate', '绩效校准', 'BUTTON', 'performance:calibrate', '/performance/calibration', 11),

-- 进度跟踪模块
('performance:record:view', '查看进度记录', 'MENU', 'performance:record:view', '/performance/records', 12),
('performance:record:create', '创建进度记录', 'BUTTON', 'performance:record:create', NULL, 13),

-- 数据分析模块
('analytics:view', '查看数据分析', 'MENU', 'analytics:view', '/analytics', 14),
('analytics:export', '导出报表', 'BUTTON', 'analytics:export', NULL, 15),

-- 组织管理模块
('org:view', '查看组织', 'MENU', 'org:view', '/organization', 16),
('org:create', '创建组织', 'BUTTON', 'org:create', NULL, 17),
('org:edit', '编辑组织', 'BUTTON', 'org:edit', NULL, 18),
('org:delete', '删除组织', 'BUTTON', 'org:delete', NULL, 19),

-- 用户管理模块
('user:view', '查看用户', 'MENU', 'user:view', '/users', 20),
('user:create', '创建用户', 'BUTTON', 'user:create', NULL, 21),
('user:edit', '编辑用户', 'BUTTON', 'user:edit', NULL, 22),
('user:delete', '删除用户', 'BUTTON', 'user:delete', NULL, 23),
('user:import', '批量导入用户', 'BUTTON', 'user:import', NULL, 24),

-- 指标管理模块
('indicator:view', '查看指标库', 'MENU', 'indicator:view', '/indicators', 25),
('indicator:create', '创建指标', 'BUTTON', 'indicator:create', NULL, 26),
('indicator:edit', '编辑指标', 'BUTTON', 'indicator:edit', NULL, 27),
('indicator:delete', '删除指标', 'BUTTON', 'indicator:delete', NULL, 28),

-- 系统配置模块
('system:config', '系统配置', 'MENU', 'system:config', '/system/config', 29),
('system:role', '角色管理', 'MENU', 'system:role', '/system/roles', 30),
('system:permission', '权限管理', 'MENU', 'system:permission', '/system/permissions', 31);

-- ============================================
-- 分配管理员权限（ADMIN 拥有所有权限）
-- ============================================

INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 4, id FROM permissions WHERE is_deleted = 0;

-- ============================================
-- 分配 HR 权限
-- ============================================

INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 3, id FROM permissions 
WHERE code IN (
    'dashboard:view',
    'performance:plan:view',
    'performance:plan:approve',
    'performance:calibrate',
    'performance:record:view',
    'analytics:view',
    'analytics:export',
    'org:view',
    'user:view',
    'indicator:view'
) AND is_deleted = 0;

-- ============================================
-- 分配主管权限
-- ============================================

INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 2, id FROM permissions 
WHERE code IN (
    'dashboard:view',
    'performance:plan:view',
    'performance:plan:approve',
    'performance:eval:manager',
    'performance:record:view',
    'analytics:view',
    'org:view',
    'user:view',
    'indicator:view'
) AND is_deleted = 0;

-- ============================================
-- 分配员工权限
-- ============================================

INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT 1, id FROM permissions 
WHERE code IN (
    'dashboard:view',
    'performance:plan:view',
    'performance:plan:create',
    'performance:plan:edit',
    'performance:plan:submit',
    'performance:eval:self',
    'performance:record:view',
    'performance:record:create',
    'indicator:view'
) AND is_deleted = 0;

-- ============================================
-- 插入默认评分模型
-- ============================================

INSERT INTO `scoring_models` (`name`, `code`, `kpi_weight`, `okr_weight`, `peer_weight`, `applicable_roles`, `enabled`) VALUES
('标准评分模型', 'STANDARD', 60.00, 30.00, 10.00, '["EMPLOYEE", "MANAGER"]', 1),
('管理层评分模型', 'MANAGER', 50.00, 30.00, 20.00, '["MANAGER"]', 1);

-- ============================================
-- 插入等级映射配置
-- ============================================

-- 标准模型的等级映射
INSERT INTO `level_mappings` (`model_id`, `level`, `min_score`, `max_score`, `description`, `distribution_ratio`) VALUES
(1, 'A', 90.00, 100.00, '优秀：远超预期目标', 20.00),
(1, 'B', 80.00, 89.99, '良好：达到并部分超越目标', 50.00),
(1, 'C', 70.00, 79.99, '合格：基本达到目标', 25.00),
(1, 'D', 0.00, 69.99, '待改进：未达到目标', 5.00);

-- 管理层模型的等级映射
INSERT INTO `level_mappings` (`model_id`, `level`, `min_score`, `max_score`, `description`, `distribution_ratio`) VALUES
(2, 'A', 90.00, 100.00, '优秀：卓越领导力', 15.00),
(2, 'B', 80.00, 89.99, '良好：有效管理', 55.00),
(2, 'C', 70.00, 79.99, '合格：基本胜任', 25.00),
(2, 'D', 0.00, 69.99, '待改进：需要提升', 5.00);

-- ============================================
-- 插入系统配置
-- ============================================

INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `category`, `editable`) VALUES
('system.name', '企业绩效考核系统', 'STRING', '系统名称', 'basic', 0),
('system.version', '1.0.0', 'STRING', '系统版本', 'basic', 0),
('auth.login.max-fail-count', '5', 'NUMBER', '登录最大失败次数', 'security', 1),
('auth.login.lock-duration', '1800', 'NUMBER', '账号锁定时长（秒）', 'security', 1),
('jwt.access-token.expiration', '86400000', 'NUMBER', 'Access Token 过期时间（毫秒）', 'security', 1),
('jwt.refresh-token.expiration', '604800000', 'NUMBER', 'Refresh Token 过期时间（毫秒）', 'security', 1),
('performance.cycle.default-type', 'QUARTERLY', 'STRING', '默认绩效周期类型', 'performance', 1),
('performance.plan.weight-validation', 'true', 'BOOLEAN', '是否启用权重校验', 'performance', 1),
('performance.calibration.enabled', 'true', 'BOOLEAN', '是否启用绩效校准', 'performance', 1),
('notification.email.enabled', 'false', 'BOOLEAN', '是否启用邮件通知', 'notification', 1),
('ai.summary.enabled', 'true', 'BOOLEAN', '是否启用 AI 总结', 'ai', 1),
('cache.dashboard.ttl', '3600', 'NUMBER', '看板缓存过期时间（秒）', 'cache', 1);
