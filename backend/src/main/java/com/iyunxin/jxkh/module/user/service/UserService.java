package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     */
    public Page<User> getUsers(int page, int size, String keyword, Long orgId, String role) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.findByNameContainingOrUsernameContainingAndIsDeletedFalse(
                    keyword, keyword, pageable);
        } else if (orgId != null) {
            return userRepository.findByOrgIdAndIsDeletedFalse(orgId, pageable);
        } else if (role != null && !role.trim().isEmpty()) {
            return userRepository.findByRoleAndIsDeletedFalse(role, pageable);
        } else {
            return userRepository.findByIsDeletedFalse(pageable);
        }
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getIsDeleted()) {
            throw new RuntimeException("用户已删除");
        }
        return user;
    }

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查工号是否已存在
        if (userRepository.findByEmployeeNo(user.getEmployeeNo()).isPresent()) {
            throw new RuntimeException("工号已存在");
        }
        
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 默认密码
            user.setPassword(passwordEncoder.encode("123456"));
        }
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        if (user.getRole() == null) {
            user.setRole("EMPLOYEE");
        }
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(false);
        }
        
        return userRepository.save(user);
    }

    /**
     * 更新用户
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User existing = getUserById(id);
        
        // 不允许修改用户名和工号
        if (!existing.getUsername().equals(userDetails.getUsername())) {
            throw new RuntimeException("不允许修改用户名");
        }
        if (!existing.getEmployeeNo().equals(userDetails.getEmployeeNo())) {
            throw new RuntimeException("不允许修改工号");
        }
        
        existing.setName(userDetails.getName());
        existing.setEmail(userDetails.getEmail());
        existing.setPhone(userDetails.getPhone());
        existing.setOrgId(userDetails.getOrgId());
        existing.setPositionId(userDetails.getPositionId());
        existing.setManagerId(userDetails.getManagerId());
        existing.setRole(userDetails.getRole());
        existing.setStatus(userDetails.getStatus());
        existing.setAvatar(userDetails.getAvatar());
        
        return userRepository.save(existing);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setIsDeleted(true);
        userRepository.save(user);
        log.info("用户已删除: {}", id);
    }

    /**
     * 启用/禁用用户
     */
    @Transactional
    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        
        if ("ACTIVE".equals(user.getStatus())) {
            user.setStatus("INACTIVE");
        } else if ("INACTIVE".equals(user.getStatus())) {
            user.setStatus("ACTIVE");
        } else {
            throw new RuntimeException("当前状态不允许切换");
        }
        
        return userRepository.save(user);
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("用户密码已重置: {}", id);
    }

    /**
     * 解锁用户
     */
    @Transactional
    public User unlockUser(Long id) {
        User user = getUserById(id);
        user.setStatus("ACTIVE");
        user.setLoginFailCount(0);
        user.setLockedAt(null);
        return userRepository.save(user);
    }
}
