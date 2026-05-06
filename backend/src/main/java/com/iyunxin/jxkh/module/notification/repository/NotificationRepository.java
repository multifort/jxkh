package com.iyunxin.jxkh.module.notification.repository;

import com.iyunxin.jxkh.module.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    
    /**
     * 删除指定时间前的已读通知
     * @param cutoffDate 截止时间
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffDate AND n.isDeleted = false")
    int deleteByIsReadTrueAndCreatedAtBefore(LocalDateTime cutoffDate);
}
