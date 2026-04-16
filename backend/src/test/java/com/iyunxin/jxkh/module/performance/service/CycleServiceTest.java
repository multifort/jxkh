package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.org.domain.Org;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.CycleStatus;
import com.iyunxin.jxkh.module.performance.domain.CycleType;
import com.iyunxin.jxkh.module.performance.domain.PerformanceCycle;
import com.iyunxin.jxkh.module.performance.repository.PerformanceCycleRepository;
import com.iyunxin.jxkh.module.user.domain.User;
import com.iyunxin.jxkh.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 绩效周期服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("绩效周期服务测试")
class CycleServiceTest {

    @Mock
    private PerformanceCycleRepository cycleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrgRepository orgRepository;

    @InjectMocks
    private CycleService cycleService;

    private User adminUser;
    private User managerUser;
    private User employeeUser;
    private PerformanceCycle testCycle;

    @BeforeEach
    void setUp() {
        // 清除 SecurityContext
        SecurityContextHolder.clearContext();

        // 创建测试用户
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole("ADMIN");
        adminUser.setOrgId(1L);

        managerUser = new User();
        managerUser.setId(2L);
        managerUser.setRole("MANAGER");
        managerUser.setOrgId(2L);

        employeeUser = new User();
        employeeUser.setId(3L);
        employeeUser.setRole("EMPLOYEE");
        employeeUser.setOrgId(3L);

        // 创建测试周期
        testCycle = new PerformanceCycle();
        testCycle.setId(1L);
        testCycle.setName("2026年Q1");
        testCycle.setType(CycleType.QUARTERLY);
        testCycle.setStartDate(LocalDate.of(2026, 1, 1));
        testCycle.setEndDate(LocalDate.of(2026, 3, 31));
        testCycle.setStatus(CycleStatus.DRAFT);
        testCycle.setOrgId(1L);
    }

    @Test
    @DisplayName("创建周期 - 成功")
    void testCreateCycle_Success() {
        // 设置当前用户为管理员
        setCurrentUser(adminUser);

        // Mock repository
        when(cycleRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsDeletedFalse(
                any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        when(cycleRepository.save(any(PerformanceCycle.class))).thenReturn(testCycle);

        // 执行测试
        PerformanceCycle result = cycleService.createCycle(testCycle);

        // 验证结果
        assertNotNull(result);
        assertEquals(CycleStatus.DRAFT, result.getStatus());
        verify(cycleRepository, times(1)).save(any(PerformanceCycle.class));
    }

    @Test
    @DisplayName("创建周期 - 时间冲突")
    void testCreateCycle_DateConflict() {
        setCurrentUser(adminUser);

        // Mock 存在时间冲突
        when(cycleRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsDeletedFalse(
                any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

        // 执行测试，应该抛出异常
        assertThrows(BusinessException.class, () -> {
            cycleService.createCycle(testCycle);
        });
    }

    @Test
    @DisplayName("创建周期 - 开始日期晚于结束日期")
    void testCreateCycle_InvalidDate() {
        setCurrentUser(adminUser);

        testCycle.setStartDate(LocalDate.of(2026, 3, 31));
        testCycle.setEndDate(LocalDate.of(2026, 1, 1));

        assertThrows(BusinessException.class, () -> {
            cycleService.createCycle(testCycle);
        });
    }

    @Test
    @DisplayName("启动周期 - 成功")
    void testStartCycle_Success() {
        setCurrentUser(adminUser);

        testCycle.setStatus(CycleStatus.DRAFT);
        when(cycleRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(testCycle));
        when(cycleRepository.save(any(PerformanceCycle.class))).thenReturn(testCycle);

        PerformanceCycle result = cycleService.startCycle(1L);

        assertEquals(CycleStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    @DisplayName("启动周期 - 状态不正确")
    void testStartCycle_InvalidStatus() {
        setCurrentUser(adminUser);

        testCycle.setStatus(CycleStatus.IN_PROGRESS);
        when(cycleRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(testCycle));

        assertThrows(BusinessException.class, () -> {
            cycleService.startCycle(1L);
        });
    }

    @Test
    @DisplayName("结束周期 - 成功")
    void testEndCycle_Success() {
        setCurrentUser(adminUser);

        testCycle.setStatus(CycleStatus.IN_PROGRESS);
        when(cycleRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(testCycle));
        when(cycleRepository.save(any(PerformanceCycle.class))).thenReturn(testCycle);

        PerformanceCycle result = cycleService.endCycle(1L);

        assertEquals(CycleStatus.ENDED, result.getStatus());
    }

    @Test
    @DisplayName("删除周期 - 成功")
    void testDeleteCycle_Success() {
        setCurrentUser(adminUser);

        testCycle.setStatus(CycleStatus.DRAFT);
        when(cycleRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(testCycle));
        when(cycleRepository.save(any(PerformanceCycle.class))).thenReturn(testCycle);

        cycleService.deleteCycle(1L);

        assertTrue(testCycle.getIsDeleted());
    }

    @Test
    @DisplayName("删除周期 - 非草稿状态不允许删除")
    void testDeleteCycle_NotDraft() {
        setCurrentUser(adminUser);

        testCycle.setStatus(CycleStatus.IN_PROGRESS);
        when(cycleRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(testCycle));

        assertThrows(BusinessException.class, () -> {
            cycleService.deleteCycle(1L);
        });
    }

    /**
     * 设置当前用户到 SecurityContext
     */
    private void setCurrentUser(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getId(), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }
}
