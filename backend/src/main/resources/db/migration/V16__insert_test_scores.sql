-- V16__insert_test_scores.sql
-- Sprint 5: 评分测试数据
-- 版本: 16
-- 日期: 2026-05-06

-- ============================================
-- 1. 创建待评估状态的绩效计划（用于测试评分功能）
-- ============================================

-- 1.1 为员工用户4创建待评估计划（计划ID=6）
INSERT INTO `performance_plans` (
    `user_id`, 
    `cycle_id`, 
    `org_id`, 
    `status`, 
    `total_score`, 
    `final_level`, 
    `evaluator_id`, 
    `comment`, 
    `submitted_at`, 
    `approved_at`, 
    `created_at`, 
    `updated_at`, 
    `created_by`, 
    `updated_by`
) VALUES
(
    4,                  -- user_id: 员工用户4
    1,                  -- cycle_id: 2026年第二季度
    1,                  -- org_id: 组织1
    'PENDING_EVAL',     -- status: 待评估（这样才能进行评分）
    NULL,               -- total_score: 暂无评分
    NULL,               -- final_level: 暂无等级
    1,                  -- evaluator_id: 上级为用户1（admin）
    NULL,               -- comment: 暂无评语
    '2026-04-01 10:00:00',  -- submitted_at: 提交时间
    '2026-04-05 14:30:00',  -- approved_at: 审批时间
    NOW(),              -- created_at
    NOW(),              -- updated_at
    4,                  -- created_by
    4                   -- updated_by
);

-- 1.2 为员工用户5创建待评估计划（计划ID=7）
INSERT INTO `performance_plans` (
    `user_id`, 
    `cycle_id`, 
    `org_id`, 
    `status`, 
    `total_score`, 
    `final_level`, 
    `evaluator_id`, 
    `comment`, 
    `submitted_at`, 
    `approved_at`, 
    `created_at`, 
    `updated_at`, 
    `created_by`, 
    `updated_by`
) VALUES
(
    5,                  -- user_id: 员工用户5
    1,                  -- cycle_id: 2026年第二季度
    1,                  -- org_id: 组织1
    'PENDING_EVAL',     -- status: 待评估
    NULL,               -- total_score
    NULL,               -- final_level
    1,                  -- evaluator_id: 上级为用户1（admin）
    NULL,               -- comment
    '2026-04-01 10:00:00',  -- submitted_at
    '2026-04-05 14:30:00',  -- approved_at
    NOW(),              -- created_at
    NOW(),              -- updated_at
    5,                  -- created_by
    5                   -- updated_by
);

-- ============================================
-- 2. 为计划6添加指标实例
-- ============================================

INSERT INTO `indicator_instances` (
    `plan_id`, 
    `indicator_id`, 
    `owner_id`,
    `name`, 
    `type`, 
    `weight`, 
    `target_value`, 
    `current_value`, 
    `progress`,
    `status`,
    `score`,
    `unit`, 
    `remark`, 
    `created_at`, 
    `updated_at`
) VALUES
-- 财务指标（权重总和 = 100%）
(6, 1, 4, '年度营业收入', 'QUANTITATIVE', 20.00, 1000.00, 850.00, 85.00, 'IN_PROGRESS', NULL, '万元', '营收目标达成良好', NOW(), NOW()),
(6, 3, 4, '成本控制率', 'QUANTITATIVE', 15.00, 15.00, 13.00, 86.67, 'IN_PROGRESS', NULL, '%', '成本控制较好', NOW(), NOW()),
(6, 4, 4, '净利润率', 'QUANTITATIVE', 15.00, 20.00, 18.50, 92.50, 'IN_PROGRESS', NULL, '%', '利润率优秀', NOW(), NOW()),

