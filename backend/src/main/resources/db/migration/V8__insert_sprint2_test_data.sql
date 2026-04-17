-- V8__insert_sprint2_test_data.sql
-- Sprint2 功能测试数据
-- 版本: 8.0
-- 日期: 2026-04-17

-- ============================================
-- 1. 绩效周期测试数据
-- ============================================

-- 1.1 进行中的季度周期（2026 Q2）
INSERT INTO `performance_cycles` (`name`, `type`, `start_date`, `end_date`, `status`, `org_id`, `remark`, `created_by`, `updated_by`) VALUES
('2026年第二季度', 'QUARTERLY', '2026-04-01', '2026-06-30', 'IN_PROGRESS', NULL, '2026年Q2绩效周期', 1, 1),

-- 1.2 已结束的季度周期（2026 Q1）
('2026年第一季度', 'QUARTERLY', '2026-01-01', '2026-03-31', 'ENDED', NULL, '2026年Q1绩效周期', 1, 1),

-- 1.3 草稿状态的年度周期
('2026年度', 'ANNUAL', '2026-01-01', '2026-12-31', 'DRAFT', NULL, '2026年全年绩效周期', 1, 1),

-- 1.4 特定部门的月度周期
('2026年4月-研发部', 'MONTHLY', '2026-04-01', '2026-04-30', 'IN_PROGRESS', 1, '研发部4月绩效周期', 1, 1);

-- ============================================
-- 2. 指标分类测试数据
-- ============================================

-- 2.1 一级分类（根分类）
INSERT INTO `indicator_categories` (`name`, `code`, `parent_id`, `level`, `sort_order`, `description`, `org_id`, `created_by`, `updated_by`) VALUES
('财务指标', 'FINANCIAL', NULL, 1, 1, '财务相关指标', NULL, 1, 1),
('客户指标', 'CUSTOMER', NULL, 1, 2, '客户相关指标', NULL, 1, 1),
('内部流程', 'INTERNAL_PROCESS', NULL, 1, 3, '内部流程相关指标', NULL, 1, 1),
('学习成长', 'LEARNING_GROWTH', NULL, 1, 4, '学习与成长相关指标', NULL, 1, 1),
('创新指标', 'INNOVATION', NULL, 1, 5, '创新相关指标', NULL, 1, 1);

-- 2.2 二级分类
INSERT INTO `indicator_categories` (`name`, `code`, `parent_id`, `level`, `sort_order`, `description`, `org_id`, `created_by`, `updated_by`) VALUES
('收入指标', 'FINANCIAL_REVENUE', 1, 2, 1, '收入相关指标', NULL, 1, 1),
('成本指标', 'FINANCIAL_COST', 1, 2, 2, '成本相关指标', NULL, 1, 1),
('利润指标', 'FINANCIAL_PROFIT', 1, 2, 3, '利润相关指标', NULL, 1, 1),
('满意度指标', 'CUSTOMER_SATISFACTION', 2, 2, 1, '客户满意度指标', NULL, 1, 1),
('留存率指标', 'CUSTOMER_RETENTION', 2, 2, 2, '客户留存率指标', NULL, 1, 1),
('效率指标', 'PROCESS_EFFICIENCY', 3, 2, 1, '流程效率指标', NULL, 1, 1),
('质量指标', 'PROCESS_QUALITY', 3, 2, 2, '流程质量指标', NULL, 1, 1),
('培训指标', 'LEARNING_TRAINING', 4, 2, 1, '培训相关指标', NULL, 1, 1),
('技能指标', 'LEARNING_SKILL', 4, 2, 2, '技能提升指标', NULL, 1, 1);

-- ============================================
-- 3. 指标测试数据
-- ============================================

-- 3.1 财务指标类
INSERT INTO `indicators` (`name`, `code`, `category_id`, `type`, `unit`, `description`, `calculation_method`, `data_source`, `target_type`, `default_weight`, `status`, `org_id`, `created_by`, `updated_by`) VALUES
('年度营业收入', 'FIN_REVENUE_ANNUAL', 6, 'QUANTITATIVE', '万元', '年度营业收入目标达成情况', '实际收入/目标收入*100', '财务系统', 'MINIMUM', 20.00, 'ACTIVE', NULL, 1, 1),
('季度营业收入', 'FIN_REVENUE_QUARTERLY', 6, 'QUANTITATIVE', '万元', '季度营业收入目标达成情况', '实际收入/目标收入*100', '财务系统', 'MINIMUM', 15.00, 'ACTIVE', NULL, 1, 1),
('成本控制率', 'FIN_COST_CONTROL', 7, 'QUANTITATIVE', '%', '成本控制达成情况', '（预算成本-实际成本）/预算成本*100', '财务系统', 'MAXIMUM', 10.00, 'ACTIVE', NULL, 1, 1),
('净利润率', 'FIN_PROFIT_MARGIN', 8, 'QUANTITATIVE', '%', '净利润率达成情况', '净利润/营业收入*100', '财务系统', 'MINIMUM', 15.00, 'ACTIVE', NULL, 1, 1);

