package com.iyunxin.jxkh.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     * @throws IllegalStateException 如果用户未登录
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("用户未登录");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        throw new IllegalStateException("无法获取用户ID");
    }

    /**
     * 安全地获取当前用户ID（未登录时返回 null）
     * 
     * @return 用户ID，未登录时返回 null
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * 检查当前用户是否已认证
     * 
     * @return true 如果已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
