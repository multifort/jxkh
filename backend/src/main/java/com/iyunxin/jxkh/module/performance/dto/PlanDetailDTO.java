package com.iyunxin.jxkh.module.performance.dto;

import com.iyunxin.jxkh.module.performance.domain.PerformanceLevel;
import com.iyunxin.jxkh.module.performance.domain.PlanStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 计划详情 DTO（包含完整的关联信息）
 */
@Data
public class PlanDetailDTO implements Serializable {

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
     * 组织名称
     */
    private String orgName;

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
     * 评估人ID
     */
    private Long evaluatorId;

    /**
     * 评估人姓名
     */
    private String evaluatorName;

    /**
     * 评语
     */
    private String comment;

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
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 指标实例列表
     */
    private List<PlanIndicatorDetailDTO> indicators;

    /**
     * 指标实例详情 DTO
     */
    @Data
    public static class PlanIndicatorDetailDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private Long indicatorId;
        private Long ownerId;
        private String ownerName;
        private String name;
        private String type;
        private BigDecimal weight;
        private BigDecimal targetValue;
        private BigDecimal currentValue;
        private BigDecimal progress;
        private String status;
        private String unit;
        private String remark;
        private BigDecimal score;
    }
}
