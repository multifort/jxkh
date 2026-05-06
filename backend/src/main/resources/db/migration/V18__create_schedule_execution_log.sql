-- 创建定时任务执行历史表
CREATE TABLE IF NOT EXISTS `schedule_execution_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `status` VARCHAR(20) NOT NULL COMMENT '执行状态：SUCCESS/FAILED',
    `risk_count` INT DEFAULT 0 COMMENT '检测到的风险数量',
    `notification_count` INT DEFAULT 0 COMMENT '发送的通知数量',
    `error_message` TEXT COMMENT '错误信息',
    `duration_ms` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_task_name` (`task_name`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行历史表';
