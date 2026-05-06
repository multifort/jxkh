-- V14__insert_test_performance_plans.sql
-- Sprint 4: 绩效计划测试数据
-- 版本: 14
-- 日期: 2026-04-30

-- ============================================
-- 1. 绩效计划测试数据
-- ============================================

-- 1.1 执行中的计划（用于测试进度跟踪）
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
    2,              -- user_id: 测试用户2
    1,              -- cycle_id: 2026年第二季度（进行中）
    1,              -- org_id: 组织1
    'IN_PROGRESS',  -- status: 执行中（这样才能看到进度跟踪按钮）
    NULL,           -- total_score: 暂无评分
    NULL,           -- final_level: 暂无等级
    1,              -- evaluator_id: 评估人为用户1（admin）
    NULL,           -- comment: 暂无评语
    '2026-04-01 10:00:00',  -- submitted_at: 提交时间
    '2026-04-05 14:30:00',  -- approved_at: 审批时间
    NOW(),          -- created_at
    NOW(),          -- updated_at
    1,              -- created_by
    1               -- updated_by
),

-- 1.2 草稿状态的计划（用于对比测试）
(
    3,              -- user_id: 测试用户3
    1,              -- cycle_id: 2026年第二季度
    1,              -- org_id: 组织1
    'DRAFT',        -- status: 草稿
    NULL,           -- total_score
    NULL,           -- final_level
    NULL,           -- evaluator_id
    NULL,           -- comment
    NULL,           -- submitted_at
    NULL,           -- approved_at
    NOW(),          -- created_at
    NOW(),          -- updated_at
    3,              -- created_by
    3               -- updated_by
);

-- ============================================
-- 2. 指标实例测试数据（为计划ID=1添加指标）
-- ============================================

-- 2.1 为计划5添加指标实例（计划5是 IN_PROGRESS 状态）
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
-- 财务指标
(5, 1, 2, '年度营业收入', 'QUANTITATIVE', 20.00, 1000.00, 750.00, 75.00, 'IN_PROGRESS', NULL, '万元', '目标达成良好', NOW(), NOW()),
(5, 3, 2, '成本控制率', 'QUANTITATIVE', 15.00, 15.00, 12.50, 83.33, 'IN_PROGRESS', NULL, '%', '成本控制较好', NOW(), NOW()),
(5, 4, 2, '净利润率', 'QUANTITATIVE', 15.00, 20.00, 17.50, 87.50, 'IN_PROGRESS', NULL, '%', '利润率达标', NOW(), NOW()),

-- 客户指标
(5, 5, 2, '客户满意度', 'QUANTITATIVE', 10.00, 90.00, 88.00, 97.78, 'IN_PROGRESS', NULL, '分', '客户评价较高', NOW(), NOW()),
(5, 6, 2, '客户投诉率', 'QUANTITATIVE', 5.00, 2.00, 1.50, 75.00, 'IN_PROGRESS', NULL, '%', '投诉率较低', NOW(), NOW()),
(5, 7, 2, '客户留存率', 'QUANTITATIVE', 10.00, 95.00, 92.00, 96.84, 'IN_PROGRESS', NULL, '%', '留存率稳定', NOW(), NOW()),

-- 内部流程
(5, 10, 2, '项目按时完成率', 'QUANTITATIVE', 8.00, 100.00, 85.00, 85.00, 'IN_PROGRESS', NULL, '%', '大部分项目按时完成', NOW(), NOW()),
(5, 12, 2, '产品缺陷率', 'QUANTITATIVE', 5.00, 1.00, 0.80, 80.00, 'IN_PROGRESS', NULL, '%', '质量把控良好', NOW(), NOW()),

-- 学习成长
(5, 14, 2, '培训完成率', 'QUANTITATIVE', 5.00, 100.00, 90.00, 90.00, 'IN_PROGRESS', NULL, '%', '培训基本完成', NOW(), NOW()),
(5, 16, 2, '技能认证通过率', 'QUANTITATIVE', 7.00, 80.00, 75.00, 93.75, 'IN_PROGRESS', NULL, '%', '技能提升明显', NOW(), NOW());
