-- ============================================
-- Sprint 5: 评分系统测试数据
-- ============================================
-- 数据库：jxkh_db (远程: 113.45.154.4:33306)
-- 执行方式：mysql -h 113.45.154.4 -P 33306 -u root -p jxkh_db
-- 密码：Zhikeyunxin123$

-- ============================================
-- 1. 创建待评估状态的绩效计划（用于测试评分功能）
-- ============================================

-- 1.1 员工自评进度 100% 的计划（用于展示完整的自评）
INSERT INTO `performance_plans` (
    `user_id`, 
    `cycle_id`, 
    `org_id`, 
    `status`, 
    `total_score`, 
    `final_level`, 
    `final_score`,
    `evaluator_id`, 
    `comment`, 
    `submitted_at`, 
    `approved_at`, 
    `calculated_at`,
    `created_at`, 
    `updated_at`, 
    `created_by`, 
    `updated_by`,
    `is_deleted`
) VALUES
(
    2,                  -- user_id: 测试用户2（员工）
    1,                  -- cycle_id: 2026年第二季度
    1,                  -- org_id: 组织1
    'PENDING_EVAL',     -- status: 待评估（这样才能看到评分按钮）
    NULL,               -- total_score: 暂无总分
    NULL,               -- final_level: 暂无等级
    NULL,               -- final_score: 暂无最终得分
    1,                  -- evaluator_id: 评估人为主管（用户1/admin）
    NULL,               -- comment: 暂无评语
    '2026-04-01 10:00:00',  -- submitted_at: 提交时间
    '2026-04-05 14:30:00',  -- approved_at: 审批时间
    NULL,               -- calculated_at: 暂无计算时间
    NOW(),              -- created_at
    NOW(),              -- updated_at
    2,                  -- created_by
    2,                  -- updated_by
    0                   -- is_deleted
);

-- 1.2 员工自评进度 60% 的计划（用于展示部分自评）
INSERT INTO `performance_plans` (
    `user_id`, 
    `cycle_id`, 
    `org_id`, 
    `status`, 
    `total_score`, 
    `final_level`, 
    `final_score`,
    `evaluator_id`, 
    `comment`, 
    `submitted_at`, 
    `approved_at`, 
    `calculated_at`,
    `created_at`, 
    `updated_at`, 
    `created_by`, 
    `updated_by`,
    `is_deleted`
) VALUES
(
    3,                  -- user_id: 测试用户3（员工）
    1,                  -- cycle_id: 2026年第二季度
    1,                  -- org_id: 组织1
    'PENDING_EVAL',     -- status: 待评估
    NULL,               -- total_score
    NULL,               -- final_level
    NULL,               -- final_score
    1,                  -- evaluator_id: 评估人为主管（用户1/admin）
    NULL,               -- comment
    '2026-04-02 09:00:00',  -- submitted_at
    '2026-04-06 11:00:00',  -- approved_at
    NULL,               -- calculated_at
    NOW(),              -- created_at
    NOW(),              -- updated_at
    3,                  -- created_by
    3,                  -- updated_by
    0                   -- is_deleted
);

-- ============================================
-- 2. 为计划7（用户2）添加指标实例
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
    `updated_at`,
    `is_deleted`
) VALUES
-- 财务指标（3个）
(7, 1, 2, '年度营业收入', 'QUANTITATIVE', 20.00, 1000.00, 950.00, 95.00, 'COMPLETED', NULL, '万元', '营收目标基本达成', NOW(), NOW(), 0),
(7, 3, 2, '成本控制率', 'QUANTITATIVE', 15.00, 15.00, 13.00, 86.67, 'COMPLETED', NULL, '%', '成本控制良好', NOW(), NOW(), 0),
(7, 4, 2, '净利润率', 'QUANTITATIVE', 15.00, 20.00, 18.50, 92.50, 'COMPLETED', NULL, '%', '利润率优秀', NOW(), NOW(), 0),

