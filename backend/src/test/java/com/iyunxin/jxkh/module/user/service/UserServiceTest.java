package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.common.util.SecurityUtils;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import com.iyunxin.jxkh.module.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 覆盖10个核心场景
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DataPermissionService dataPermissionService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmployeeNo("E001");
        testUser.setName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setPassword("encodedPassword");
        testUser.setRole("EMPLOYEE");
        testUser.setStatus("ACTIVE");
        testUser.setIsDeleted(false);
        testUser.setOrgId(1L);
    }

    /**
     * 测试场景1: 分页查询用户列表 - 无筛选条件
     */
    @Test
    @DisplayName("分页查询用户列表 - 无筛选条件")
    void testGetUsers_NoFilter() {
        // Given
        Page<User> mockPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findByIsDeletedFalse(any())).thenReturn(mockPage);
        when(dataPermissionService.filterAccessibleUsers(anyLong(), anyList())).thenReturn(List.of(testUser));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            // When
            Page<User> result = userService.getUsers(0, 10, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(userRepository).findByIsDeletedFalse(any());
        }
    }

    /**
     * 测试场景2: 根据关键词搜索用户
     */
    @Test
    @DisplayName("根据关键词搜索用户")
    void testGetUsers_WithKeyword() {
        // Given
        String keyword = "测试";
        Page<User> mockPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findByNameContainingOrUsernameContainingAndIsDeletedFalse(
                anyString(), anyString(), any())).thenReturn(mockPage);
        when(dataPermissionService.filterAccessibleUsers(anyLong(), anyList())).thenReturn(List.of(testUser));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            // When
            Page<User> result = userService.getUsers(0, 10, keyword, null, null);

            // Then
            assertNotNull(result);
            verify(userRepository).findByNameContainingOrUsernameContainingAndIsDeletedFalse(
                    eq(keyword), eq(keyword), any());
        }
    }

    /**
     * 测试场景3: 根据组织ID查询用户
     */
    @Test
    @DisplayName("根据组织ID查询用户")
    void testGetUsers_ByOrgId() {
        // Given
        Long orgId = 1L;
        Page<User> mockPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findByOrgIdAndIsDeletedFalse(eq(orgId), any())).thenReturn(mockPage);
        when(dataPermissionService.getSafeOrgId(anyLong(), eq(orgId))).thenReturn(orgId);
        when(dataPermissionService.filterAccessibleUsers(anyLong(), anyList())).thenReturn(List.of(testUser));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            // When
            Page<User> result = userService.getUsers(0, 10, null, orgId, null);

            // Then
            assertNotNull(result);
            verify(dataPermissionService).getSafeOrgId(anyLong(), eq(orgId));
        }
    }

    /**
     * 测试场景4: 根据角色查询用户
     */
    @Test
    @DisplayName("根据角色查询用户")
    void testGetUsers_ByRole() {
        // Given
        String role = "ADMIN";
        Page<User> mockPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findByRoleAndIsDeletedFalse(eq(role), any())).thenReturn(mockPage);
        when(dataPermissionService.filterAccessibleUsers(anyLong(), anyList())).thenReturn(List.of(testUser));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            // When
            Page<User> result = userService.getUsers(0, 10, null, null, role);

            // Then
            assertNotNull(result);
            verify(userRepository).findByRoleAndIsDeletedFalse(eq(role), any());
        }
    }

    /**
     * 测试场景5: 根据ID获取用户 - 成功
     */
    @Test
    @DisplayName("根据ID获取用户 - 成功")
    void testGetUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getName());
    }

    /**
     * 测试场景6: 根据ID获取用户 - 用户不存在
     */
    @Test
    @DisplayName("根据ID获取用户 - 用户不存在")
    void testGetUserById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(999L);
        });
        assertEquals("用户不存在", exception.getMessage());
    }

    /**
     * 测试场景7: 创建用户 - 成功
     */
    @Test
    @DisplayName("创建用户 - 成功")
    void testCreateUser_Success() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmployeeNo("E002");
        newUser.setName("新用户");
        newUser.setPassword("123456");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeNo("E002")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        User result = userService.createUser(newUser);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals("EMPLOYEE", result.getRole());
        verify(userRepository).save(any(User.class));
    }

    /**
     * 测试场景8: 创建用户 - 用户名已存在
     */
    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void testCreateUser_UsernameExists() {
        // Given
        User newUser = new User();
        newUser.setUsername("existing");
        newUser.setEmployeeNo("E003");
        newUser.setName("重复用户");

        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(newUser);
        });
        assertEquals("用户名已存在", exception.getMessage());
    }

    /**
     * 测试场景9: 创建用户 - 工号已存在
     */
    @Test
    @DisplayName("创建用户 - 工号已存在")
    void testCreateUser_EmployeeNoExists() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser2");
        newUser.setEmployeeNo("E001");
        newUser.setName("重复工号");

        when(userRepository.findByUsername("newuser2")).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeNo("E001")).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(newUser);
        });
        assertEquals("工号已存在", exception.getMessage());
    }

    /**
     * 测试场景10: 更新用户 - 成功
     */
    @Test
    @DisplayName("更新用户 - 成功")
    void testUpdateUser_Success() {
        // Given
        User userDetails = new User();
        userDetails.setUsername("testuser");
        userDetails.setEmployeeNo("E001");
        userDetails.setName("更新后的名字");
        userDetails.setEmail("updated@example.com");
        userDetails.setPhone("13900139000");
        userDetails.setRole("MANAGER");
        userDetails.setStatus("ACTIVE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(1L, userDetails);

        // Then
        assertNotNull(result);
        assertEquals("更新后的名字", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("MANAGER", result.getRole());
        verify(userRepository).save(any(User.class));
    }

    /**
     * 测试场景11: 更新用户 - 不允许修改用户名
     */
    @Test
    @DisplayName("更新用户 - 不允许修改用户名")
    void testUpdateUser_CannotChangeUsername() {
        // Given
        User userDetails = new User();
        userDetails.setUsername("different_username");
        userDetails.setEmployeeNo("E001");
        userDetails.setName("测试");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, userDetails);
        });
        assertEquals("不允许修改用户名", exception.getMessage());
    }

    /**
     * 测试场景12: 删除用户（逻辑删除）
     */
    @Test
    @DisplayName("删除用户（逻辑删除）")
    void testDeleteUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).save(argThat(user -> user.getIsDeleted()));
    }

    /**
     * 测试场景13: 切换用户状态 - ACTIVE -> INACTIVE
     */
    @Test
    @DisplayName("切换用户状态 - ACTIVE -> INACTIVE")
    void testToggleUserStatus_ActiveToInactive() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.toggleUserStatus(1L);

        // Then
        assertEquals("INACTIVE", result.getStatus());
        verify(userRepository).save(any(User.class));
    }

    /**
     * 测试场景14: 重置密码
     */
    @Test
    @DisplayName("重置密码")
    void testResetPassword_Success() {
        // Given
        String newPassword = "newPassword123";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.resetPassword(1L, newPassword);

        // Then
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(argThat(user -> "encodedNewPassword".equals(user.getPassword())));
    }

    /**
     * 测试场景15: 解锁用户
     */
    @Test
    @DisplayName("解锁用户")
    void testUnlockUser_Success() {
        // Given
        User lockedUser = new User();
        lockedUser.setId(1L);
        lockedUser.setUsername("testuser");
        lockedUser.setEmployeeNo("E001");
        lockedUser.setName("测试用户");
        lockedUser.setStatus("LOCKED");
        lockedUser.setLoginFailCount(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(lockedUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.unlockUser(1L);

        // Then
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(0, result.getLoginFailCount());
        assertNull(result.getLockedAt());
        verify(userRepository).save(any(User.class));
    }
}
