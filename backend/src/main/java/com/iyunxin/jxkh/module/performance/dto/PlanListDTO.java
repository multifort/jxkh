package com.iyunxin.jxkh.module.performance.dto;

import com.iyunxin.jxkh.module.performance.domain.PerformanceLevel;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 计划列表 DTO（包含关联查询的员工姓名和周期名称）
 */
@Data
public class PlanListDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 计划ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long userId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 周期ID
     */
    private Long cycleId;

    /**
     * 周期名称
     */
    private String cycleName;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 状态
     */
    private PlanStatus status;

    /**
     * 总分
     */
    private BigDecimal totalScore;

    /**
     * 最终等级
     */
    private PerformanceLevel finalLevel;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 评估时间
     */
    private LocalDateTime evaluatedAt;

    /**
     * 校准时间
     */
    private LocalDateTime calibratedAt;

    /**
     * 归档时间
     */
    private LocalDateTime archivedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 指标实例列表
     */
    private List<PlanIndicatorDTO> indicators;

    /**
     * 自评进度（已评分指标数/总指标数）
     */
    private Integer selfScoredCount;

    /**
     * 上级评进度（已评分指标数/总指标数）
     */
    private Integer managerScoredCount;

    /**
     * 总指标数
     */
    private Integer totalIndicators;

    /**
     * 指标实例 DTO
     */
    @Data
    public static class PlanIndicatorDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private Long indicatorId;
        private String name;
        private String type;
        private BigDecimal weight;
        private BigDecimal targetValue;
        private BigDecimal currentValue;
        private BigDecimal progress;
        private String unit;
        private String remark;
    }
}
