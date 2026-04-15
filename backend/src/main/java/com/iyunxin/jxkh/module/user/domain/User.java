package com.iyunxin.jxkh.module.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * 密码
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * 工号
     */
    @Column(name = "employee_no", unique = true, nullable = false, length = 50)
    private String employeeNo;

    /**
     * 姓名
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;

    /**
     * 组织ID
     */
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    /**
     * 职位ID
     */
    @Column(name = "position_id")
    private Long positionId;

    /**
     * 主管ID
     */
    @Column(name = "manager_id")
    private Long managerId;

    /**
     * 角色 (EMPLOYEE, MANAGER, HR, ADMIN)
     */
    @Column(nullable = false, length = 20)
    private String role = "EMPLOYEE";

    /**
     * 状态 (ACTIVE, INACTIVE, LOCKED)
     */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    /**
     * 头像
     */
    @Column(length = 255)
    private String avatar;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 登录失败次数
     */
    @Column(name = "login_fail_count", nullable = false)
    private Integer loginFailCount = 0;

    /**
     * 锁定时间
     */
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
