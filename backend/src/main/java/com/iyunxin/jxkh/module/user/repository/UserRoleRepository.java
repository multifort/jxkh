package com.iyunxin.jxkh.module.user.repository;

import com.iyunxin.jxkh.module.user.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联 Repository
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query("SELECT ur.roleId FROM UserRole ur WHERE ur.userId = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    List<UserRole> findByUserId(Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
