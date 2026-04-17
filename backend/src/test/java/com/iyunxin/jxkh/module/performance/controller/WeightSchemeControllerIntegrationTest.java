package com.iyunxin.jxkh.module.performance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyunxin.jxkh.module.performance.domain.WeightScheme;
import com.iyunxin.jxkh.module.performance.domain.WeightSchemeItem;
import com.iyunxin.jxkh.module.performance.domain.WeightSchemeStatus;
import com.iyunxin.jxkh.module.performance.service.WeightSchemeService;
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
 * WeightSchemeController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("WeightSchemeController 集成测试")
class WeightSchemeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WeightSchemeService weightSchemeService;

    private WeightScheme testScheme;
    private WeightSchemeItem testItem;

    @BeforeEach
    void setUp() {
        testScheme = new WeightScheme();
        testScheme.setId(1L);
        testScheme.setName("标准方案");
        testScheme.setCode("STANDARD");
        testScheme.setStatus(WeightSchemeStatus.DRAFT);
        testScheme.setTotalWeight(new BigDecimal("100"));
        testScheme.setOrgId(null);

        testItem = new WeightSchemeItem();
        testItem.setId(1L);
        testItem.setSchemeId(1L);
        testItem.setIndicatorId(1L);
        testItem.setWeight(new BigDecimal("100.00"));
        testItem.setSortOrder(0);
    }

    @Test
    @DisplayName("创建权重方案 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateWeightScheme_Success() throws Exception {
        when(weightSchemeService.createScheme(any(WeightScheme.class))).thenReturn(testScheme);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/weight-schemes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testScheme)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("标准方案"));

        verify(weightSchemeService, times(1)).createScheme(any(WeightScheme.class));
    }

    @Test
    @DisplayName("获取权重方案列表 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testListWeightSchemes_Success() throws Exception {
        when(weightSchemeService.listSchemes(any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testScheme)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/weight-schemes")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("标准方案"));
    }

    @Test
    @DisplayName("获取权重方案详情 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetWeightScheme_Success() throws Exception {
        when(weightSchemeService.getScheme(1L)).thenReturn(testScheme);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/weight-schemes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("获取权重方案明细 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetSchemeItems_Success() throws Exception {
        when(weightSchemeService.getSchemeItems(1L)).thenReturn(List.of(testItem));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/weight-schemes/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].weight").value(100.00));
    }

    @Test
    @DisplayName("更新权重方案 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateWeightScheme_Success() throws Exception {
        WeightScheme updated = new WeightScheme();
        updated.setId(1L);
        updated.setName("标准方案（更新）");
        updated.setCode("STANDARD");
        updated.setOrgId(null);

        when(weightSchemeService.updateScheme(anyLong(), any(WeightScheme.class))).thenReturn(updated);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/weight-schemes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("标准方案（更新）"));
    }

    @Test
    @DisplayName("保存权重明细 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSaveSchemeItems_Success() throws Exception {
        doNothing().when(weightSchemeService).saveSchemeItems(anyLong(), anyList());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/weight-schemes/1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(testItem))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(weightSchemeService, times(1)).saveSchemeItems(anyLong(), anyList());
    }

    @Test
    @DisplayName("发布权重方案 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPublishWeightScheme_Success() throws Exception {
        testScheme.setStatus(WeightSchemeStatus.PUBLISHED);
        when(weightSchemeService.publishScheme(1L)).thenReturn(testScheme);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/weight-schemes/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("归档权重方案 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testArchiveWeightScheme_Success() throws Exception {
        testScheme.setStatus(WeightSchemeStatus.ARCHIVED);
        when(weightSchemeService.archiveScheme(1L)).thenReturn(testScheme);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/weight-schemes/1/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }

    @Test
    @DisplayName("删除权重方案 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteWeightScheme_Success() throws Exception {
        doNothing().when(weightSchemeService).deleteScheme(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/weight-schemes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(weightSchemeService, times(1)).deleteScheme(1L);
    }

    @Test
    @DisplayName("复制权重方案 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCopyWeightScheme_Success() throws Exception {
        WeightScheme copied = new WeightScheme();
        copied.setId(2L);
        copied.setName("标准方案 (复制)");
        copied.setCode("STANDARD_COPY_123");
        copied.setStatus(WeightSchemeStatus.DRAFT);

        when(weightSchemeService.copyScheme(1L)).thenReturn(copied);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/weight-schemes/1/copy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("标准方案 (复制)"));
    }

    @Test
    @DisplayName("创建权重方案 - 无权限")
    void testCreateWeightScheme_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/weight-schemes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testScheme)))
                .andExpect(status().isUnauthorized());
    }
}
