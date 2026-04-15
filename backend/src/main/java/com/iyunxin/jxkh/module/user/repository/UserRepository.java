package com.iyunxin.jxkh.module.user.repository;

import com.iyunxin.jxkh.module.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