-- 客户指标
(6, 5, 4, '客户满意度', 'QUANTITATIVE', 15.00, 90.00, 92.00, 102.22, 'IN_PROGRESS', NULL, '分', '客户评价很高', NOW(), NOW()),
(6, 7, 4, '客户留存率', 'QUANTITATIVE', 15.00, 95.00, 94.00, 98.95, 'IN_PROGRESS', NULL, '%', '留存率稳定', NOW(), NOW()),

-- 内部流程
(6, 10, 4, '项目按时完成率', 'QUANTITATIVE', 10.00, 100.00, 95.00, 95.00, 'IN_PROGRESS', NULL, '%', '项目按时交付', NOW(), NOW()),
(6, 12, 4, '产品缺陷率', 'QUANTITATIVE', 5.00, 1.00, 0.50, 50.00, 'IN_PROGRESS', NULL, '%', '质量把控优秀', NOW(), NOW()),

-- 学习成长
(6, 14, 4, '培训完成率', 'QUANTITATIVE', 5.00, 100.00, 100.00, 100.00, 'COMPLETED', NULL, '%', '培训全部完成', NOW(), NOW());

-- ============================================
-- 3. 为计划7添加指标实例
-- ============================================

INSERT INTO `indicator_instances` (
    `plan_id`, 
    `indicator_id`, 
    `owner_id`,
    `name`, 
    `type`, 
    `weight`, 
    `target_value`, 
    `current_value`, 
    `progress`,
    `status`,
    `score`,
    `unit`, 
    `remark`, 
    `created_at`, 
    `updated_at`
) VALUES
-- 销售相关指标（权重总和 = 100%）
(7, 1, 5, '年度营业收入', 'QUANTITATIVE', 25.00, 800.00, 720.00, 90.00, 'IN_PROGRESS', NULL, '万元', '营收接近目标', NOW(), NOW()),
(7, 2, 5, '季度营业收入', 'QUANTITATIVE', 15.00, 200.00, 185.00, 92.50, 'IN_PROGRESS', NULL, '万元', '季度表现良好', NOW(), NOW()),
(7, 5, 5, '客户满意度', 'QUANTITATIVE', 15.00, 90.00, 88.00, 97.78, 'IN_PROGRESS', NULL, '分', '客户评价较高', NOW(), NOW()),
(7, 6, 5, '客户投诉率', 'QUANTITATIVE', 10.00, 2.00, 1.80, 90.00, 'IN_PROGRESS', NULL, '%', '投诉率较低', NOW(), NOW()),
(7, 7, 5, '客户留存率', 'QUANTITATIVE', 15.00, 95.00, 93.00, 97.89, 'IN_PROGRESS', NULL, '%', '留存率稳定', NOW(), NOW()),
(7, 8, 5, '新客户增长率', 'QUANTITATIVE', 10.00, 20.00, 18.00, 90.00, 'IN_PROGRESS', NULL, '%', '新客户增长良好', NOW(), NOW()),
(7, 13, 5, '客户响应时间', 'QUANTITATIVE', 5.00, 24.00, 20.00, 83.33, 'IN_PROGRESS', NULL, '小时', '响应速度较快', NOW(), NOW()),
(7, 14, 5, '培训完成率', 'QUANTITATIVE', 5.00, 100.00, 95.00, 95.00, 'IN_PROGRESS', NULL, '%', '培训基本完成', NOW(), NOW());

-- ============================================
-- 4. 为计划6插入自评数据（用户2自评）
-- ============================================

-- 假设自评分数普遍较高（85-95分）
INSERT INTO `scores` (
    `plan_id`, 
    `indicator_instance_id`, 
    `evaluator_id`, 
    `score`, 
    `comment`, 
    `type`, 
    `status`, 
    `created_at`, 
    `updated_at`, 
    `created_by`, 
    `updated_by`
) VALUES
-- 获取指标实例ID（需要动态查询，这里使用占位符，实际执行时需要替换）
-- 注意：由于 indicator_instance_id 是自增的，我们需要先查询出来
-- 这里使用子查询方式插入