-- 3.2 客户指标类
INSERT INTO `indicators` (`name`, `code`, `category_id`, `type`, `unit`, `description`, `calculation_method`, `data_source`, `target_type`, `default_weight`, `status`, `org_id`, `created_by`, `updated_by`) VALUES
('客户满意度', 'CUST_SATISFACTION', 9, 'QUANTITATIVE', '分', '客户满意度调查得分', '问卷调查平均分', 'CRM系统', 'MINIMUM', 10.00, 'ACTIVE', NULL, 1, 1),
('客户投诉率', 'CUST_COMPLAINT_RATE', 9, 'QUANTITATIVE', '%', '客户投诉占比', '投诉客户数/总客户数*100', '客服系统', 'MAXIMUM', 5.00, 'ACTIVE', NULL, 1, 1),
('客户留存率', 'CUST_RETENTION_RATE', 10, 'QUANTITATIVE', '%', '客户留存比例', '期末客户数/期初客户数*100', 'CRM系统', 'MINIMUM', 10.00, 'ACTIVE', NULL, 1, 1),
('新客户增长率', 'CUST_NEW_GROWTH', 10, 'QUANTITATIVE', '%', '新客户增长比例', '（新客户数-流失客户数）/总客户数*100', 'CRM系统', 'MINIMUM', 8.00, 'ACTIVE', NULL, 1, 1);

-- 3.3 内部流程类
INSERT INTO `indicators` (`name`, `code`, `category_id`, `type`, `unit`, `description`, `calculation_method`, `data_source`, `target_type`, `default_weight`, `status`, `org_id`, `created_by`, `updated_by`) VALUES
('项目按时完成率', 'PROC_PROJECT_ONTIME', 11, 'QUANTITATIVE', '%', '项目按时完成比例', '按时完成项目数/总项目数*100', '项目管理系统', 'MINIMUM', 12.00, 'ACTIVE', NULL, 1, 1),
('流程优化数量', 'PROC_OPTIMIZATION_COUNT', 11, 'QUANTITATIVE', '个', '流程优化改进数量', '统计期内完成的流程优化数量', '内部记录', 'MINIMUM', 5.00, 'ACTIVE', NULL, 1, 1),
('产品缺陷率', 'PROC_DEFECT_RATE', 12, 'QUANTITATIVE', '%', '产品缺陷比例', '缺陷数/总交付数*100', '质量管理系统', 'MAXIMUM', 8.00, 'ACTIVE', NULL, 1, 1),
('客户响应时间', 'PROC_RESPONSE_TIME', 12, 'QUANTITATIVE', '小时', '平均客户响应时间', '总响应时间/响应次数', '客服系统', 'MAXIMUM', 6.00, 'ACTIVE', NULL, 1, 1);

-- 3.4 学习成长类
INSERT INTO `indicators` (`name`, `code`, `category_id`, `type`, `unit`, `description`, `calculation_method`, `data_source`, `target_type`, `default_weight`, `status`, `org_id`, `created_by`, `updated_by`) VALUES
('培训完成率', 'LEARN_TRAINING_COMPLETE', 13, 'QUANTITATIVE', '%', '培训计划完成比例', '完成培训人数/应培训人数*100', '培训系统', 'MINIMUM', 8.00, 'ACTIVE', NULL, 1, 1),
('培训满意度', 'LEARN_TRAINING_SATISFACTION', 13, 'QUANTITATIVE', '分', '培训满意度评分', '培训反馈平均分', '培训系统', 'MINIMUM', 5.00, 'ACTIVE', NULL, 1, 1),
('技能认证通过率', 'LEARN_SKILL_CERT_PASS', 14, 'QUANTITATIVE', '%', '技能认证考试通过率', '通过人数/参考人数*100', '培训系统', 'MINIMUM', 10.00, 'ACTIVE', NULL, 1, 1),
('知识分享次数', 'LEARN_KNOWLEDGE_SHARE', 14, 'QUANTITATIVE', '次', '知识分享活动次数', '统计期内知识分享次数', '内部记录', 'MINIMUM', 4.00, 'ACTIVE', NULL, 1, 1);

