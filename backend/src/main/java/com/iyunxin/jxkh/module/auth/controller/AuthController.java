package com.iyunxin.jxkh.module.auth.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.module.auth.dto.LoginRequest;
import com.iyunxin.jxkh.module.auth.dto.LoginResponse;
import com.iyunxin.jxkh.module.auth.dto.RefreshTokenRequest;
import com.iyunxin.jxkh.module.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    
    @Value("${app.security.refresh-token.cookie-name:refresh_token}")
    private String refreshTokenCookieName;
    
    @Value("${app.security.refresh-token.max-age:604800}")
    private int refreshTokenMaxAge; // 默认7天

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        
        // 设置 Refresh Token 到 HttpOnly Cookie
        setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        
        // 从响应中移除 Refresh Token（不再通过 JSON 返回）
        loginResponse.setRefreshToken(null);
        
        return ApiResponse.success(loginResponse);
    }
    
    /**
     * 设置 Refresh Token Cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
        cookie.setHttpOnly(true);  // 防止 XSS 攻击
        cookie.setSecure(false);   // 开发环境设为 false，生产环境应设为 true（需要 HTTPS）
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenMaxAge);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ApiResponse.error("Refresh Token 不存在");
        }
        
        LoginResponse loginResponse = authService.refreshToken(refreshToken);
        
        // 更新 Refresh Token Cookie
        setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        
        // 从响应中移除 Refresh Token
        loginResponse.setRefreshToken(null);
        
        return ApiResponse.success(loginResponse);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("X-User-Id") Long userId,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            authService.logout(userId, refreshToken);
        }
        
        // 清除 Refresh Token Cookie
        clearRefreshTokenCookie(response);
        
        return ApiResponse.success();
    }
    
    /**
     * 清除 Refresh Token Cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 立即过期
        response.addCookie(cookie);
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
