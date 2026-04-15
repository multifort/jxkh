package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.module.user.domain.Role;
import com.iyunxin.jxkh.module.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Role updateRole(Long roleId, Role role) {
        Role existing = getRoleById(roleId);
        
        existing.setName(role.getName());
        existing.setCode(role.getCode());
        existing.setDescription(role.getDescription());
        existing.setRoleType(role.getRoleType());
        existing.setSortOrder(role.getSortOrder());
        
        return roleRepository.save(existing);
    }

    /**
     * 删除角色（逻辑删除）
     */
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = getRoleById(roleId);
        role.setIsDeleted(true);
        roleRepository.save(role);
        log.info("角色已删除: {}", roleId);
    }
}

