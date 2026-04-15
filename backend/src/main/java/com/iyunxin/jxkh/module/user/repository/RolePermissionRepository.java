package com.iyunxin.jxkh.module.user.repository;

import com.iyunxin.jxkh.module.user.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 角色权限关联 Repository
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    void deleteByRoleId(Long roleId);
}
