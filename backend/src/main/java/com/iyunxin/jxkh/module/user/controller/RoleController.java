package com.iyunxin.jxkh.module.user.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.user.domain.Role;
import com.iyunxin.jxkh.module.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色CRUD操作")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取所有活跃角色")
    @GetMapping
    public ApiResponse<List<Role>> getAllRoles() {
        return ApiResponse.success(roleService.getAllActiveRoles());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public ApiResponse<Role> getRoleById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> createRole(@RequestBody Role role) {
        return ApiResponse.success(roleService.createRole(role));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return ApiResponse.success(roleService.updateRole(id, role));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success(null);
    }
}
