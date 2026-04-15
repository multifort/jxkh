package com.iyunxin.jxkh.module.user.repository;

import com.iyunxin.jxkh.module.user.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 权限 Repository
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("SELECT p FROM Permission p WHERE p.isDeleted = false")
    List<Permission> findAllActive();

    @Query("SELECT p FROM Permission p JOIN RolePermission rp ON p.id = rp.permissionId " +
           "WHERE rp.roleId IN :roleIds AND p.isDeleted = false")
    List<Permission> findByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Query("SELECT p.code FROM Permission p JOIN RolePermission rp ON p.id = rp.permissionId " +
           "WHERE rp.roleId IN :roleIds AND p.isDeleted = false")
    List<String> findCodesByRoleIds(@Param("roleIds") List<Long> roleIds);
}
