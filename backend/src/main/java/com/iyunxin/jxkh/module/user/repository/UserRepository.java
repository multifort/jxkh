package com.iyunxin.jxkh.module.user.repository;

import com.iyunxin.jxkh.module.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据工号查找用户
     */
    Optional<User> findByEmployeeNo(String employeeNo);

    /**
     * 根据组织ID查找用户
     */
    List<User> findByOrgIdAndIsDeletedFalse(Long orgId);

    /**
     * 分页查询用户（支持搜索）
     */
    Page<User> findByIsDeletedFalse(Pageable pageable);

    /**
     * 根据姓名或用户名模糊搜索
     */
    Page<User> findByNameContainingOrUsernameContainingAndIsDeletedFalse(
            String name, String username, Pageable pageable);

    /**
     * 根据组织ID分页查询
     */
    Page<User> findByOrgIdAndIsDeletedFalse(Long orgId, Pageable pageable);

    /**
     * 根据角色查询
     */
    Page<User> findByRoleAndIsDeletedFalse(String role, Pageable pageable);
}
