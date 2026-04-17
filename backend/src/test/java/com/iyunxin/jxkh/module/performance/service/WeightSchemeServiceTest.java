package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.WeightScheme;
import com.iyunxin.jxkh.module.performance.domain.WeightSchemeItem;
import com.iyunxin.jxkh.module.performance.domain.WeightSchemeStatus;
import com.iyunxin.jxkh.module.performance.repository.WeightSchemeItemRepository;
import com.iyunxin.jxkh.module.performance.repository.WeightSchemeRepository;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 权重方案服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("权重方案服务测试")
class WeightSchemeServiceTest {

    @Mock
    private WeightSchemeRepository schemeRepository;

    @Mock
    private WeightSchemeItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrgRepository orgRepository;

    @InjectMocks
    private WeightSchemeService weightSchemeService;

    private User adminUser;
    private WeightScheme draftScheme;
    private WeightScheme publishedScheme;
    private WeightSchemeItem item1;
    private WeightSchemeItem item2;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole("ADMIN");
        adminUser.setOrgId(null);

        draftScheme = new WeightScheme();
        draftScheme.setId(1L);
        draftScheme.setName("标准方案");
        draftScheme.setCode("STANDARD");
        draftScheme.setStatus(WeightSchemeStatus.DRAFT);
        draftScheme.setTotalWeight(BigDecimal.ZERO);
        draftScheme.setOrgId(null);

        publishedScheme = new WeightScheme();
        publishedScheme.setId(2L);
        publishedScheme.setName("已发布方案");
        publishedScheme.setCode("PUBLISHED_SCHEME");
        publishedScheme.setStatus(WeightSchemeStatus.PUBLISHED);
        publishedScheme.setTotalWeight(new BigDecimal("100"));
        publishedScheme.setOrgId(null);

        item1 = new WeightSchemeItem();
        item1.setId(1L);
        item1.setSchemeId(1L);
        item1.setIndicatorId(1L);
        item1.setWeight(new BigDecimal("60.00"));
        item1.setSortOrder(0);

