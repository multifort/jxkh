package com.iyunxin.jxkh.module.performance.service;

import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.module.org.domain.Org;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import com.iyunxin.jxkh.module.performance.domain.IndicatorCategory;
import com.iyunxin.jxkh.module.performance.repository.IndicatorCategoryRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 指标分类服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("指标分类服务测试")
class IndicatorCategoryServiceTest {

    @Mock
    private IndicatorCategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrgRepository orgRepository;

    @InjectMocks
    private IndicatorCategoryService categoryService;

    private User adminUser;
    private User managerUser;
    private User employeeUser;
    private IndicatorCategory rootCategory;
    private IndicatorCategory childCategory;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // 创建测试用户
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole("ADMIN");
        adminUser.setOrgId(null);

        managerUser = new User();
        managerUser.setId(2L);
        managerUser.setRole("MANAGER");
        managerUser.setOrgId(1L);

        employeeUser = new User();
        employeeUser.setId(3L);
        employeeUser.setRole("EMPLOYEE");
        employeeUser.setOrgId(1L);

        // 创建根分类
        rootCategory = new IndicatorCategory();
        rootCategory.setId(1L);
        rootCategory.setName("财务类");
        rootCategory.setCode("FINANCE");
        rootCategory.setParentId(null);
        rootCategory.setLevel(1);
        rootCategory.setSortOrder(1);
        rootCategory.setOrgId(null);
        rootCategory.setCreatedBy(1L);

        // 创建子分类
        childCategory = new IndicatorCategory();
        childCategory.setId(2L);
        childCategory.setName("收入指标");
        childCategory.setCode("REVENUE");
        childCategory.setParentId(1L);
        childCategory.setLevel(2);
        childCategory.setSortOrder(1);
        childCategory.setOrgId(null);
        childCategory.setCreatedBy(1L);
    }

    @Test
    @DisplayName("创建根分类 - 成功")
    void testCreateRootCategory_Success() {
        setCurrentUser(adminUser);

        when(categoryRepository.findByCodeAndOrgId("FINANCE", null)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(IndicatorCategory.class))).thenReturn(rootCategory);

        IndicatorCategory result = categoryService.createCategory(rootCategory);

        assertNotNull(result);
        assertEquals(1, result.getLevel());
        assertNull(result.getParentId());
        verify(categoryRepository, times(1)).save(any(IndicatorCategory.class));
    }

    @Test
    @DisplayName("创建子分类 - 成功")
    void testCreateChildCategory_Success() {
        setCurrentUser(adminUser);

        when(categoryRepository.findByCodeAndOrgId("REVENUE", null)).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.save(any(IndicatorCategory.class))).thenReturn(childCategory);

        IndicatorCategory result = categoryService.createCategory(childCategory);

        assertNotNull(result);
        assertEquals(2, result.getLevel());
        assertEquals(1L, result.getParentId());
    }

    @Test
    @DisplayName("创建分类 - 编码重复")
    void testCreateCategory_DuplicateCode() {
        setCurrentUser(adminUser);

        when(categoryRepository.findByCodeAndOrgId("FINANCE", null))
                .thenReturn(Optional.of(rootCategory));

        assertThrows(BusinessException.class, () -> {
            categoryService.createCategory(rootCategory);
        });
    }

    @Test
    @DisplayName("创建子分类 - 父分类不存在")
    void testCreateChildCategory_ParentNotFound() {
        setCurrentUser(adminUser);

        childCategory.setParentId(999L);
        when(categoryRepository.findByCodeAndOrgId("REVENUE", null)).thenReturn(Optional.empty());
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            categoryService.createCategory(childCategory);
        });
    }

    @Test
    @DisplayName("更新分类 - 成功")
    void testUpdateCategory_Success() {
        setCurrentUser(adminUser);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.findByCodeAndOrgId("FINANCE_UPDATED", null)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(IndicatorCategory.class))).thenReturn(rootCategory);

        IndicatorCategory updateData = new IndicatorCategory();
        updateData.setName("财务类更新");
        updateData.setCode("FINANCE_UPDATED");
        updateData.setParentId(null);
        updateData.setSortOrder(1);
        updateData.setDescription(null);
        updateData.setOrgId(null);

        IndicatorCategory result = categoryService.updateCategory(1L, updateData);

        assertEquals("财务类更新", result.getName());
        assertEquals("FINANCE_UPDATED", result.getCode());
    }

    @Test
    @DisplayName("更新分类 - 编码重复")
    void testUpdateCategory_DuplicateCode() {
        setCurrentUser(adminUser);

        IndicatorCategory existing = new IndicatorCategory();
        existing.setId(2L);
        existing.setCode("FINANCE");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.findByCodeAndOrgId("FINANCE", null)).thenReturn(Optional.of(existing));

        IndicatorCategory updateData = new IndicatorCategory();
        updateData.setCode("FINANCE");
        updateData.setOrgId(null);

        assertThrows(BusinessException.class, () -> {
            categoryService.updateCategory(1L, updateData);
        });
    }

    @Test
    @DisplayName("更新分类 - 父分类不能是自己")
    void testUpdateCategory_ParentCannotBeSelf() {
        setCurrentUser(adminUser);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));

        IndicatorCategory updateData = new IndicatorCategory();
        updateData.setParentId(1L);
        updateData.setOrgId(null);

        assertThrows(BusinessException.class, () -> {
            categoryService.updateCategory(1L, updateData);
        });
    }

    @Test
    @DisplayName("删除分类 - 成功")
    void testDeleteCategory_Success() {
        setCurrentUser(adminUser);

        when(categoryRepository.existsByParentIdAndIsDeletedFalse(2L)).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(childCategory));
        when(categoryRepository.save(any(IndicatorCategory.class))).thenReturn(childCategory);

        categoryService.deleteCategory(2L);

        assertTrue(childCategory.getIsDeleted());
    }

    @Test
    @DisplayName("删除分类 - 存在子分类")
    void testDeleteCategory_HasChildren() {
        setCurrentUser(adminUser);

        when(categoryRepository.existsByParentIdAndIsDeletedFalse(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            categoryService.deleteCategory(1L);
        });
    }

    @Test
    @DisplayName("获取分类 - 成功")
    void testGetCategory_Success() {
        setCurrentUser(adminUser);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));

        IndicatorCategory result = categoryService.getCategory(1L);

        assertNotNull(result);
        assertEquals("财务类", result.getName());
    }

    @Test
    @DisplayName("获取分类 - 不存在")
    void testGetCategory_NotFound() {
        setCurrentUser(adminUser);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            categoryService.getCategory(999L);
        });
    }

    @Test
    @DisplayName("分页查询分类 - 成功")
    void testListCategories_Success() {
        setCurrentUser(adminUser);

        Page<IndicatorCategory> mockPage = new PageImpl<>(List.of(rootCategory));
        when(categoryRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        Page<IndicatorCategory> result = categoryService.listCategories(null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("获取分类树 - 成功")
    void testGetCategoryTree_Success() {
        setCurrentUser(adminUser);

        when(categoryRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(List.of(rootCategory, childCategory));

        List<IndicatorCategory> result = categoryService.getCategoryTree();

        assertNotNull(result);
        assertEquals(2, result.size());
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
