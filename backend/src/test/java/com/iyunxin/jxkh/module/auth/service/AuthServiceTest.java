package com.iyunxin.jxkh.module.auth.service;

import com.iyunxin.jxkh.common.util.JwtUtil;
import com.iyunxin.jxkh.module.auth.dto.LoginRequest;
import com.iyunxin.jxkh.module.auth.dto.LoginResponse;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 单元测试")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setStatus("ACTIVE");
        testUser.setLoginFailCount(0);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("登录成功 - 返回Token和用户信息")
    void testLogin_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyLong())).thenReturn("refresh-token");

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUserInfo());
        assertEquals("testuser", response.getUserInfo().getUsername());
        
        verify(redisTemplate).opsForValue();
        verify(userRepository).save(any(User.class)); // 更新最后登录时间
    }

    @Test
    @DisplayName("登录失败 - 用户名不存在")
    void testLogin_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(request));
        
        assertTrue(exception.getMessage().contains("用户名或密码错误"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(request));
        
        assertTrue(exception.getMessage().contains("用户名或密码错误"));
        verify(userRepository).save(argThat(user -> 
            user.getLoginFailCount() != null && user.getLoginFailCount() == 1
        ));
    }

    @Test
    @DisplayName("登录失败 - 账号被禁用")
    void testLogin_AccountInactive() {
        // Given
        testUser.setStatus("INACTIVE");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        
        assertTrue(exception.getMessage().contains("账号已被禁用"));
    }

    @Test
    @DisplayName("登录失败 - 账号被锁定")
    void testLogin_AccountLocked() {
        // Given
        testUser.setStatus("LOCKED");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        
        assertTrue(exception.getMessage().contains("账号已被锁定"));
    }

    @Test
    @DisplayName("退出登录 - 成功删除Refresh Token")
    void testLogout_Success() {
        // Given
        Long userId = 1L;
        String refreshToken = "refresh-token";
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));

        // When
        authService.logout(userId, refreshToken);

        // Then
        verify(redisTemplate.opsForValue()).get("refresh_token:" + userId + ":" + refreshToken);
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("刷新Token - 成功返回新Token")
    void testRefreshToken_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        when(redisTemplate.opsForValue().get(anyString())).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(anyLong())).thenReturn("new-refresh-token");

        // When
        LoginResponse response = authService.refreshToken(refreshToken);

        // Then
        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    @DisplayName("刷新Token - Token无效")
    void testRefreshToken_Invalid() {
        // Given
        String invalidToken = "invalid-token";
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.refreshToken(invalidToken));
        
        assertTrue(exception.getMessage().contains("Refresh Token已失效"));
    }
}
