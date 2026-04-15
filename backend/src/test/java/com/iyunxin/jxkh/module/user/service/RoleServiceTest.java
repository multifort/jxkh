package com.iyunxin.jxkh.module.user.service;

import com.iyunxin.jxkh.module.user.domain.Role;
import com.iyunxin.jxkh.module.user.domain.RolePermission;
import com.iyunxin.jxkh.module.user.repository.RolePermissionRepository;
import com.iyunxin.jxkh.module.user.repository.RoleRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RoleService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService 单元测试")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setCode("ADMIN");
        testRole.setName("管理员");
        testRole.setDescription("系统管理员");
        testRole.setEnabled(true);
        testRole.setIsDeleted(false);
    }

    @Test
    @DisplayName("获取所有活跃角色")
    void testGetAllActiveRoles() {
        // Given
        List<Role> roles = Arrays.asList(testRole);
        when(roleRepository.findByEnabledTrueAndIsDeletedFalseOrderBySort()).thenReturn(roles);

        // When
        List<Role> result = roleService.getAllActiveRoles();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getCode());
    }

    @Test
    @DisplayName("获取角色详情 - 成功")
    void testGetRoleById_Success() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));

        // When
        Role result = roleService.getRoleById(roleId);

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.getCode());
    }

    @Test
    @DisplayName("获取角色详情 - 角色不存在")
    void testGetRoleById_NotFound() {
        // Given
        Long roleId = 999L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> roleService.getRoleById(roleId));
        
        assertTrue(exception.getMessage().contains("角色不存在"));
    }

    @Test
    @DisplayName("创建角色 - 成功")
    void testCreateRole_Success() {
        // Given
        Role newRole = new Role();
        newRole.setCode("TEST_ROLE");
        newRole.setName("测试角色");

        when(roleRepository.findByCode("TEST_ROLE")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);

        // When
        Role result = roleService.createRole(newRole);

        // Then
        assertNotNull(result);
        assertEquals("TEST_ROLE", result.getCode());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("创建角色 - 代码已存在")
    void testCreateRole_CodeExists() {
        // Given
        Role existingRole = new Role();
        existingRole.setCode("EXISTING");

        when(roleRepository.findByCode("EXISTING")).thenReturn(Optional.of(existingRole));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> roleService.createRole(existingRole));
        
        assertTrue(exception.getMessage().contains("角色代码已存在"));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("更新角色 - 成功")
    void testUpdateRole_Success() {
        // Given
        Long roleId = 1L;
        Role updatedData = new Role();
        updatedData.setName("更新后的名称");
        updatedData.setDescription("更新后的描述");
        updatedData.setSort(10);
        updatedData.setEnabled(false);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        Role result = roleService.updateRole(roleId, updatedData);

        // Then
        assertNotNull(result);
        verify(roleRepository).save(argThat(role -> 
            "更新后的名称".equals(role.getName()) &&
            "更新后的描述".equals(role.getDescription()) &&
            Integer.valueOf(10).equals(role.getSort()) &&
            Boolean.FALSE.equals(role.getEnabled())
        ));
    }

    @Test
    @DisplayName("删除角色 - 成功（逻辑删除）")
    void testDeleteRole_Success() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        roleService.deleteRole(roleId);

        // Then
        assertTrue(testRole.getIsDeleted());
        verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("分配角色权限 - 成功")
    void testAssignPermissions_Success() {
        // Given
        Long roleId = 1L;
        List<Long> permissionIds = Arrays.asList(1L, 2L, 3L);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        doNothing().when(rolePermissionRepository).deleteByRoleId(roleId);
        when(rolePermissionRepository.save(any(RolePermission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        roleService.assignPermissions(roleId, permissionIds);

        // Then
        verify(rolePermissionRepository).deleteByRoleId(roleId);
        verify(rolePermissionRepository, times(3)).save(any(RolePermission.class));
    }

    @Test
    @DisplayName("获取角色权限ID列表")
    void testGetRolePermissionIds() {
        // Given
        Long roleId = 1L;
        RolePermission rp1 = new RolePermission();
        rp1.setRoleId(roleId);
        rp1.setPermissionId(1L);
        
        RolePermission rp2 = new RolePermission();
        rp2.setRoleId(roleId);
        rp2.setPermissionId(2L);

        when(rolePermissionRepository.findByRoleId(roleId)).thenReturn(Arrays.asList(rp1, rp2));

        // When
        List<Long> result = roleService.getRolePermissionIds(roleId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }
}
