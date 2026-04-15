package com.iyunxin.jxkh.module.auth.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.auth.dto.LoginRequest;
import com.iyunxin.jxkh.module.auth.dto.LoginResponse;
import com.iyunxin.jxkh.module.auth.dto.RefreshTokenRequest;
import com.iyunxin.jxkh.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ApiResponse.success(response);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String refreshToken) {
        authService.logout(userId, refreshToken);
        return ApiResponse.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        LoginResponse.UserInfo userInfo = authService.getCurrentUser(userId);
        return ApiResponse.success(userInfo);
    }
}
