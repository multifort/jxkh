-- ============================================
-- Sprint 5 评分系统测试数据 - 手动执行脚本
-- ============================================
-- 数据库：jxkh_db (远程: 113.45.154.4:33306)
-- 执行方式：mysql -h 113.45.154.4 -P 33306 -u root -p'Zhikeyunxin123$' jxkh_db

-- ============================================
-- 1. 更新现有计划5和6为待评估状态
-- ============================================

UPDATE performance_plans 
SET 
    status = 'PENDING_EVAL',
    evaluator_id = 1,
    updated_at = NOW()
WHERE id = 5;

UPDATE performance_plans 
SET 
    status = 'PENDING_EVAL',
    submitted_at = '2026-04-02 09:00:00',
    approved_at = '2026-04-06 11:00:00',
    evaluator_id = 1,
    updated_at = NOW()
WHERE id = 6;

-- ============================================
-- 2. 查看计划5和6的指标实例ID
-- ============================================

SELECT '计划5的指标实例ID：' as info;
SELECT id, plan_id, name, weight FROM indicator_instances WHERE plan_id = 5 ORDER BY id;

SELECT '计划6的指标实例ID：' as info;
SELECT id, plan_id, name, weight FROM indicator_instances WHERE plan_id = 6 ORDER BY id;

-- ============================================
-- 3. 为计划5添加自评数据（10个指标全部自评 - 100%进度）
-- ============================================
-- 注意：需要根据上一步查询结果调整 indicator_instance_id

INSERT INTO scores (plan_id, indicator_instance_id, evaluator_id, score, comment, type, status, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
-- 计划5的自评（用户2）- 假设指标实例ID是 51-60
(5, 51, 2, 88.00, '营业收入目标基本达成，市场拓展良好', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 52, 2, 85.00, '成本控制较好，但还有优化空间', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 53, 2, 90.00, '利润率表现优秀，超出预期', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 54, 2, 92.00, '客户满意度持续提升，服务质量改善', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 55, 2, 80.00, '投诉率控制良好，客户体验提升', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 56, 2, 95.00, '客户留存率保持稳定，关系维护到位', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 57, 2, 87.00, '项目交付及时率较高', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 58, 2, 82.00, '产品质量把控严格，缺陷率较低', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 59, 2, 95.00, '培训全部完成，学习积极', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0),
(5, 60, 2, 90.00, '技能认证超额完成，能力提升明显', 'SELF', 'SUBMITTED', NOW(), NOW(), 2, 2, 0);

-- ============================================
-- 4. 为计划5添加上级评数据（前6个指标已评 - 60%进度）
-- ============================================

INSERT INTO scores (plan_id, indicator_instance_id, evaluator_id, score, comment, type, status, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
-- 主管（用户1）对计划5的前6个指标评分
(5, 51, 1, 85.00, '营收达成情况良好，市场开拓有成效', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(5, 52, 1, 82.00, '成本控制符合预期，建议进一步优化流程', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(5, 53, 1, 88.00, '利润率表现优秀，继续保持', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(5, 54, 1, 90.00, '客户满意度高，服务质量提升明显', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(5, 55, 1, 78.00, '投诉率控制较好，但仍有改进空间', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(5, 56, 1, 92.00, '客户留存率优秀，关系维护到位', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0);

-- ============================================
-- 5. 为计划6添加自评数据（6个指标自评 - 60%进度）
-- ============================================

INSERT INTO scores (plan_id, indicator_instance_id, evaluator_id, score, comment, type, status, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
-- 员工（用户3）对计划6的前6个指标自评 - 假设指标实例ID是 61-70
(6, 61, 3, 82.00, '营业收入稳步增长，基本达成目标', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(6, 62, 3, 88.00, '成本控制达标，流程优化有成效', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(6, 63, 3, 85.00, '利润率良好，盈利能力稳定', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(6, 64, 3, 87.00, '客户满意度较高，服务持续改善', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(6, 65, 3, 80.00, '投诉率控制较好', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0),
(6, 66, 3, 85.00, '客户留存率稳定', 'SELF', 'SUBMITTED', NOW(), NOW(), 3, 3, 0);

-- ============================================
-- 6. 为计划6添加上级评数据（前4个指标已评 - 40%进度）
-- ============================================

INSERT INTO scores (plan_id, indicator_instance_id, evaluator_id, score, comment, type, status, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
-- 主管（用户1）对计划6的前4个指标评分
(6, 61, 1, 80.00, '营收增长符合预期，继续保持', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(6, 62, 1, 85.00, '成本控制达标，建议进一步优化', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(6, 63, 1, 83.00, '利润率表现良好', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0),
(6, 64, 1, 86.00, '客户满意度较高，服务质量不错', 'MANAGER', 'SUBMITTED', NOW(), NOW(), 1, 1, 0);

-- ============================================
-- 7. 验证数据
-- ============================================

SELECT '评分数据统计：' as info;
SELECT 
    plan_id,
    type,
    COUNT(*) as scored_count,
    ROUND(AVG(score), 2) as avg_score
FROM scores 
WHERE plan_id IN (5, 6)
GROUP BY plan_id, type
ORDER BY plan_id, type;

SELECT '测试数据插入完成！' as result;

-- ============================================
-- 说明
-- ============================================
-- 测试数据说明：
-- 1. 计划5（用户2）：自评 10/10 (100%)，上级评 6/10 (60%) - 展示部分评分完成的状态
-- 2. 计划6（用户3）：自评 6/10 (60%)，上级评 4/10 (40%) - 展示评分进行中的状态
--
-- 评分规则：
-- - 自评权重 30%，上级评权重 70%
-- - 等级判定：A(≥90), B(≥80), C(≥70), D(<70)
-- - 加权平均：总分 = Σ(指标分数 × 权重) / 100
--
-- 访问方式：
-- - 员工自评：登录用户2，访问"绩效管理 → 员工自评"
-- - 上级评分：登录用户1（admin），访问"绩效管理 → 上级评分"
--
-- 注意事项：
-- 1. 执行前请先运行步骤2，查看实际的指标实例ID
-- 2. 根据查询结果调整步骤3-6中的 indicator_instance_id
-- 3. 如果指标实例ID不是 51-60 和 61-70，需要相应修改
