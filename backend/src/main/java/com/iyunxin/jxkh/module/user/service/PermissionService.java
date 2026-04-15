package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.module.user.domain.Permission;
import com.iyunxin.jxkh.module.user.repository.PermissionRepository;
import com.iyunxin.jxkh.module.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * 获取用户的所有权限代码
     */
    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<String> getUserPermissionCodes(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return permissionRepository.findCodesByRoleIds(roleIds)
                .stream()
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户的所有权限对象
     */
    @Cacheable(value = "userPermissions", key = "'objects_' + #userId")
    public List<Permission> getUserPermissions(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return permissionRepository.findByRoleIds(roleIds);
    }

    /**
     * 获取所有活跃权限
     */
    @Cacheable(value = "permissions", key = "'all'")
    public List<Permission> getAllActivePermissions() {
        return permissionRepository.findAllActive();
    }

    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        Set<String> permissions = getUserPermissionCodes(userId);
        return permissions.contains(permissionCode);
    }

    /**
     * 检查用户是否有任一权限
     */
    public boolean hasAnyPermission(Long userId, String... permissionCodes) {
        Set<String> permissions = getUserPermissionCodes(userId);
        for (String code : permissionCodes) {
            if (permissions.contains(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查用户是否有所有权限
     */
    public boolean hasAllPermissions(Long userId, String... permissionCodes) {
        Set<String> permissions = getUserPermissionCodes(userId);
        for (String code : permissionCodes) {
            if (!permissions.contains(code)) {
                return false;
            }
        }
        return true;
    }
}
