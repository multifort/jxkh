package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.module.user.domain.Permission;
import com.iyunxin.jxkh.module.user.repository.PermissionRepository;
import com.iyunxin.jxkh.module.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PermissionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService 单元测试")
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setId(1L);
        testPermission.setCode("user:view");
        testPermission.setName("查看用户");
        testPermission.setType("MENU");
        testPermission.setIsDeleted(false);
    }

    @Test
    @DisplayName("获取用户权限代码 - 成功")
    void testGetUserPermissionCodes_Success() {
        // Given
        Long userId = 1L;
        List<Long> roleIds = Arrays.asList(1L, 2L);
        List<String> permissionCodes = Arrays.asList("user:view", "user:create");

        when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(roleIds);
        when(permissionRepository.findCodesByRoleIds(roleIds)).thenReturn(permissionCodes);

        // When
        Set<String> result = permissionService.getUserPermissionCodes(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("user:view"));
        assertTrue(result.contains("user:create"));
    }

    @Test
    @DisplayName("获取用户权限代码 - 用户无角色")
    void testGetUserPermissionCodes_NoRoles() {
        // Given
        Long userId = 1L;
        when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(List.of());

        // When
        Set<String> result = permissionService.getUserPermissionCodes(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("检查用户权限 - 有权限")
    void testHasPermission_True() {
        // Given
        Long userId = 1L;
        List<Long> roleIds = List.of(1L);
        List<String> permissionCodes = List.of("user:view");

        when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(roleIds);
        when(permissionRepository.findCodesByRoleIds(roleIds)).thenReturn(permissionCodes);

        // When
        boolean result = permissionService.hasPermission(userId, "user:view");

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查用户权限 - 无权限")
    void testHasPermission_False() {
        // Given
        Long userId = 1L;
        List<Long> roleIds = List.of(1L);
        List<String> permissionCodes = List.of("user:view");

        when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(roleIds);
        when(permissionRepository.findCodesByRoleIds(roleIds)).thenReturn(permissionCodes);

        // When
        boolean result = permissionService.hasPermission(userId, "user:delete");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("创建权限 - 成功")
    void testCreatePermission_Success() {
        // Given
        Permission newPermission = new Permission();
        newPermission.setCode("test:permission");
        newPermission.setName("测试权限");
        newPermission.setType("BUTTON");

        when(permissionRepository.existsByCode("test:permission")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(newPermission);

        // When
        Permission result = permissionService.createPermission(newPermission);

        // Then
        assertNotNull(result);
        assertEquals("test:permission", result.getCode());
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("创建权限 - 代码已存在")
    void testCreatePermission_CodeExists() {
        // Given
        Permission existingPermission = new Permission();
        existingPermission.setCode("existing:code");

        when(permissionRepository.existsByCode("existing:code")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> permissionService.createPermission(existingPermission));
        
        assertTrue(exception.getMessage().contains("权限代码已存在"));
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    @DisplayName("更新权限 - 成功")
    void testUpdatePermission_Success() {
        // Given
        Long permissionId = 1L;
        Permission updatedData = new Permission();
        updatedData.setCode("user:view");
        updatedData.setName("查看用户-更新");
        updatedData.setType("MENU");

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        // When
        Permission result = permissionService.updatePermission(permissionId, updatedData);

        // Then
        assertNotNull(result);
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("更新权限 - 不允许修改code")
    void testUpdatePermission_CannotChangeCode() {
        // Given
        Long permissionId = 1L;
        Permission updatedData = new Permission();
        updatedData.setCode("different:code");

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> permissionService.updatePermission(permissionId, updatedData));
        
        assertTrue(exception.getMessage().contains("不允许修改权限代码"));
    }

    @Test
    @DisplayName("删除权限 - 成功（逻辑删除）")
    void testDeletePermission_Success() {
        // Given
        Long permissionId = 1L;
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        // When
        permissionService.deletePermission(permissionId);

        // Then
        assertTrue(testPermission.getIsDeleted());
        verify(permissionRepository).save(testPermission);
    }

    @Test
    @DisplayName("获取权限详情 - 成功")
    void testGetPermissionById_Success() {
        // Given
        Long permissionId = 1L;
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));

        // When
        Permission result = permissionService.getPermissionById(permissionId);

        // Then
        assertNotNull(result);
        assertEquals("user:view", result.getCode());
    }

    @Test
    @DisplayName("获取权限详情 - 权限不存在")
    void testGetPermissionById_NotFound() {
        // Given
        Long permissionId = 999L;
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> permissionService.getPermissionById(permissionId));
        
        assertTrue(exception.getMessage().contains("权限不存在"));
    }

    @Test
    @DisplayName("获取所有活跃权限")
    void testGetAllActivePermissions() {
        // Given
        List<Permission> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findAllActive()).thenReturn(permissions);

        // When
        List<Permission> result = permissionService.getAllActivePermissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
