package com.iyunxin.jxkh.module.auth.service;

import com.iyunxin.jxkh.common.util.JwtUtil;
import com.iyunxin.jxkh.module.auth.dto.LoginRequest;
import com.iyunxin.jxkh.module.auth.dto.LoginResponse;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    
    @Value("${app.security.concurrent-login.enabled:true}")
    private boolean concurrentLoginEnabled;
    
    @Value("${app.security.concurrent-login.max-sessions:3}")
    private int maxSessions;

    // 内存存储（当 Redis 不可用时）
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
            incrementLoginFailCount(user);
            throw new RuntimeException("用户名或密码错误");
        }

        resetLoginFailCount(user);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. 检查并发登录限制
        if (concurrentLoginEnabled) {
            checkConcurrentLogin(user.getId());
        }

        // 6. 生成 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 7. 存储 Refresh Token（优先 Redis，失败则用内存）
        String redisKey = REFRESH_TOKEN_PREFIX + user.getId() + ":" + refreshToken;
        try {
            redisTemplate.opsForValue().set(redisKey, user.getUsername(), REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
            log.debug("Refresh Token 已存储到 Redis");
        } catch (Exception e) {
            log.warn("Redis 不可用，使用内存存储: {}", e.getMessage());
            refreshTokenMemoryStore.put(redisKey, user.getUsername());
        }
        
        // 8. 记录用户会话（用于并发控制）
        if (concurrentLoginEnabled) {
            recordUserSession(user.getId(), refreshToken);
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
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
     * 检查并发登录限制
     */
    private void checkConcurrentLogin(Long userId) {
        String sessionKey = "user_sessions:" + userId;
        
        try {
            // 获取当前用户的所有活跃会话
            Set<String> sessions = redisTemplate.opsForZSet().range(sessionKey, 0, -1);
            
            if (sessions != null && sessions.size() >= maxSessions) {
                // 超出限制，踢出最早的会话
                String oldestSession = sessions.iterator().next();
                redisTemplate.opsForZSet().remove(sessionKey, oldestSession);
                
                // 删除该会话的 Refresh Token
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId + ":" + oldestSession);
                
                log.info("用户 {} 超出最大会话数限制({})，已踢出最早会话", userId, maxSessions);
            }
        } catch (Exception e) {
            log.warn("检查并发登录失败: {}", e.getMessage());
            // 不阻断登录流程
        }
    }
    
    /**
     * 记录用户会话
     */
    private void recordUserSession(Long userId, String refreshToken) {
        String sessionKey = "user_sessions:" + userId;
        
        try {
            // 使用 ZSet 存储会话，score 为时间戳
            redisTemplate.opsForZSet().add(sessionKey, refreshToken, System.currentTimeMillis());
            // 设置过期时间（7天）
            redisTemplate.expire(sessionKey, REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
            
            log.debug("用户 {} 的会话已记录", userId);
        } catch (Exception e) {
            log.warn("记录用户会话失败: {}", e.getMessage());
        }
    }

    /**
     * 刷新 Token
     */
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("无效的 Refresh Token");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        String redisKey = REFRESH_TOKEN_PREFIX + userId + ":" + refreshToken;
        String storedUsername = null;
        
        try {
            storedUsername = redisTemplate.opsForValue().get(redisKey);
        } catch (Exception e) {
            log.warn("Redis 不可用，从内存查找");
            storedUsername = refreshTokenMemoryStore.get(redisKey);
        }
        
        if (storedUsername == null || !storedUsername.equals(username)) {
            throw new RuntimeException("Refresh Token 已失效");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        try {
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            refreshTokenMemoryStore.remove(redisKey);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), claims);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        String newRedisKey = REFRESH_TOKEN_PREFIX + user.getId() + ":" + newRefreshToken;
        try {
            redisTemplate.opsForValue().set(newRedisKey, user.getUsername(), REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            refreshTokenMemoryStore.put(newRedisKey, user.getUsername());
        }

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
        String redisKey = REFRESH_TOKEN_PREFIX + userId + ":" + refreshToken;
        try {
            redisTemplate.delete(redisKey);
        } catch (Exception e) {
            refreshTokenMemoryStore.remove(redisKey);
        }
        
        // 清除会话记录
        if (concurrentLoginEnabled) {
            clearUserSession(userId, refreshToken);
        }
        
        log.info("用户 {} 已退出登录", userId);
    }
    
    /**
     * 清除用户会话记录
     */
    private void clearUserSession(Long userId, String refreshToken) {
        String sessionKey = "user_sessions:" + userId;
        
        try {
            redisTemplate.opsForZSet().remove(sessionKey, refreshToken);
            log.debug("用户 {} 的会话已清除", userId);
        } catch (Exception e) {
            log.warn("清除用户会话失败: {}", e.getMessage());
        }
    }

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

    private void incrementLoginFailCount(User user) {
        user.setLoginFailCount(user.getLoginFailCount() + 1);
        if (user.getLoginFailCount() >= 5) {
            user.setStatus("LOCKED");
            user.setLockedAt(LocalDateTime.now());
            log.warn("用户 {} 因多次登录失败被锁定", user.getUsername());
        }
        userRepository.save(user);
    }

    private void resetLoginFailCount(User user) {
        user.setLoginFailCount(0);
        userRepository.save(user);
    }
}
