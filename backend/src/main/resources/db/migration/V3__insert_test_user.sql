-- 添加测试用户（密码: admin123，使用 BCrypt 加密）
INSERT INTO users (username, password, employee_no, name, email, phone, org_id, role, status, created_at, updated_at, is_deleted)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'EMP001', '系统管理员', 'admin@example.com', '13800138000', 1, 'ADMIN', 'ACTIVE', NOW(), NOW(), false)
ON DUPLICATE KEY UPDATE username = username;
