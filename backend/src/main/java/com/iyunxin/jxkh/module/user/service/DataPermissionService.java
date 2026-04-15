package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限服务
 * 实现基于组织的数据隔离
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataPermissionService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    /**
     * 检查用户是否有权限访问指定组织的数据
     * 
     * @param currentUserId 当前用户ID
     * @param targetOrgId 目标组织ID
     * @return 是否有权限
     */
    public boolean canAccessOrg(Long currentUserId, Long targetOrgId) {
        // 1. 检查是否有全局查看权限
        if (permissionService.hasPermission(currentUserId, "org:view:all")) {
            return true;
        }
        
        // 2. 获取当前用户信息
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 3. 只能查看自己所在组织的数据
        if (currentUser.getOrgId() != null && currentUser.getOrgId().equals(targetOrgId)) {
            return true;
        }
        
        // 4. 检查是否有跨组织查看权限
        if (permissionService.hasPermission(currentUserId, "org:view:cross")) {
            return true;
        }
        
        log.warn("用户 {} 无权访问组织 {} 的数据", currentUserId, targetOrgId);
        return false;
    }

    /**
     * 过滤用户列表，只返回当前用户有权限查看的用户
     * 
     * @param currentUserId 当前用户ID
     * @param allUsers 所有用户列表
     * @return 过滤后的用户列表
     */
    public List<User> filterAccessibleUsers(Long currentUserId, List<User> allUsers) {
        // 检查是否有全局查看权限
        if (permissionService.hasPermission(currentUserId, "user:view:all")) {
            return allUsers;
        }
        
        // 获取当前用户信息
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 如果没有组织ID，返回空列表
        if (currentUser.getOrgId() == null) {
            log.warn("用户 {} 没有所属组织，无法查看任何用户", currentUserId);
            return List.of();
        }
        
        // 只返回同一组织的用户
        Long userOrgId = currentUser.getOrgId();
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> user.getOrgId() != null && user.getOrgId().equals(userOrgId))
                .collect(Collectors.toList());
        
        log.debug("用户 {} 可查看 {} 个用户（共 {} 个）", currentUserId, filteredUsers.size(), allUsers.size());
        return filteredUsers;
    }

    /**
     * 获取当前用户可访问的组织ID列表
     * 
     * @param currentUserId 当前用户ID
     * @return 可访问的组织ID集合
     */
    public Set<Long> getAccessibleOrgIds(Long currentUserId) {
        // 检查是否有全局查看权限
        if (permissionService.hasPermission(currentUserId, "org:view:all")) {
            // 返回所有组织ID（需要注入 OrgRepository）
            return Set.of(); // TODO: 实现获取所有组织ID
        }
        
        // 获取当前用户信息
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 默认只能访问自己所在的组织
        if (currentUser.getOrgId() != null) {
            return Set.of(currentUser.getOrgId());
        }
        
        return Set.of();
    }

    /**
     * 验证并获取安全的组织ID
     * 如果用户无权访问指定的组织ID，则返回其所在组织ID
     * 
     * @param currentUserId 当前用户ID
     * @param requestedOrgId 请求的组织ID
     * @return 安全的组织ID
     */
    public Long getSafeOrgId(Long currentUserId, Long requestedOrgId) {
        if (requestedOrgId == null) {
            // 如果没有指定组织ID，使用当前用户的组织ID
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            return currentUser.getOrgId();
        }
        
        // 检查是否有权限访问指定的组织
        if (!canAccessOrg(currentUserId, requestedOrgId)) {
            log.warn("用户 {} 无权访问组织 {}，拒绝请求", currentUserId, requestedOrgId);
            throw new RuntimeException("无权访问该组织的数据");
        }
        
        return requestedOrgId;
    }
}
