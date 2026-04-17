package com.iyunxin.jxkh.module.performance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyunxin.jxkh.module.performance.domain.CycleStatus;
import com.iyunxin.jxkh.module.performance.domain.CycleType;
import com.iyunxin.jxkh.module.performance.domain.PerformanceCycle;
import com.iyunxin.jxkh.module.performance.service.CycleService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CycleController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CycleController 集成测试")
class CycleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CycleService cycleService;

    private PerformanceCycle testCycle;

    @BeforeEach
    void setUp() {
        testCycle = new PerformanceCycle();
        testCycle.setId(1L);
        testCycle.setName("2026年Q1");
        testCycle.setType(CycleType.QUARTERLY);
        testCycle.setStartDate(LocalDate.of(2026, 1, 1));
        testCycle.setEndDate(LocalDate.of(2026, 3, 31));
        testCycle.setStatus(CycleStatus.DRAFT);
        testCycle.setOrgId(null);
    }

    @Test
    @DisplayName("创建周期 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateCycle_Success() throws Exception {
        when(cycleService.createCycle(any(PerformanceCycle.class))).thenReturn(testCycle);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cycles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCycle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("2026年Q1"));

        verify(cycleService, times(1)).createCycle(any(PerformanceCycle.class));
    }

    @Test
    @DisplayName("获取周期列表 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testListCycles_Success() throws Exception {
        when(cycleService.getCycles(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(testCycle)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cycles")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("2026年Q1"));
    }

    @Test
    @DisplayName("获取周期详情 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetCycle_Success() throws Exception {
        when(cycleService.getCycleById(1L)).thenReturn(testCycle);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cycles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("2026年Q1"));
    }

    @Test
    @DisplayName("更新周期 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateCycle_Success() throws Exception {
        PerformanceCycle updated = new PerformanceCycle();
        updated.setId(1L);
        updated.setName("2026年Q1（更新）");
        updated.setType(CycleType.QUARTERLY);
        updated.setStartDate(LocalDate.of(2026, 1, 1));
        updated.setEndDate(LocalDate.of(2026, 3, 31));
        updated.setStatus(CycleStatus.DRAFT);

        when(cycleService.updateCycle(anyLong(), any(PerformanceCycle.class))).thenReturn(updated);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/cycles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("2026年Q1（更新）"));
    }

    @Test
    @DisplayName("启动周期 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testStartCycle_Success() throws Exception {
        testCycle.setStatus(CycleStatus.IN_PROGRESS);
        when(cycleService.startCycle(1L)).thenReturn(testCycle);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cycles/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("结束周期 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testEndCycle_Success() throws Exception {
        testCycle.setStatus(CycleStatus.ENDED);
        when(cycleService.endCycle(1L)).thenReturn(testCycle);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cycles/1/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ENDED"));
    }

    @Test
    @DisplayName("删除周期 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteCycle_Success() throws Exception {
        doNothing().when(cycleService).deleteCycle(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/cycles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cycleService, times(1)).deleteCycle(1L);
    }

    @Test
    @DisplayName("创建周期 - 无权限")
    void testCreateCycle_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cycles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCycle)))
                .andExpect(status().isUnauthorized());
    }
}
