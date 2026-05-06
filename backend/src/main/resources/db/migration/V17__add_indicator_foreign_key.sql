-- 为 indicators 表添加外键约束
-- 确保 category_id 引用存在的 indicator_categories

-- 先清理孤儿记录（category_id 不存在的记录）
DELETE FROM indicators 
WHERE category_id NOT IN (SELECT id FROM indicator_categories);

-- 添加外键约束
ALTER TABLE `indicators`
ADD CONSTRAINT `fk_indicators_category`
FOREIGN KEY (`category_id`) REFERENCES `indicator_categories`(`id`)
ON DELETE RESTRICT
ON UPDATE CASCADE;
