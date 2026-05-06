package com.iyunxin.jxkh.module.org.service;

import com.iyunxin.jxkh.module.org.domain.Org;
import com.iyunxin.jxkh.module.org.repository.OrgRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OrgService 单元测试
 * 覆盖5个核心场景
 */
@ExtendWith(MockitoExtension.class)
class OrgServiceTest {

    @Mock
    private OrgRepository orgRepository;

    @InjectMocks
    private OrgService orgService;

    private Org rootOrg;
    private Org childOrg;

    @BeforeEach
    void setUp() {
        // 根组织
        rootOrg = new Org();
        rootOrg.setId(1L);
        rootOrg.setName("总公司");
        rootOrg.setCode("HQ");
        rootOrg.setParentId(null);
        rootOrg.setIsDeleted(false);
        rootOrg.setEnabled(true);
        rootOrg.setSort(1);

        // 子组织
        childOrg = new Org();
        childOrg.setId(2L);
        childOrg.setName("技术部");
        childOrg.setCode("TECH");
        childOrg.setParentId(1L);
        childOrg.setIsDeleted(false);
        childOrg.setEnabled(true);
        childOrg.setSort(1);
    }

    /**
     * 测试场景1: 获取组织树 - 成功
     */
    @Test
    @DisplayName("获取组织树 - 成功")
    void testGetOrgTree_Success() {
        // Given
        List<Org> allOrgs = List.of(rootOrg, childOrg);
        when(orgRepository.findByIsDeletedFalseOrderBySort()).thenReturn(allOrgs);

        // When
        List<OrgService.OrgTreeNode> tree = orgService.getOrgTree();

        // Then
        assertNotNull(tree);
        assertEquals(1, tree.size()); // 只有根节点
        assertEquals("总公司", tree.get(0).getName());
        assertEquals(1, tree.get(0).getChildren().size()); // 根节点有1个子节点
        assertEquals("技术部", tree.get(0).getChildren().get(0).getName());
    }

    /**
     * 测试场景2: 获取所有活跃组织
     */
    @Test
    @DisplayName("获取所有活跃组织")
    void testGetAllActiveOrgs_Success() {
        // Given
        Org disabledOrg = new Org();
        disabledOrg.setId(3L);
        disabledOrg.setName("已禁用部门");
        disabledOrg.setCode("DISABLED");
        disabledOrg.setEnabled(false);
        disabledOrg.setIsDeleted(false);

        List<Org> allOrgs = List.of(rootOrg, childOrg, disabledOrg);
        when(orgRepository.findByIsDeletedFalseOrderBySort()).thenReturn(allOrgs);

        // When
        List<Org> activeOrgs = orgService.getAllActiveOrgs();

        // Then
        assertNotNull(activeOrgs);
        assertEquals(2, activeOrgs.size()); // 只返回启用的组织
        assertTrue(activeOrgs.stream().anyMatch(o -> "总公司".equals(o.getName())));
        assertTrue(activeOrgs.stream().anyMatch(o -> "技术部".equals(o.getName())));
    }

    /**
     * 测试场景3: 创建组织 - 成功
     */
    @Test
    @DisplayName("创建组织 - 成功")
    void testCreateOrg_Success() {
        // Given
        Org newOrg = new Org();
        newOrg.setName("市场部");
        newOrg.setCode("MARKET");
        newOrg.setParentId(1L);

        when(orgRepository.findById(1L)).thenReturn(Optional.of(rootOrg));
        when(orgRepository.findByCode("MARKET")).thenReturn(Optional.empty());
        when(orgRepository.save(any(Org.class))).thenAnswer(invocation -> {
            Org saved = invocation.getArgument(0);
            saved.setId(4L);
            return saved;
        });

        // When
        Org result = orgService.createOrg(newOrg);

        // Then
        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("市场部", result.getName());
        verify(orgRepository).save(any(Org.class));
    }

