package com.iyunxin.jxkh.module.performance.domain;

/**
 * 绩效等级枚举
 */
public enum PerformanceLevel {
    A("A", "优秀", 90, 100),
    B("B", "良好", 80, 89),
    C("C", "合格", 70, 79),
    D("D", "待改进", 0, 69);

    private final String code;
    private final String description;
    private final int minScore;
    private final int maxScore;

    PerformanceLevel(String code, String description, int minScore, int maxScore) {
        this.code = code;
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    /**
     * 根据分数获取等级
     */
    public static PerformanceLevel fromScore(double score) {
        if (score >= 90) return A;
        if (score >= 80) return B;
        if (score >= 70) return C;
        return D;
    }
}
