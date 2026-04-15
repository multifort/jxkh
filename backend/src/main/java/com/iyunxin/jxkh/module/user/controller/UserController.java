package com.iyunxin.jxkh.module.user.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户CRUD及状态管理")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public ApiResponse<Page<User>> getUsers(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词（姓名或用户名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "组织ID") @RequestParam(required = false) Long orgId,
            @Parameter(description = "角色") @RequestParam(required = false) String role) {
        return ApiResponse.success(userService.getUsers(page, size, keyword, orgId, role));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ApiResponse<User> createUser(@RequestBody User user) {
        return ApiResponse.success(userService.createUser(user));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ApiResponse.success(userService.updateUser(id, user));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "启用/禁用用户")
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ApiResponse<User> toggleUserStatus(@PathVariable Long id) {
        return ApiResponse.success(userService.toggleUserStatus(id));
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ApiResponse.success(null);
    }

    @Operation(summary = "解锁用户")
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> unlockUser(@PathVariable Long id) {
        return ApiResponse.success(userService.unlockUser(id));
    }

    @Operation(summary = "分配用户角色")
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ApiResponse<Void> assignRoles(
            @PathVariable Long id,
            @RequestBody java.util.List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取用户角色ID列表")
    @GetMapping("/{id}/roles")
    public ApiResponse<java.util.List<Long>> getUserRoles(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserRoleIds(id));
    }
}
