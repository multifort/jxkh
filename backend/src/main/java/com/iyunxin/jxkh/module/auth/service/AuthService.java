package com.iyunxin.jxkh.module.auth.service;

import com.iyunxin.jxkh.common.util.JwtUtil;
import com.iyunxin.jxkh.module.auth.dto.LoginRequest;
import com.iyunxin.jxkh.module.auth.dto.LoginResponse;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // 临时内存存储（当 Redis 不可用时）
    private final Map<String, String> refreshTokenMemoryStore = new ConcurrentHashMap<>();

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2. 检查账号状态
        if ("LOCKED".equals(user.getStatus())) {
            throw new RuntimeException("账号已被锁定");
        }
        if ("INACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("账号未激活");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 增加登录失败次数
            incrementLoginFailCount(user);
            throw new RuntimeException("用户名或密码错误");
        }

        // 4. 重置登录失败次数
        resetLoginFailCount(user);

        // 5. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 6. 生成 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 7. 存储 Refresh Token 到 Redis（失败时使用内存存储）
        String redisKey = REFRESH_TOKEN_PREFIX + user.getId() + ":" + refreshToken;
        try {
            redisTemplate.opsForValue().set(redisKey, user.getUsername(), REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 不可用，使用内存存储 Refresh Token: {}", e.getMessage());
            refreshTokenMemoryStore.put(redisKey, user.getUsername());
        }

        // 8. 构建响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24小时
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .orgId(user.getOrgId())
                        .build())
                .build();
    }

    /**
     * 刷新 Token
     */
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        // 1. 验证 Refresh Token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("无效的 Refresh Token");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 2. 检查 Redis 中是否存在（或内存存储）
        String redisKey = REFRESH_TOKEN_PREFIX + userId + ":" + refreshToken;
        String storedUsername = null;
        try {
            storedUsername = redisTemplate.opsForValue().get(redisKey);
        } catch (Exception e) {
            log.warn("Redis 不可用，从内存存储查找: {}", e.getMessage());
            storedUsername = refreshTokenMemoryStore.get(redisKey);
        }
        
        if (storedUsername == null || !storedUsername.equals(username)) {
            throw new RuntimeException("Refresh Token 已失效");
        }

        // 3. 获取用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 4. 删除旧的 Refresh Token（Redis 或内存）
        try {
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.warn("Redis 不可用，从内存存储删除: {}", e.getMessage());
            refreshTokenMemoryStore.remove(redisKey);
        }

        // 5. 生成新的 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), claims);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 6. 存储新的 Refresh Token
        String newRedisKey = REFRESH_TOKEN_PREFIX + user.getId() + ":" + newRefreshToken;
        try {
            redisTemplate.opsForValue().set(newRedisKey, user.getUsername(), REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 不可用，使用内存存储: {}", e.getMessage());
            refreshTokenMemoryStore.put(newRedisKey, user.getUsername());
        }

        // 7. 构建响应
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .orgId(user.getOrgId())
                        .build())
                .build();
    }

    /**
     * 退出登录
     */
    @Transactional
    public void logout(Long userId, String refreshToken) {
        // 删除 Redis 中的 Refresh Token（或内存存储）
        String redisKey = REFRESH_TOKEN_PREFIX + userId + ":" + refreshToken;
        try {
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.warn("Redis 不可用，从内存存储删除: {}", e.getMessage());
            refreshTokenMemoryStore.remove(redisKey);
        }
        
        log.info("用户 {} 已退出登录", userId);
    }

    /**
     * 获取当前用户信息
     */
    public LoginResponse.UserInfo getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .orgId(user.getOrgId())
                .build();
    }

    /**
     * 增加登录失败次数
     */
    private void incrementLoginFailCount(User user) {
        user.setLoginFailCount(user.getLoginFailCount() + 1);
        
        // 失败5次锁定账号
        if (user.getLoginFailCount() >= 5) {
            user.setStatus("LOCKED");
            user.setLockedAt(LocalDateTime.now());
            log.warn("用户 {} 因多次登录失败被锁定", user.getUsername());
        }
        
        userRepository.save(user);
    }

    /**
     * 重置登录失败次数
     */
    private void resetLoginFailCount(User user) {
        user.setLoginFailCount(0);
        userRepository.save(user);
    }
}
