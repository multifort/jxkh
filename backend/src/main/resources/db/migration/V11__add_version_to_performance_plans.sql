-- 为 performance_plans 表添加 version 字段（乐观锁）
ALTER TABLE performance_plans 
ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）';