-- 客户指标（3个）
(7, 5, 2, '客户满意度', 'QUANTITATIVE', 10.00, 90.00, 88.00, 97.78, 'COMPLETED', NULL, '分', '客户评价较高', NOW(), NOW(), 0),
(7, 6, 2, '客户投诉率', 'QUANTITATIVE', 5.00, 2.00, 1.20, 60.00, 'COMPLETED', NULL, '%', '投诉率控制优秀', NOW(), NOW(), 0),
(7, 7, 2, '客户留存率', 'QUANTITATIVE', 10.00, 95.00, 93.00, 97.89, 'COMPLETED', NULL, '%', '留存率稳定', NOW(), NOW(), 0),

-- 内部流程（2个）
(7, 10, 2, '项目按时完成率', 'QUANTITATIVE', 8.00, 100.00, 90.00, 90.00, 'COMPLETED', NULL, '%', '项目交付及时', NOW(), NOW(), 0),
(7, 12, 2, '产品缺陷率', 'QUANTITATIVE', 5.00, 1.00, 0.60, 60.00, 'COMPLETED', NULL, '%', '质量把控严格', NOW(), NOW(), 0),

-- 学习成长（2个）
(7, 14, 2, '培训完成率', 'QUANTITATIVE', 5.00, 100.00, 100.00, 100.00, 'COMPLETED', NULL, '%', '培训全部完成', NOW(), NOW(), 0),
(7, 16, 2, '技能认证通过率', 'QUANTITATIVE', 7.00, 80.00, 85.00, 106.25, 'COMPLETED', NULL, '%', '超额完成认证', NOW(), NOW(), 0);

-- ============================================
-- 3. 为计划8（用户3）添加指标实例
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
    `updated_at`,
    `is_deleted`
) VALUES
(8, 1, 3, '年度营业收入', 'QUANTITATIVE', 20.00, 800.00, 650.00, 81.25, 'IN_PROGRESS', NULL, '万元', '营收稳步增长', NOW(), NOW(), 0),
(8, 3, 3, '成本控制率', 'QUANTITATIVE', 15.00, 15.00, 14.00, 93.33, 'IN_PROGRESS', NULL, '%', '成本控制达标', NOW(), NOW(), 0),
(8, 4, 3, '净利润率', 'QUANTITATIVE', 15.00, 18.00, 16.50, 91.67, 'IN_PROGRESS', NULL, '%', '利润率良好', NOW(), NOW(), 0),
(8, 5, 3, '客户满意度', 'QUANTITATIVE', 10.00, 90.00, 85.00, 94.44, 'IN_PROGRESS', NULL, '分', '客户满意度较高', NOW(), NOW(), 0),
(8, 6, 3, '客户投诉率', 'QUANTITATIVE', 5.00, 2.00, 1.80, 90.00, 'IN_PROGRESS', NULL, '%', '投诉率控制较好', NOW(), NOW(), 0),
(8, 7, 3, '客户留存率', 'QUANTITATIVE', 10.00, 95.00, 90.00, 94.74, 'IN_PROGRESS', NULL, '%', '留存率稳定', NOW(), NOW(), 0),
(8, 10, 3, '项目按时完成率', 'QUANTITATIVE', 8.00, 100.00, 80.00, 80.00, 'IN_PROGRESS', NULL, '%', '大部分项目按时完成', NOW(), NOW(), 0),
(8, 12, 3, '产品缺陷率', 'QUANTITATIVE', 5.00, 1.00, 0.90, 90.00, 'IN_PROGRESS', NULL, '%', '质量基本达标', NOW(), NOW(), 0),
(8, 14, 3, '培训完成率', 'QUANTITATIVE', 5.00, 100.00, 60.00, 60.00, 'NOT_STARTED', NULL, '%', '培训进行中', NOW(), NOW(), 0),
(8, 16, 3, '技能认证通过率', 'QUANTITATIVE', 7.00, 80.00, 40.00, 50.00, 'NOT_STARTED', NULL, '%', '认证待完成', NOW(), NOW(), 0);