-- 自评：年度营业收入 - 90分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 1 LIMIT 1), 4, 90.00, '本年度营收目标完成良好，超额完成预期', 'SELF', 'SUBMITTED', NOW(), NOW(), 4, 4),

-- 自评：成本控制率 - 88分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 3 LIMIT 1), 4, 88.00, '成本控制措施有效，各项费用控制在预算内', 'SELF', 'SUBMITTED', NOW(), NOW(), 4, 4),

-- 自评：净利润率 - 92分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 4 LIMIT 1), 4, 92.00, '利润率表现优秀，超出预期目标', 'SELF', 'SUBMITTED', NOW(), NOW(), 4, 4),

-- 自评：客户满意度 - 95分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 5 LIMIT 1), 4, 95.00, '客户反馈非常积极，多次获得表扬', 'SELF', 'SUBMITTED', NOW(), NOW(), 4, 4),

-- 自评：客户留存率 - 93分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 7 LIMIT 1), 4, 93.00, '老客户维护良好，流失率极低', 'SELF', 'SUBMITTED', NOW(), NOW(), 4, 4),

-- 自评：项目按时完成率 - 87分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 10 LIMIT 1), 4, 87.00, '大部分项目按时交付，个别项目略有延期', 'SELF', 'SUBMITTED', NOW(), NOW(), 4, 4),

-- 自评：产品缺陷率 - 91分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 12 LIMIT 1), 4, 91.00, '产品质量把控严格，缺陷率低于行业标准', 'SELF', 'SUBMITTED', NOW(), NOW(), 4, 4),

-- 自评：培训完成率 - 96分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 14 LIMIT 1), 4, 96.00, '积极参加各类培训，技能提升明显', 'SELF', 'SUBMITTED', NOW(), NOW(), 4, 4);

-- ============================================
-- 5. 为计划6插入上级评数据（用户1评用户2）
-- ============================================

-- 假设上级评分略低于自评（82-90分）
INSERT INTO `scores` (
    `plan_id`, 
    `indicator_instance_id`, 
    `evaluator_id`, 
    `score`, 
    `comment`, 
    `type`, 
    `status`, 
    `created_at`, 
    `updated_at`, 
    `created_by`, 
    `updated_by`
) VALUES
-- 上级评：年度营业收入 - 88分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 1 LIMIT 1), 1, 88.00, '营收目标完成较好，但仍有提升空间', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：成本控制率 - 85分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 3 LIMIT 1), 1, 85.00, '成本控制基本达标，部分项目成本偏高', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：净利润率 - 90分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 4 LIMIT 1), 1, 90.00, '利润率表现良好，符合预期', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：客户满意度 - 92分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 5 LIMIT 1), 1, 92.00, '客户评价很高，服务意识强', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：客户留存率 - 90分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 7 LIMIT 1), 1, 90.00, '客户维护工作到位，留存率稳定', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：项目按时完成率 - 84分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 10 LIMIT 1), 1, 84.00, '项目交付及时性有待提高', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：产品缺陷率 - 88分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 12 LIMIT 1), 1, 88.00, '质量控制良好，但需持续改进', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：培训完成率 - 93分
(6, (SELECT id FROM indicator_instances WHERE plan_id = 6 AND indicator_id = 14 LIMIT 1), 1, 93.00, '学习态度积极，培训完成度高', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1);

-- ============================================
-- 6. 为计划7插入自评数据（用户3自评）
-- ============================================

-- 假设自评分数中等偏上（80-90分）
INSERT INTO `scores` (
    `plan_id`, 
    `indicator_instance_id`, 
    `evaluator_id`, 
    `score`, 
    `comment`, 
    `type`, 
    `status`, 
    `created_at`, 
    `updated_at`, 
    `created_by`, 
    `updated_by`
) VALUES
-- 自评：年度营业收入 - 85分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 1 LIMIT 1), 5, 85.00, '营收目标基本达成，市场拓展有成效', 'SELF', 'SUBMITTED', NOW(), NOW(), 5, 5),

