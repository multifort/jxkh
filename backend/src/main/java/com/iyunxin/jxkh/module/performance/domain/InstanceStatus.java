package com.iyunxin.jxkh.module.performance.domain;

/**
 * 指标实例状态枚举
 */
public enum InstanceStatus {
    NOT_STARTED("NOT_STARTED", "未开始"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    DELAYED("DELAYED", "延期");

    private final String code;
    private final String description;

    InstanceStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