-- ============================================
-- 4. 为计划7添加自评数据（10个指标全部自评 - 100%进度）
-- ============================================

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
    `updated_by`,
    `is_deleted`
) VALUES
-- 指标实例ID 71-80 对应计划7的10个指标
(7, 71, 2, 88.00, '营业收入目标基本达成，市场拓展良好', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 72, 2, 85.00, '成本控制较好，但还有优化空间', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 73, 2, 90.00, '利润率表现优秀，超出预期', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 74, 2, 92.00, '客户满意度持续提升，服务质量改善', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 75, 2, 80.00, '投诉率控制良好，客户体验提升', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 76, 2, 95.00, '客户留存率保持稳定，关系维护到位', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 77, 2, 87.00, '项目交付及时率较高', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 78, 2, 82.00, '产品质量把控严格，缺陷率较低', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 79, 2, 95.00, '培训全部完成，学习积极', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(7, 80, 2, 90.00, '技能认证超额完成，能力提升明显', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0);

-- ============================================
-- 5. 为计划7添加上级评数据（前6个指标已评 - 60%进度）
-- ============================================

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
    `updated_by`,
    `is_deleted`
) VALUES
-- 主管（用户1）对前6个指标评分
(7, 71, 1, 85.00, '营收达成情况良好，市场开拓有成效', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(7, 72, 1, 82.00, '成本控制符合预期，建议进一步优化流程', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(7, 73, 1, 88.00, '利润率表现优秀，继续保持', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(7, 74, 1, 90.00, '客户满意度高，服务质量提升明显', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(7, 75, 1, 78.00, '投诉率控制较好，但仍有改进空间', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(7, 76, 1, 92.00, '客户留存率优秀，关系维护到位', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0);

-- ============================================
-- 6. 为计划8添加自评数据（6个指标自评 - 60%进度）
-- ============================================

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
    `updated_by`,
    `is_deleted`
) VALUES
-- 员工（用户3）对前6个指标自评
(8, 81, 3, 82.00, '营业收入稳步增长，基本达成目标', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(8, 82, 3, 88.00, '成本控制达标，流程优化有成效', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(8, 83, 3, 85.00, '利润率良好，盈利能力稳定', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(8, 84, 3, 87.00, '客户满意度较高，服务持续改善', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(8, 85, 3, 80.00, '投诉率控制较好', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(8, 86, 3, 85.00, '客户留存率稳定', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0);

-- ============================================
-- 7. 为计划8添加上级评数据（前4个指标已评 - 40%进度）
-- ============================================

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
    `updated_by`,
    `is_deleted`
) VALUES
-- 主管（用户1）对前4个指标评分
(8, 81, 1, 80.00, '营收增长符合预期，继续保持', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(8, 82, 1, 85.00, '成本控制达标，建议进一步优化', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(8, 83, 1, 83.00, '利润率表现良好', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(8, 84, 1, 86.00, '客户满意度较高，服务质量不错', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0);

-- ============================================
-- 验证数据插入
-- ============================================

-- 查看计划7和8的指标实例ID
SELECT 
    ii.id as instance_id,
    ii.plan_id,
    ii.name,
    ii.weight
FROM indicator_instances ii
WHERE ii.plan_id IN (7, 8)
ORDER BY ii.plan_id, ii.id;

-- 查看评分统计
SELECT 
    s.plan_id,
    s.type,
    COUNT(*) as scored_count,
    AVG(s.score) as avg_score
FROM scores s
WHERE s.plan_id IN (7, 8)
GROUP BY s.plan_id, s.type
ORDER BY s.plan_id, s.type;

-- ============================================
-- 说明
-- ============================================
-- 测试数据说明：
-- 1. 计划7（用户2）：自评 10/10 (100%)，上级评 6/10 (60%) - 展示部分评分完成的状态
-- 2. 计划8（用户3）：自评 6/10 (60%)，上级评 4/10 (40%) - 展示评分进行中的状态
--
-- 评分规则：
-- - 自评权重 30%，上级评权重 70%
-- - 等级判定：A(≥90), B(≥80), C(≥70), D(<70)
-- - 加权平均：总分 = Σ(指标分数 × 权重) / 100
--
-- 访问方式：
-- - 员工自评：登录用户2，访问"绩效管理 → 员工自评"
-- - 上级评分：登录用户1（admin），访问"绩效管理 → 上级评分"
