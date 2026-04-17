package com.iyunxin.jxkh.module.performance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyunxin.jxkh.module.performance.service.PlanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PlanController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("绩效计划控制器集成测试")
class PlanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("创建计划 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreatePlan_Success() throws Exception {
        PlanService.PlanCreateRequest request = new PlanService.PlanCreateRequest();
        request.setUserId(1L);
        request.setCycleId(1L);

        List<PlanService.IndicatorItemRequest> indicators = new ArrayList<>();
        
        PlanService.IndicatorItemRequest item1 = new PlanService.IndicatorItemRequest();
        item1.setIndicatorId(1L);
        item1.setOwnerId(1L);
        item1.setName("营业收入");
        item1.setType("QUANTITATIVE");
        item1.setWeight(new BigDecimal("60"));
        item1.setTargetValue(new BigDecimal("1000"));
        indicators.add(item1);

        PlanService.IndicatorItemRequest item2 = new PlanService.IndicatorItemRequest();
        item2.setIndicatorId(2L);
        item2.setOwnerId(1L);
        item2.setName("客户满意度");
        item2.setType("QUANTITATIVE");
        item2.setWeight(new BigDecimal("40"));
        item2.setTargetValue(new BigDecimal("90"));
        indicators.add(item2);

        request.setIndicators(indicators);

        mockMvc.perform(post("/api/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("查询计划列表 - 成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testListPlans_Success() throws Exception {
        mockMvc.perform(get("/api/v1/plans")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未认证访问 - 返回401")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/plans"))
                .andExpect(status().isUnauthorized());
    }
}
