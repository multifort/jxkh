-- 更新 orgs 表，添加 org_type 字段
ALTER TABLE `orgs` 
ADD COLUMN `org_type` VARCHAR(50) DEFAULT NULL COMMENT '组织类型' AFTER `code`;
