package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.module.user.domain.Role;
import com.iyunxin.jxkh.module.user.domain.RolePermission;
import com.iyunxin.jxkh.module.user.repository.RolePermissionRepository;
import com.iyunxin.jxkh.module.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * 获取所有活跃角色
     */
    @Cacheable(value = "roles", key = "'active'")
    public List<Role> getAllActiveRoles() {
        return roleRepository.findByEnabledTrueAndIsDeletedFalseOrderBySort();
    }

    /**
     * 根据ID获取角色
     */
    @Cacheable(value = "roles", key = "#roleId")
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
    }

    /**
     * 创建角色
     */
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public Role createRole(Role role) {
        // 检查 code 是否已存在
        if (roleRepository.findByCode(role.getCode()).isPresent()) {
            throw new RuntimeException("角色代码已存在");
        }
        return roleRepository.save(role);
    }

    /**
     * 更新角色
     */
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public Role updateRole(Long roleId, Role role) {
        Role existing = getRoleById(roleId);
        
        existing.setName(role.getName());
        existing.setDescription(role.getDescription());
        existing.setSort(role.getSort());
        existing.setEnabled(role.getEnabled());
        
        return roleRepository.save(existing);
    }

    /**
     * 删除角色（逻辑删除）
     */
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)
    public void deleteRole(Long roleId) {
        Role role = getRoleById(roleId);
        role.setIsDeleted(true);
        roleRepository.save(role);
        log.info("角色已删除: {}", roleId);
    }

    /**
     * 分配角色权限
     */
    @Transactional
    @CacheEvict(value = {"roles", "userPermissions"}, allEntries = true)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = getRoleById(roleId);
        
        // 删除旧的权限关联
        rolePermissionRepository.deleteByRoleId(roleId);
        
        // 添加新的权限关联
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = RolePermission.builder()
                    .roleId(roleId)
                    .permissionId(permissionId)
                    .build();
            rolePermissionRepository.save(rolePermission);
        }
        
        log.info("角色 {} 的权限已更新为: {}", roleId, permissionIds);
    }

    /**
     * 获取角色的权限ID列表
     */
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId)
                .stream()
                .map(RolePermission::getPermissionId)
                .toList();
    }
}

