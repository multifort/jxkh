package com.iyunxin.jxkh.module.notification.repository;

import com.iyunxin.jxkh.module.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知 Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 查询用户的未读通知
     */
    List<Notification> findByUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);
    
    /**
     * 分页查询用户的通知
     */
    Page<Notification> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    
    /**
     * 统计用户未读通知数量
     */
    long countByUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);
    
    /**
     * 分页查询所有通知（Admin 使用）
     */
    Page<Notification> findByIsDeletedFalse(Pageable pageable);
    
    /**
     * 查询所有未读通知（Admin 使用）
     */
    List<Notification> findByIsReadFalseAndIsDeletedFalse();
    
    /**
     * 统计所有未读通知数量（Admin 使用）
     */
    long countByIsReadFalseAndIsDeletedFalse();
}
