package com.iyunxin.jxkh.module.user.repository;

import com.iyunxin.jxkh.module.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色 Repository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    List<Role> findByEnabledTrueAndIsDeletedFalseOrderBySort();

    @Query("SELECT r FROM Role r WHERE r.id IN :roleIds AND r.enabled = true AND r.isDeleted = false")
    List<Role> findByIdsAndActive(@Param("roleIds") List<Long> roleIds);
}