        item2 = new WeightSchemeItem();
        item2.setId(2L);
        item2.setSchemeId(1L);
        item2.setIndicatorId(2L);
        item2.setWeight(new BigDecimal("40.00"));
        item2.setSortOrder(1);
    }

    @Test
    @DisplayName("创建方案 - 成功")
    void testCreateScheme_Success() {
        setCurrentUser(adminUser);

        when(schemeRepository.findTopByCodeAndOrgIdOrderByVersionDesc("STANDARD", null))
                .thenReturn(Optional.empty());
        when(schemeRepository.save(any(WeightScheme.class))).thenReturn(draftScheme);

        WeightScheme result = weightSchemeService.createScheme(draftScheme);

        assertNotNull(result);
        assertEquals(WeightSchemeStatus.DRAFT, result.getStatus());
        assertEquals(1, result.getVersion());
        assertEquals(BigDecimal.ZERO, result.getTotalWeight());
    }

    @Test
    @DisplayName("创建方案 - 编码已存在草稿")
    void testCreateScheme_DraftCodeExists() {
        setCurrentUser(adminUser);

        when(schemeRepository.findTopByCodeAndOrgIdOrderByVersionDesc("STANDARD", null))
                .thenReturn(Optional.of(draftScheme));

        assertThrows(BusinessException.class, () -> {
            weightSchemeService.createScheme(draftScheme);
        });
    }

    @Test
    @DisplayName("更新方案 - 成功")
    void testUpdateScheme_Success() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));
        when(schemeRepository.findTopByCodeAndOrgIdOrderByVersionDesc("STANDARD_UPDATED", null))
                .thenReturn(Optional.empty());
        when(schemeRepository.save(any(WeightScheme.class))).thenReturn(draftScheme);

        WeightScheme updateData = new WeightScheme();
        updateData.setName("标准方案（更新）");
        updateData.setCode("STANDARD_UPDATED");
        updateData.setOrgId(null);

        WeightScheme result = weightSchemeService.updateScheme(1L, updateData);

        assertEquals("标准方案（更新）", result.getName());
    }

    @Test
    @DisplayName("更新方案 - 已发布方案不允许修改")
    void testUpdateScheme_PublishedNotAllowed() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(2L)).thenReturn(Optional.of(publishedScheme));

        WeightScheme updateData = new WeightScheme();
        updateData.setName("新名称");
        updateData.setCode("NEW_CODE");
        updateData.setOrgId(null);

        assertThrows(BusinessException.class, () -> {
            weightSchemeService.updateScheme(2L, updateData);
        });
    }

    @Test
    @DisplayName("保存明细 - 成功")
    void testSaveSchemeItems_Success() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));
        when(schemeRepository.save(any(WeightScheme.class))).thenReturn(draftScheme);

        List<WeightSchemeItem> items = List.of(item1, item2);

        weightSchemeService.saveSchemeItems(1L, items);

        verify(itemRepository, times(1)).deleteBySchemeId(1L);
        verify(itemRepository, times(2)).save(any(WeightSchemeItem.class));
    }

    @Test
    @DisplayName("保存明细 - 权重总和不等于100%")
    void testSaveSchemeItems_WeightSumInvalid() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));

        WeightSchemeItem invalidItem = new WeightSchemeItem();
        invalidItem.setWeight(new BigDecimal("50.00"));

        assertThrows(BusinessException.class, () -> {
            weightSchemeService.saveSchemeItems(1L, List.of(invalidItem));
        });
    }

    @Test
    @DisplayName("保存明细 - 非草稿状态不允许修改")
    void testSaveSchemeItems_NotDraftStatus() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(2L)).thenReturn(Optional.of(publishedScheme));

        assertThrows(BusinessException.class, () -> {
            weightSchemeService.saveSchemeItems(2L, List.of(item1));
        });
    }

    @Test
    @DisplayName("发布方案 - 成功")
    void testPublishScheme_Success() {
        setCurrentUser(adminUser);

        draftScheme.setTotalWeight(new BigDecimal("100"));
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));
        when(itemRepository.sumWeightBySchemeId(1L)).thenReturn(new BigDecimal("100"));
        when(itemRepository.findBySchemeIdOrderBySortOrderAsc(1L)).thenReturn(List.of(item1, item2));
        when(schemeRepository.save(any(WeightScheme.class))).thenReturn(draftScheme);

        WeightScheme result = weightSchemeService.publishScheme(1L);

        assertEquals(WeightSchemeStatus.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublishedAt());
        assertEquals(adminUser.getId(), result.getPublishedBy());
    }

    @Test
    @DisplayName("发布方案 - 权重总和校验失败")
    void testPublishScheme_WeightSumInvalid() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));
        when(itemRepository.sumWeightBySchemeId(1L)).thenReturn(new BigDecimal("80"));

        assertThrows(BusinessException.class, () -> {
            weightSchemeService.publishScheme(1L);
        });
    }

    @Test
    @DisplayName("发布方案 - 无明细")
    void testPublishScheme_NoItems() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));
        when(itemRepository.sumWeightBySchemeId(1L)).thenReturn(new BigDecimal("100"));
        when(itemRepository.findBySchemeIdOrderBySortOrderAsc(1L)).thenReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> {
            weightSchemeService.publishScheme(1L);
        });
    }

    @Test
    @DisplayName("归档方案 - 成功")
    void testArchiveScheme_Success() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(2L)).thenReturn(Optional.of(publishedScheme));
        when(schemeRepository.save(any(WeightScheme.class))).thenReturn(publishedScheme);

        WeightScheme result = weightSchemeService.archiveScheme(2L);

        assertEquals(WeightSchemeStatus.ARCHIVED, result.getStatus());
    }

    @Test
    @DisplayName("归档方案 - 非已发布状态不允许")
    void testArchiveScheme_NotPublished() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));

        assertThrows(BusinessException.class, () -> {
            weightSchemeService.archiveScheme(1L);
        });
    }

    @Test
    @DisplayName("删除方案 - 成功")
    void testDeleteScheme_Success() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));
        when(schemeRepository.save(any(WeightScheme.class))).thenReturn(draftScheme);

        weightSchemeService.deleteScheme(1L);

        assertTrue(draftScheme.getIsDeleted());
        verify(itemRepository, times(1)).deleteBySchemeId(1L);
    }

    @Test
    @DisplayName("删除方案 - 非草稿状态不允许")
    void testDeleteScheme_NotDraft() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(2L)).thenReturn(Optional.of(publishedScheme));

        assertThrows(BusinessException.class, () -> {
            weightSchemeService.deleteScheme(2L);
        });
    }

    @Test
    @DisplayName("复制方案 - 成功")
    void testCopyScheme_Success() {
        setCurrentUser(adminUser);

        draftScheme.setUpdatedAt(LocalDateTime.now());
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));
        when(itemRepository.findBySchemeIdOrderBySortOrderAsc(1L)).thenReturn(List.of(item1, item2));
        when(schemeRepository.save(any(WeightScheme.class))).thenAnswer(invocation -> {
            WeightScheme saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        WeightScheme result = weightSchemeService.copyScheme(1L);

        assertNotNull(result);
        assertTrue(result.getName().contains("(复制)"));
        verify(itemRepository, times(2)).save(any(WeightSchemeItem.class));
    }

    @Test
    @DisplayName("获取方案 - 成功")
    void testGetScheme_Success() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));

        WeightScheme result = weightSchemeService.getScheme(1L);

        assertNotNull(result);
        assertEquals("标准方案", result.getName());
    }

    @Test
    @DisplayName("获取方案明细 - 成功")
    void testGetSchemeItems_Success() {
        setCurrentUser(adminUser);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(draftScheme));
        when(itemRepository.findBySchemeIdOrderBySortOrderAsc(1L)).thenReturn(List.of(item1, item2));

        List<WeightSchemeItem> result = weightSchemeService.getSchemeItems(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("分页查询方案 - 成功")
    void testListSchemes_Success() {
        setCurrentUser(adminUser);

        Page<WeightScheme> mockPage = new PageImpl<>(List.of(draftScheme));
        when(schemeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        Page<WeightScheme> result = weightSchemeService.listSchemes(
                null, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
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
