package com.iyunxin.jxkh.module.performance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyunxin.jxkh.module.performance.domain.*;
import com.iyunxin.jxkh.module.performance.service.IndicatorCategoryService;
import com.iyunxin.jxkh.module.performance.service.IndicatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * IndicatorController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("IndicatorController 集成测试")
class IndicatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IndicatorService indicatorService;

    @MockitoBean
    private IndicatorCategoryService categoryService;

    private Indicator testIndicator;
    private IndicatorCategory testCategory;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("创建指标 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateIndicator_Success() throws Exception {
        when(indicatorService.createIndicator(any(Indicator.class))).thenReturn(testIndicator);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/indicators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testIndicator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("收入增长率"));

        verify(indicatorService, times(1)).createIndicator(any(Indicator.class));
    }

    @Test
    @DisplayName("获取指标列表 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testListIndicators_Success() throws Exception {
        when(indicatorService.listIndicators(any(), any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testIndicator)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/indicators")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("收入增长率"));
    }

    @Test
    @DisplayName("获取指标详情 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetIndicator_Success() throws Exception {
        when(indicatorService.getIndicator(1L)).thenReturn(testIndicator);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/indicators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("更新指标 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateIndicator_Success() throws Exception {
        Indicator updated = new Indicator();
        updated.setId(1L);
        updated.setName("收入增长率（更新）");
        updated.setCode("REVENUE_GROWTH");
        updated.setCategoryId(1L);
        updated.setType(IndicatorType.QUANTITATIVE);
        updated.setStatus(IndicatorStatus.ACTIVE);
        updated.setOrgId(null);

        when(indicatorService.updateIndicator(anyLong(), any(Indicator.class))).thenReturn(updated);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/indicators/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("收入增长率（更新）"));
    }

    @Test
    @DisplayName("删除指标 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteIndicator_Success() throws Exception {
        doNothing().when(indicatorService).deleteIndicator(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/indicators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(indicatorService, times(1)).deleteIndicator(1L);
    }

    @Test
    @DisplayName("切换指标状态 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testToggleIndicatorStatus_Success() throws Exception {
        testIndicator.setStatus(IndicatorStatus.INACTIVE);
        when(indicatorService.toggleStatus(1L)).thenReturn(testIndicator);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/indicators/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("获取分类树 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetCategoryTree_Success() throws Exception {
        when(categoryService.getCategoryTree()).thenReturn(List.of(testCategory));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/indicator-categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("财务类"));
    }

    @Test
    @DisplayName("创建分类 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateCategory_Success() throws Exception {
        when(categoryService.createCategory(any(IndicatorCategory.class))).thenReturn(testCategory);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/indicator-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(categoryService, times(1)).createCategory(any(IndicatorCategory.class));
    }

    @Test
    @DisplayName("获取指标 - 无权限")
    void testGetIndicator_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/indicators/1"))
                .andExpect(status().isUnauthorized());
    }
}
