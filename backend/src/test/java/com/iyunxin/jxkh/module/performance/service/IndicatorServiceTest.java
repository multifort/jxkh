package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.*;
import com.iyunxin.jxkh.module.performance.repository.IndicatorCategoryRepository;
import com.iyunxin.jxkh.module.performance.repository.IndicatorRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 指标服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("指标服务测试")
class IndicatorServiceTest {

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private IndicatorCategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrgRepository orgRepository;

    @InjectMocks
    private IndicatorService indicatorService;

    private User adminUser;
    private Indicator testIndicator;
    private IndicatorCategory testCategory;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole("ADMIN");
        adminUser.setOrgId(null);

        testCategory = new IndicatorCategory();
        testCategory.setId(1L);
        testCategory.setName("财务类");
        testCategory.setCode("FINANCE");

        testIndicator = new Indicator();
        testIndicator.setId(1L);
        testIndicator.setName("收入增长率");
        testIndicator.setCode("REVENUE_GROWTH");
        testIndicator.setCategoryId(1L);
        testIndicator.setType(IndicatorType.QUANTITATIVE);
        testIndicator.setStatus(IndicatorStatus.ACTIVE);
        testIndicator.setDefaultWeight(new BigDecimal("20.00"));
        testIndicator.setOrgId(null);
        testIndicator.setCreatedBy(1L);
    }

    @Test
    @DisplayName("创建指标 - 成功")
    void testCreateIndicator_Success() {
        setCurrentUser(adminUser);

        when(indicatorRepository.findByCodeAndOrgId("REVENUE_GROWTH", null)).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);

        Indicator result = indicatorService.createIndicator(testIndicator);

        assertNotNull(result);
        assertEquals(IndicatorStatus.ACTIVE, result.getStatus());
        verify(indicatorRepository, times(1)).save(any(Indicator.class));
    }

    @Test
    @DisplayName("创建指标 - 编码重复")
    void testCreateIndicator_DuplicateCode() {
        setCurrentUser(adminUser);

        when(indicatorRepository.findByCodeAndOrgId("REVENUE_GROWTH", null))
                .thenReturn(Optional.of(testIndicator));

        assertThrows(BusinessException.class, () -> {
            indicatorService.createIndicator(testIndicator);
        });
    }

    @Test
    @DisplayName("创建指标 - 分类不存在")
    void testCreateIndicator_CategoryNotFound() {
        setCurrentUser(adminUser);

        testIndicator.setCategoryId(999L);
        when(indicatorRepository.findByCodeAndOrgId("REVENUE_GROWTH", null)).thenReturn(Optional.empty());
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            indicatorService.createIndicator(testIndicator);
        });
    }

    @Test
    @DisplayName("创建指标 - 默认状态为ACTIVE")
    void testCreateIndicator_DefaultStatus() {
        setCurrentUser(adminUser);

        testIndicator.setStatus(null);
        when(indicatorRepository.findByCodeAndOrgId("REVENUE_GROWTH", null)).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);

        Indicator result = indicatorService.createIndicator(testIndicator);

        assertEquals(IndicatorStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("更新指标 - 成功")
    void testUpdateIndicator_Success() {
        setCurrentUser(adminUser);

        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(indicatorRepository.findByCodeAndOrgId("REVENUE_GROWTH_UPDATED", null)).thenReturn(Optional.empty());
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);

        Indicator updateData = new Indicator();
        updateData.setName("收入增长率（更新）");
        updateData.setCode("REVENUE_GROWTH_UPDATED");
        updateData.setCategoryId(1L);
        updateData.setType(IndicatorType.QUANTITATIVE);
        updateData.setOrgId(null);

        Indicator result = indicatorService.updateIndicator(1L, updateData);

        assertEquals("收入增长率（更新）", result.getName());
    }

    @Test
    @DisplayName("更新指标 - 编码重复")
    void testUpdateIndicator_DuplicateCode() {
        setCurrentUser(adminUser);

        Indicator duplicate = new Indicator();
        duplicate.setId(2L);
        duplicate.setCode("REVENUE_GROWTH");

        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(indicatorRepository.findByCodeAndOrgId("REVENUE_GROWTH", null))
                .thenReturn(Optional.of(duplicate));

        Indicator updateData = new Indicator();
        updateData.setCode("REVENUE_GROWTH");
        updateData.setCategoryId(1L);
        updateData.setOrgId(null);

        assertThrows(BusinessException.class, () -> {
            indicatorService.updateIndicator(1L, updateData);
        });
    }

    @Test
    @DisplayName("更新指标 - 分类不存在")
    void testUpdateIndicator_CategoryNotFound() {
        setCurrentUser(adminUser);

        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(categoryRepository.existsById(999L)).thenReturn(false);

        Indicator updateData = new Indicator();
        updateData.setCode("REVENUE_GROWTH");
        updateData.setCategoryId(999L);
        updateData.setOrgId(null);

        assertThrows(BusinessException.class, () -> {
            indicatorService.updateIndicator(1L, updateData);
        });
    }

    @Test
    @DisplayName("删除指标 - 成功")
    void testDeleteIndicator_Success() {
        setCurrentUser(adminUser);

        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);

        indicatorService.deleteIndicator(1L);

        assertTrue(testIndicator.getIsDeleted());
    }

    @Test
    @DisplayName("获取指标 - 成功")
    void testGetIndicator_Success() {
        setCurrentUser(adminUser);

        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));

        Indicator result = indicatorService.getIndicator(1L);

        assertNotNull(result);
        assertEquals("收入增长率", result.getName());
    }

    @Test
    @DisplayName("获取指标 - 不存在")
    void testGetIndicator_NotFound() {
        setCurrentUser(adminUser);

        when(indicatorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            indicatorService.getIndicator(999L);
        });
    }

    @Test
    @DisplayName("切换状态 - 从ACTIVE到INACTIVE")
    void testToggleStatus_ActiveToInactive() {
        setCurrentUser(adminUser);

        testIndicator.setStatus(IndicatorStatus.ACTIVE);
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);

        Indicator result = indicatorService.toggleStatus(1L);

        assertEquals(IndicatorStatus.INACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("切换状态 - 从INACTIVE到ACTIVE")
    void testToggleStatus_InactiveToActive() {
        setCurrentUser(adminUser);

        testIndicator.setStatus(IndicatorStatus.INACTIVE);
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(indicatorRepository.save(any(Indicator.class))).thenReturn(testIndicator);

        Indicator result = indicatorService.toggleStatus(1L);

        assertEquals(IndicatorStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("分页查询指标 - 成功")
    void testListIndicators_Success() {
        setCurrentUser(adminUser);

        Page<Indicator> mockPage = new PageImpl<>(List.of(testIndicator));
        when(indicatorRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        Page<Indicator> result = indicatorService.listIndicators(
                null, null, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("分页查询指标 - 带筛选条件")
    void testListIndicators_WithFilters() {
        setCurrentUser(adminUser);

        Page<Indicator> mockPage = new PageImpl<>(List.of(testIndicator));
        when(indicatorRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        Page<Indicator> result = indicatorService.listIndicators(
                "收入", 1L, "QUANTITATIVE", "ACTIVE", PageRequest.of(0, 10));

        assertNotNull(result);
        verify(indicatorRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class));
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
