package com.iyunxin.jxkh.module.notification.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.common.util.SecurityUtils;
import com.iyunxin.jxkh.module.notification.domain.Notification;
import com.iyunxin.jxkh.module.notification.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "用户通知的查询和管理")
public class NotificationController {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * 查询用户的通知列表
     * Admin 用户可以查看所有通知，普通用户只能查看自己的通知
     */
    @GetMapping
    @Operation(summary = "查询通知列表", description = "分页查询通知列表（Admin 可查看所有）")
    public ApiResponse<Page<Notification>> getNotifications(
            @Parameter(description = "页码（从 0 开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
            
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
        // 检查是否为 Admin 用户
        if (isAdminUser()) {
            // Admin 用户：查看所有通知
            log.info("Admin 用户 {} 查询所有通知", userId);
            Page<Notification> notifications = notificationRepository.findByIsDeletedFalse(pageable);
            return ApiResponse.success(notifications);
        } else {
            // 普通用户：只能查看自己的通知
            Page<Notification> notifications = notificationRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
            return ApiResponse.success(notifications);
        }
    }
    
    /**
     * 查询未读通知数量
     * Admin 用户查看所有未读通知，普通用户只查看自己的
     */
    @GetMapping("/unread-count")
    @Operation(summary = "查询未读数量", description = "查询未读通知数量（Admin 可查看所有）")
    public ApiResponse<Long> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 检查是否为 Admin 用户
        if (isAdminUser()) {
            // Admin 用户：统计所有未读通知
            log.info("Admin 用户 {} 查询所有未读通知数量", userId);
            long count = notificationRepository.countByIsReadFalseAndIsDeletedFalse();
            return ApiResponse.success(count);
        } else {
            // 普通用户：只统计自己的未读通知
            long count = notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(userId);
            return ApiResponse.success(count);
        }
    }
    
    /**
     * 标记通知为已读
     * Admin 用户可以标记任何通知为已读，普通用户只能标记自己的
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将指定通知标记为已读")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        // Admin 用户可以操作任何通知，普通用户只能操作自己的
        if (!isAdminUser() && !notification.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此通知");
        }
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        return ApiResponse.success();
    }
    
    /**
     * 批量标记已读
     * Admin 用户标记所有未读通知，普通用户只标记自己的
     */
    @PutMapping("/mark-all-read")
    @Operation(summary = "全部标记已读", description = "将所有未读通知标记为已读")
    public ApiResponse<Void> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        List<Notification> unreadNotifications;
        
        // Admin 用户标记所有未读通知，普通用户只标记自己的
        if (isAdminUser()) {
            log.info("Admin 用户 {} 批量标记所有未读通知", userId);
            unreadNotifications = notificationRepository.findByIsReadFalseAndIsDeletedFalse();
        } else {
            unreadNotifications = notificationRepository
                    .findByUserIdAndIsReadFalseAndIsDeletedFalse(userId);
        }
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        
        notificationRepository.saveAll(unreadNotifications);
        
        return ApiResponse.success();
    }
    
    /**
     * 检查当前用户是否为 Admin
     */
    private boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}
