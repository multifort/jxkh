package com.iyunxin.jxkh.module.performance.domain;

/**
 * 绩效计划状态枚举
 */
public enum PlanStatus {
    DRAFT("DRAFT", "草稿"),
    PENDING_SUBMIT("PENDING_SUBMIT", "待提交"),
    PENDING_APPROVE("PENDING_APPROVE", "待审批"),
    IN_PROGRESS("IN_PROGRESS", "执行中"),
    PENDING_EVAL("PENDING_EVAL", "待评估"),
    EVALUATED("EVALUATED", "已评估"),
    CALIBRATED("CALIBRATED", "已校准"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String description;

    PlanStatus(String code, String description) {
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
