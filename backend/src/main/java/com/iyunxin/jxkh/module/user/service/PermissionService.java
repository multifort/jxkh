package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.module.user.domain.Permission;
import com.iyunxin.jxkh.module.user.repository.PermissionRepository;
import com.iyunxin.jxkh.module.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 创建权限
     */
    @Transactional
    @CacheEvict(value = "permissions", key = "'all'")
    public Permission createPermission(Permission permission) {
        // 校验 code 唯一性
        if (permissionRepository.existsByCode(permission.getCode())) {
            throw new RuntimeException("权限代码已存在: " + permission.getCode());
        }
        
        permission.setIsDeleted(false);
        Permission saved = permissionRepository.save(permission);
        log.info("创建权限成功: {} ({})", saved.getName(), saved.getCode());
        return saved;
    }

    /**
     * 更新权限
     */
    @Transactional
    @CacheEvict(value = {"permissions", "userPermissions"}, allEntries = true)
    public Permission updatePermission(Long id, Permission permission) {
        Permission existing = getPermissionById(id);
        
        // 不允许修改 code（保持唯一性）
        if (!existing.getCode().equals(permission.getCode())) {
            throw new RuntimeException("不允许修改权限代码");
        }
        
        existing.setName(permission.getName());
        existing.setType(permission.getType());
        existing.setResource(permission.getResource());
        existing.setParentId(permission.getParentId());
        existing.setSort(permission.getSort());
        existing.setIcon(permission.getIcon());
        existing.setPath(permission.getPath());
        
        Permission updated = permissionRepository.save(existing);
        log.info("更新权限成功: {} ({})", updated.getName(), updated.getCode());
        return updated;
    }

    /**
     * 删除权限（逻辑删除）
     */
    @Transactional
    @CacheEvict(value = {"permissions", "userPermissions"}, allEntries = true)
    public void deletePermission(Long id) {
        Permission permission = getPermissionById(id);
        permission.setIsDeleted(true);
        permissionRepository.save(permission);
        log.info("删除权限成功: {} ({})", permission.getName(), permission.getCode());
    }

    /**
     * 获取权限详情
     */
    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("权限不存在: " + id));
    }
}
