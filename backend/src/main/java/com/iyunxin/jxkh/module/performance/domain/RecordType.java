package com.iyunxin.jxkh.module.performance.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 绩效记录类型枚举
 */
@Getter
@AllArgsConstructor
public enum RecordType {
    /**
     * 周报
     */
    WEEKLY_REPORT("WEEKLY_REPORT", "周报"),
    
    /**
     * 月报
     */
    MONTHLY_REPORT("MONTHLY_REPORT", "月报"),
    
    /**
     * 里程碑
     */
    MILESTONE("MILESTONE", "里程碑"),
    
    /**
     * 成果
     */
    ACHIEVEMENT("ACHIEVEMENT", "成果");
    
    private final String code;
    private final String description;
    
    public static RecordType fromCode(String code) {
        for (RecordType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown RecordType code: " + code);
    }
}
