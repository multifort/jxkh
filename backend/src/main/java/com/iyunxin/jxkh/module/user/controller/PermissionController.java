package com.iyunxin.jxkh.module.user.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.user.domain.Permission;
import com.iyunxin.jxkh.module.user.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理", description = "权限查询操作")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取当前用户的所有权限")
    @GetMapping("/my")
    public ApiResponse<Set<String>> getMyPermissions(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        return ApiResponse.success(permissionService.getUserPermissionCodes(userId));
    }

    @Operation(summary = "获取当前用户的所有权限详情")
    @GetMapping("/my/details")
    public ApiResponse<List<Permission>> getMyPermissionDetails(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        return ApiResponse.success(permissionService.getUserPermissions(userId));
    }

    @Operation(summary = "获取所有活跃权限")
    @GetMapping
    public ApiResponse<List<Permission>> getAllPermissions() {
        return ApiResponse.success(permissionService.getAllActivePermissions());
    }

    @Operation(summary = "检查当前用户是否有指定权限")
    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> checkPermission(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String code) {
        Long userId = Long.valueOf(userDetails.getUsername());
        boolean hasPermission = permissionService.hasPermission(userId, code);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("code", code);
        result.put("hasPermission", hasPermission);
        return ApiResponse.success(result);
    }
}