-- 自评：季度营业收入 - 87分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 2 LIMIT 1), 5, 87.00, '季度业绩稳步增长', 'SELF', 'SUBMITTED', NOW(), NOW(), 5, 5),

-- 自评：客户满意度 - 88分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 5 LIMIT 1), 5, 88.00, '客户服务态度得到认可', 'SELF', 'SUBMITTED', NOW(), NOW(), 5, 5),

-- 自评：客户投诉率 - 82分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 6 LIMIT 1), 5, 82.00, '投诉处理及时，但仍有改进空间', 'SELF', 'SUBMITTED', NOW(), NOW(), 5, 5),

-- 自评：客户留存率 - 86分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 7 LIMIT 1), 5, 86.00, '客户关系维护良好', 'SELF', 'SUBMITTED', NOW(), NOW(), 5, 5),

-- 自评：新客户增长率 - 84分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 8 LIMIT 1), 5, 84.00, '新客户开发进展顺利', 'SELF', 'SUBMITTED', NOW(), NOW(), 5, 5),

-- 自评：客户响应时间 - 89分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 13 LIMIT 1), 5, 89.00, '响应速度快，客户反馈及时', 'SELF', 'SUBMITTED', NOW(), NOW(), 5, 5),

-- 自评：培训完成率 - 90分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 14 LIMIT 1), 5, 90.00, '积极参与培训，能力提升明显', 'SELF', 'SUBMITTED', NOW(), NOW(), 5, 5);

-- ============================================
-- 7. 为计划7插入上级评数据（用户1评用户3）
-- ============================================

-- 假设上级评分略低（78-86分）
INSERT INTO `scores` (
    `plan_id`, 
    `indicator_instance_id`, 
    `evaluator_id`, 
    `score`, 
    `comment`, 
    `type`, 
    `status`, 
    `created_at`, 
    `updated_at`, 
    `created_by`, 
    `updated_by`
) VALUES
-- 上级评：年度营业收入 - 82分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 1 LIMIT 1), 1, 82.00, '营收目标基本完成，需加强市场开拓', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：季度营业收入 - 84分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 2 LIMIT 1), 1, 84.00, '季度业绩稳定，但增长放缓', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：客户满意度 - 86分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 5 LIMIT 1), 1, 86.00, '客户服务良好，继续保持', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：客户投诉率 - 80分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 6 LIMIT 1), 1, 80.00, '投诉处理需要更加主动', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：客户留存率 - 83分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 7 LIMIT 1), 1, 83.00, '客户维护工作需要加强', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：新客户增长率 - 81分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 8 LIMIT 1), 1, 81.00, '新客户开发力度不够', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：客户响应时间 - 85分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 13 LIMIT 1), 1, 85.00, '响应速度尚可，需进一步提升', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1),

-- 上级评：培训完成率 - 88分
(7, (SELECT id FROM indicator_instances WHERE plan_id = 7 AND indicator_id = 14 LIMIT 1), 1, 88.00, '培训参与度良好', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1);

-- ============================================
-- 8. 验证数据
-- ============================================

-- 查看计划6的评分进度
-- SELECT 
--     p.id as plan_id,
--     p.status,
--     COUNT(DISTINCT CASE WHEN s.type = 'SELF' THEN s.id END) as self_score_count,
--     COUNT(DISTINCT CASE WHEN s.type = 'MANAGER' THEN s.id END) as manager_score_count,
--     COUNT(i.id) as total_indicators
-- FROM performance_plans p
-- LEFT JOIN indicator_instances i ON p.id = i.plan_id
-- LEFT JOIN scores s ON p.id = s.plan_id
-- WHERE p.id = 6
-- GROUP BY p.id;

-- 预期结果：
-- plan_id: 6
-- status: PENDING_EVAL
-- self_score_count: 8
-- manager_score_count: 8
-- total_indicators: 8