    /**
     * 测试场景4: 创建组织 - 代码已存在
     */
    @Test
    @DisplayName("创建组织 - 代码已存在")
    void testCreateOrg_CodeExists() {
        // Given
        Org newOrg = new Org();
        newOrg.setName("重复部门");
        newOrg.setCode("HQ"); // 与根组织代码相同

        when(orgRepository.findByCode("HQ")).thenReturn(Optional.of(rootOrg));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orgService.createOrg(newOrg);
        });
        assertEquals("组织代码已存在", exception.getMessage());
    }

    /**
     * 测试场景5: 删除组织 - 有子组织无法删除
     */
    @Test
    @DisplayName("删除组织 - 有子组织无法删除")
    void testDeleteOrg_HasChildren() {
        // Given
        when(orgRepository.findById(1L)).thenReturn(Optional.of(rootOrg));
        when(orgRepository.findByParentId(1L)).thenReturn(List.of(childOrg));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orgService.deleteOrg(1L);
        });
        assertEquals("该组织下还有子组织，无法删除", exception.getMessage());
        verify(orgRepository, never()).save(any(Org.class));
    }

    /**
     * 测试场景6: 删除组织 - 成功（无子组织）
     */
    @Test
    @DisplayName("删除组织 - 成功（无子组织）")
    void testDeleteOrg_Success() {
        // Given
        when(orgRepository.findById(2L)).thenReturn(Optional.of(childOrg));
        when(orgRepository.findByParentId(2L)).thenReturn(List.of());
        when(orgRepository.save(any(Org.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        orgService.deleteOrg(2L);

        // Then
        verify(orgRepository).save(argThat(org -> org.getIsDeleted()));
    }

    /**
     * 测试场景7: 更新组织 - 成功
     */
    @Test
    @DisplayName("更新组织 - 成功")
    void testUpdateOrg_Success() {
        // Given
        Org updatedOrg = new Org();
        updatedOrg.setName("更新后的名称");
        updatedOrg.setCode("NEW_CODE");
        updatedOrg.setSort(2);

        when(orgRepository.findById(1L)).thenReturn(Optional.of(rootOrg));
        when(orgRepository.save(any(Org.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Org result = orgService.updateOrg(1L, updatedOrg);

        // Then
        assertNotNull(result);
        assertEquals("更新后的名称", result.getName());
        assertEquals("NEW_CODE", result.getCode());
        assertEquals(2, result.getSort());
        verify(orgRepository).save(any(Org.class));
    }

    /**
     * 测试场景8: 获取子组织ID列表（包括自己）
     */
    @Test
    @DisplayName("获取子组织ID列表（包括自己）")
    void testGetSubOrgIds_Success() {
        // Given
        Org grandchildOrg = new Org();
        grandchildOrg.setId(3L);
        grandchildOrg.setName("前端组");
        grandchildOrg.setParentId(2L);
        grandchildOrg.setIsDeleted(false);

        List<Org> allOrgs = List.of(rootOrg, childOrg, grandchildOrg);
        when(orgRepository.findByIsDeletedFalseOrderBySort()).thenReturn(allOrgs);

        // When
        List<Long> subOrgIds = orgService.getSubOrgIds(1L);

        // Then
        assertNotNull(subOrgIds);
        assertEquals(3, subOrgIds.size()); // 包括自己和所有子孙组织
        assertTrue(subOrgIds.contains(1L)); // 包含自己
        assertTrue(subOrgIds.contains(2L)); // 包含子组织
        assertTrue(subOrgIds.contains(3L)); // 包含孙组织
    }

    /**
     * 测试场景9: 根据ID获取组织 - 成功
     */
    @Test
    @DisplayName("根据ID获取组织 - 成功")
    void testGetOrgById_Success() {
        // Given
        when(orgRepository.findById(1L)).thenReturn(Optional.of(rootOrg));

        // When
        Org result = orgService.getOrgById(1L);

        // Then
        assertNotNull(result);
        assertEquals("总公司", result.getName());
        assertEquals("HQ", result.getCode());
    }

    /**
     * 测试场景10: 根据ID获取组织 - 不存在
     */
    @Test
    @DisplayName("根据ID获取组织 - 不存在")
    void testGetOrgById_NotFound() {
        // Given
        when(orgRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orgService.getOrgById(999L);
        });
        assertEquals("组织不存在", exception.getMessage());
    }
}
