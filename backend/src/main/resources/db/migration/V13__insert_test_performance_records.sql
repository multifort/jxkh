-- Sprint 4: 插入绩效记录测试数据

-- 为计划 ID=1 插入周报测试数据
INSERT INTO `performance_records` (`plan_id`, `user_id`, `type`, `content`, `progress`, `record_date`, `created_at`, `updated_at`, `is_deleted`) VALUES
(1, 2, 'WEEKLY_REPORT', '<h3>本周工作总结</h3><p>1. 完成了用户管理模块的开发</p><p>2. 修复了权限控制的 Bug</p><p>3. 编写了单元测试，覆盖率达到 85%</p><p>4. 参与了代码审查，提出了 3 条改进建议</p>', 75.00, '2026-04-20', NOW(), NOW(), 0),
(1, 2, 'WEEKLY_REPORT', '<h3>本周工作总结</h3><p>1. 优化了数据库查询性能</p><p>2. 重构了 Service 层代码</p><p>3. 学习了新的技术栈</p>', 80.00, '2026-04-13', NOW(), NOW(), 0);

-- 为计划 ID=1 插入里程碑测试数据
INSERT INTO `performance_records` (`plan_id`, `user_id`, `type`, `content`, `progress`, `record_date`, `created_at`, `updated_at`, `is_deleted`) VALUES
(1, 2, 'MILESTONE', '<h3>里程碑：Sprint 3 完成</h3><p>已完成绩效计划的创建、审批流程和状态机功能。</p><p>关键成果：</p><ul><li>实现了完整的 CRUD 操作</li><li>支持权重校验和自动计算</li><li>添加了乐观锁并发控制</li></ul>', 100.00, '2026-04-22', NOW(), NOW(), 0);

-- 为计划 ID=2 插入月报测试数据
INSERT INTO `performance_records` (`plan_id`, `user_id`, `type`, `content`, `progress`, `record_date`, `created_at`, `updated_at`, `is_deleted`) VALUES
(2, 3, 'MONTHLY_REPORT', '<h3>4月工作总结</h3><p>本月主要工作：</p><ol><li>完成了前端页面的开发</li><li>实现了富文本编辑器集成</li><li>接入了 MinIO 文件存储</li><li>完成了 AI 智能总结功能</li></ol><p>下月计划：</p><ol><li>优化用户体验</li><li>增加更多测试用例</li><li>完善文档</li></ol>', 60.00, '2026-04-30', NOW(), NOW(), 0);

-- 为计划 ID=2 插入成果测试数据
INSERT INTO `performance_records` (`plan_id`, `user_id`, `type`, `content`, `progress`, `record_date`, `created_at`, `updated_at`, `is_deleted`) VALUES
(2, 3, 'ACHIEVEMENT', '<h3>技术突破</h3><p>成功解决了前后端联调中的 CORS 问题，并建立了标准化的 API 响应格式。</p><p>影响：提升了开发效率 30%，减少了沟通成本。</p>', NULL, '2026-04-25', NOW(), NOW(), 0);