-- 3.5 创新指标类
INSERT INTO `indicators` (`name`, `code`, `category_id`, `type`, `unit`, `description`, `calculation_method`, `data_source`, `target_type`, `default_weight`, `status`, `org_id`, `created_by`, `updated_by`) VALUES
('创新提案数量', 'INNO_PROPOSAL_COUNT', 5, 'QUANTITATIVE', '个', '创新提案提交数量', '统计期内提交的创新提案数量', '内部记录', 'MINIMUM', 6.00, 'ACTIVE', NULL, 1, 1),
('创新项目落地率', 'INNO_PROJECT_LANDING', 5, 'QUANTITATIVE', '%', '创新项目落地比例', '落地项目数/提案项目数*100', '项目管理系统', 'MINIMUM', 8.00, 'ACTIVE', NULL, 1, 1),
('专利数量', 'INNO_PATENT_COUNT', 5, 'QUANTITATIVE', '个', '申请/获得专利数量', '统计期内专利申请或获得数量', '知识产权系统', 'MINIMUM', 5.00, 'ACTIVE', NULL, 1, 1);

-- ============================================
-- 4. 权重方案测试数据
-- ============================================

-- 4.1 标准权重方案（已发布）
INSERT INTO `weight_schemes` (`name`, `code`, `cycle_id`, `org_id`, `version`, `status`, `description`, `total_weight`, `published_at`, `published_by`, `created_by`, `updated_by`) VALUES
('2026 Q2 标准权重方案', 'SCHEME_2026_Q2_STD', 1, NULL, 1, 'PUBLISHED', '2026年第二季度标准权重配置方案', 100.00, '2026-03-25 10:00:00', 1, 1, 1);

-- 4.2 研发部门专用方案（草稿）
INSERT INTO `weight_schemes` (`name`, `code`, `cycle_id`, `org_id`, `version`, `status`, `description`, `total_weight`, `published_at`, `published_by`, `created_by`, `updated_by`) VALUES
('研发部 Q2 权重方案', 'SCHEME_2026_Q2_RD', 1, 1, 1, 'DRAFT', '研发部门2026年Q2专用权重方案', 100.00, NULL, NULL, 1, 1);

-- 4.3 销售部门专用方案（草稿）
INSERT INTO `weight_schemes` (`name`, `code`, `cycle_id`, `org_id`, `version`, `status`, `description`, `total_weight`, `published_at`, `published_by`, `created_by`, `updated_by`) VALUES
('销售部 Q2 权重方案', 'SCHEME_2026_Q2_SALES', 1, 2, 1, 'DRAFT', '销售部门2026年Q2专用权重方案', 100.00, NULL, NULL, 1, 1);

-- ============================================
-- 5. 权重方案明细数据
-- ============================================

-- 5.1 标准方案明细（方案ID=1）
INSERT INTO `weight_scheme_items` (`scheme_id`, `indicator_id`, `weight`, `sort_order`) VALUES
(1, 1, 20.00, 0),  -- 年度营业收入
(1, 3, 15.00, 1),  -- 成本控制率
(1, 4, 15.00, 2),  -- 净利润率
(1, 5, 10.00, 3),  -- 客户满意度
(1, 6, 5.00, 4),   -- 客户投诉率
(1, 7, 10.00, 5),  -- 客户留存率
(1, 10, 8.00, 6),  -- 项目按时完成率
(1, 12, 5.00, 7),  -- 产品缺陷率
(1, 14, 5.00, 8),  -- 培训完成率
(1, 16, 7.00, 9);  -- 技能认证通过率

-- 5.2 研发部方案明细（方案ID=2）
INSERT INTO `weight_scheme_items` (`scheme_id`, `indicator_id`, `weight`, `sort_order`) VALUES
(2, 3, 10.00, 0),  -- 成本控制率
(2, 4, 10.00, 1),  -- 净利润率
(2, 10, 20.00, 2), -- 项目按时完成率
(2, 11, 10.00, 3), -- 流程优化数量
(2, 12, 15.00, 4), -- 产品缺陷率
(2, 14, 10.00, 5), -- 培训完成率
(2, 15, 5.00, 6),  -- 培训满意度
(2, 16, 10.00, 7), -- 技能认证通过率
(2, 17, 5.00, 8),  -- 知识分享次数
(2, 18, 5.00, 9);  -- 创新提案数量

-- 5.3 销售部方案明细（方案ID=3）
INSERT INTO `weight_scheme_items` (`scheme_id`, `indicator_id`, `weight`, `sort_order`) VALUES
(3, 1, 25.00, 0),  -- 年度营业收入
(3, 2, 15.00, 1),  -- 季度营业收入
(3, 5, 15.00, 2),  -- 客户满意度
(3, 6, 10.00, 3),  -- 客户投诉率
(3, 7, 15.00, 4),  -- 客户留存率
(3, 8, 10.00, 5),  -- 新客户增长率
(3, 13, 5.00, 6),  -- 客户响应时间
(3, 14, 5.00, 7);  -- 培训完成率
