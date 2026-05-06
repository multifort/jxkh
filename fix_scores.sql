-- 删除旧的评分数据
DELETE FROM scores WHERE plan_id IN (5, 6);

-- 计划5自评（10个指标）
INSERT INTO scores (plan_id, indicator_instance_id, evaluator_id, score, comment, type, status, created_at, updated_at, created_by, updated_by, is_deleted, version) VALUES
(5, 1, 2, 88.00, '营业收入目标基本达成', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 2, 2, 85.00, '成本控制较好', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 3, 2, 90.00, '利润率表现优秀', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 4, 2, 92.00, '客户满意度持续提升', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 5, 2, 80.00, '投诉率控制良好', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 6, 2, 95.00, '客户留存率稳定', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 7, 2, 87.00, '项目交付及时', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 8, 2, 82.00, '产品质量把控严格', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 9, 2, 95.00, '培训全部完成', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0),
(5, 10, 2, 90.00, '技能认证超额完成', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0, 0);

-- 计划5上级评（前6个指标）
INSERT INTO scores (plan_id, indicator_instance_id, evaluator_id, score, comment, type, status, created_at, updated_at, created_by, updated_by, is_deleted, version) VALUES
(5, 1, 1, 85.00, '营收达成情况良好', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0),
(5, 2, 1, 82.00, '成本控制符合预期', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0),
(5, 3, 1, 88.00, '利润率表现优秀', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0),
(5, 4, 1, 90.00, '客户满意度高', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0),
(5, 5, 1, 78.00, '投诉率控制较好', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0),
(5, 6, 1, 92.00, '客户留存率优秀', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0);

-- 计划6自评（前6个指标）
INSERT INTO scores (plan_id, indicator_instance_id, evaluator_id, score, comment, type, status, created_at, updated_at, created_by, updated_by, is_deleted, version) VALUES
(6, 11, 3, 82.00, '营业收入稳步增长', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0, 0),
(6, 12, 3, 88.00, '成本控制达标', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0, 0),
(6, 13, 3, 85.00, '利润率良好', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0, 0),
(6, 14, 3, 87.00, '客户满意度较高', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0, 0),
(6, 15, 3, 80.00, '投诉率控制较好', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0, 0),
(6, 16, 3, 85.00, '客户留存率稳定', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0, 0);

-- 计划6上级评（前4个指标）
INSERT INTO scores (plan_id, indicator_instance_id, evaluator_id, score, comment, type, status, created_at, updated_at, created_by, updated_by, is_deleted, version) VALUES
(6, 11, 1, 80.00, '营收增长符合预期', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0),
(6, 12, 1, 85.00, '成本控制达标', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0),
(6, 13, 1, 83.00, '利润率表现良好', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0),
(6, 14, 1, 86.00, '客户满意度较高', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0, 0);

SELECT '评分数据插入成功' as result;
