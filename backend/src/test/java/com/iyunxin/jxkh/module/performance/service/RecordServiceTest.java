package com.iyunxin.jxkh.module.performance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.infra.ai.AISummaryService;
import com.iyunxin.jxkh.infra.security.VirusScanService;
import com.iyunxin.jxkh.infra.storage.FileStorageService;
import com.iyunxin.jxkh.module.performance.domain.PerformanceRecord;
import com.iyunxin.jxkh.module.performance.domain.RecordType;
import com.iyunxin.jxkh.module.performance.repository.IndicatorInstanceRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformanceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RecordService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("绩效记录服务测试")
class RecordServiceTest {

    @Mock
    private PerformanceRecordRepository recordRepository;

    @Mock
    private IndicatorInstanceRepository instanceRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private VirusScanService virusScanService;

    @Mock
    private AISummaryService aiSummaryService;

    @InjectMocks
    private RecordService recordService;

    private ObjectMapper objectMapper;
    private Long currentUserId = 1L;
    private Long planId = 1L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("创建周报记录 - 成功")
    void testCreateWeeklyReport_Success() throws Exception {
        // Given
        RecordService.RecordCreateRequest request = new RecordService.RecordCreateRequest();
        request.setPlanId(planId);
        request.setType(RecordType.WEEKLY_REPORT);
        request.setContent("<p>本周工作总结</p>");
        request.setProgress(BigDecimal.valueOf(75));
        request.setRecordDate(LocalDate.now());
        request.setEnableAiSummary(false);

        PerformanceRecord savedRecord = new PerformanceRecord();
        savedRecord.setId(1L);
        savedRecord.setPlanId(planId);
        savedRecord.setUserId(currentUserId);
        savedRecord.setType(RecordType.WEEKLY_REPORT);
        savedRecord.setContent(request.getContent());
        savedRecord.setProgress(request.getProgress());

        when(recordRepository.save(any(PerformanceRecord.class))).thenReturn(savedRecord);

        // When
        Long recordId = recordService.createRecord(request, currentUserId);

        // Then
        assertNotNull(recordId);
        assertEquals(1L, recordId);
        verify(recordRepository, times(1)).save(any(PerformanceRecord.class));
    }

    @Test
    @DisplayName("创建记录 - 进度超出范围应抛出异常")
    void testCreateRecord_InvalidProgress_ShouldThrowException() {
        // Given
        RecordService.RecordCreateRequest request = new RecordService.RecordCreateRequest();
        request.setPlanId(planId);
        request.setType(RecordType.WEEKLY_REPORT);
        request.setContent("内容");
        request.setProgress(BigDecimal.valueOf(150)); // 超出 100

        // When & Then
        assertThrows(BusinessException.class, () -> {
            recordService.createRecord(request, currentUserId);
        });
    }

    @Test
    @DisplayName("创建记录 - 带附件上传")
    void testCreateRecord_WithAttachments() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        RecordService.RecordCreateRequest request = new RecordService.RecordCreateRequest();
        request.setPlanId(planId);
        request.setType(RecordType.WEEKLY_REPORT);
        request.setContent("内容");
        request.setFiles(new MultipartFile[]{file});

        List<String> fileUrls = Arrays.asList("http://minio/file1.pdf");
        when(fileStorageService.uploadFiles(any(MultipartFile[].class))).thenReturn(fileUrls);

        PerformanceRecord savedRecord = new PerformanceRecord();
        savedRecord.setId(1L);
        when(recordRepository.save(any(PerformanceRecord.class))).thenReturn(savedRecord);

        // When
        Long recordId = recordService.createRecord(request, currentUserId);

        // Then
        assertNotNull(recordId);
        verify(fileStorageService, times(1)).uploadFiles(any(MultipartFile[].class));
        
        ArgumentCaptor<PerformanceRecord> captor = ArgumentCaptor.forClass(PerformanceRecord.class);
        verify(recordRepository).save(captor.capture());
        
        PerformanceRecord captured = captor.getValue();
        assertNotNull(captured.getAttachments());
        assertTrue(captured.getAttachments().contains("http://minio/file1.pdf"));
    }

    @Test
    @DisplayName("创建记录 - 启用 AI 总结")
    void testCreateRecord_WithAiSummary() throws Exception {
        // Given
        RecordService.RecordCreateRequest request = new RecordService.RecordCreateRequest();
        request.setPlanId(planId);
        request.setType(RecordType.WEEKLY_REPORT);
        request.setContent("本周完成了用户管理模块的开发");
        request.setEnableAiSummary(true);

        AISummaryService.AISummaryResult aiResult = AISummaryService.AISummaryResult.builder()
                .keyAchievements(Arrays.asList("完成用户管理模块"))
                .risks(Collections.emptyList())
                .suggestions(Arrays.asList("继续优化代码"))
                .build();

        when(aiSummaryService.generateWeeklySummary(anyString())).thenReturn(aiResult);

        PerformanceRecord savedRecord = new PerformanceRecord();
        savedRecord.setId(1L);
        when(recordRepository.save(any(PerformanceRecord.class))).thenReturn(savedRecord);

        // When
        Long recordId = recordService.createRecord(request, currentUserId);

        // Then
        assertNotNull(recordId);
        verify(aiSummaryService, times(1)).generateWeeklySummary(anyString());
    }

    @Test
    @DisplayName("更新记录 - 成功")
    void testUpdateRecord_Success() {
        // Given
        Long recordId = 1L;
        PerformanceRecord existingRecord = new PerformanceRecord();
        existingRecord.setId(recordId);
        existingRecord.setUserId(currentUserId);
        existingRecord.setContent("旧内容");

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));

        RecordService.RecordUpdateRequest updateRequest = new RecordService.RecordUpdateRequest();
        updateRequest.setContent("新内容");
        updateRequest.setProgress(BigDecimal.valueOf(80));

        // When
        recordService.updateRecord(recordId, updateRequest, currentUserId);

        // Then
        assertEquals("新内容", existingRecord.getContent());
        assertEquals(BigDecimal.valueOf(80), existingRecord.getProgress());
        verify(recordRepository, times(1)).save(existingRecord);
    }

    @Test
    @DisplayName("更新记录 - 无权修改他人记录应抛出异常")
    void testUpdateRecord_PermissionDenied_ShouldThrowException() {
        // Given
        Long recordId = 1L;
        PerformanceRecord existingRecord = new PerformanceRecord();
        existingRecord.setId(recordId);
        existingRecord.setUserId(999L); // 其他用户

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));

        RecordService.RecordUpdateRequest updateRequest = new RecordService.RecordUpdateRequest();
        updateRequest.setContent("新内容");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            recordService.updateRecord(recordId, updateRequest, currentUserId);
        });
    }

    @Test
    @DisplayName("删除记录 - 逻辑删除")
    void testDeleteRecord_Success() {
        // Given
        Long recordId = 1L;
        PerformanceRecord existingRecord = new PerformanceRecord();
        existingRecord.setId(recordId);
        existingRecord.setUserId(currentUserId);
        existingRecord.setIsDeleted(false);

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));

        // When
        recordService.deleteRecord(recordId, currentUserId);

        // Then
        assertTrue(existingRecord.getIsDeleted());
        verify(recordRepository, times(1)).save(existingRecord);
    }

    @Test
    @DisplayName("查询计划记录列表 - 按类型筛选")
    void testGetRecordsByPlan_WithTypeFilter() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<PerformanceRecord> expectedPage = new PageImpl<>(Collections.emptyList());

        when(recordRepository.findByPlanIdAndTypeOrderByRecordDateDesc(
                eq(planId), eq(RecordType.WEEKLY_REPORT), any(PageRequest.class)))
                .thenReturn(expectedPage);

        // When
        Page<PerformanceRecord> result = recordService.getRecordsByPlan(planId, RecordType.WEEKLY_REPORT, pageable);

        // Then
        assertNotNull(result);
        verify(recordRepository, times(1))
                .findByPlanIdAndTypeOrderByRecordDateDesc(eq(planId), eq(RecordType.WEEKLY_REPORT), any(PageRequest.class));
    }

    @Test
    @DisplayName("查询计划记录列表 - 不指定类型")
    void testGetRecordsByPlan_WithoutTypeFilter() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<PerformanceRecord> expectedPage = new PageImpl<>(Collections.emptyList());

        when(recordRepository.findByPlanIdOrderByRecordDateDesc(eq(planId), any(PageRequest.class)))
                .thenReturn(expectedPage);

        // When
        Page<PerformanceRecord> result = recordService.getRecordsByPlan(planId, null, pageable);

        // Then
        assertNotNull(result);
        verify(recordRepository, times(1))
                .findByPlanIdOrderByRecordDateDesc(eq(planId), any(PageRequest.class));
    }

    @Test
    @DisplayName("删除附件 - 成功")
    void testDeleteAttachment_Success() throws Exception {
        // Given
        Long recordId = 1L;
        String fileUrl = "http://minio/file1.pdf";
        
        List<String> attachments = Arrays.asList("http://minio/file1.pdf", "http://minio/file2.pdf");
        String attachmentsJson = objectMapper.writeValueAsString(attachments);

        PerformanceRecord existingRecord = new PerformanceRecord();
        existingRecord.setId(recordId);
        existingRecord.setUserId(currentUserId);
        existingRecord.setAttachments(attachmentsJson);

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));

        // When
        recordService.deleteAttachment(recordId, fileUrl, currentUserId);

        // Then
        verify(fileStorageService, times(1)).deleteFile(fileUrl);
        
        ArgumentCaptor<PerformanceRecord> captor = ArgumentCaptor.forClass(PerformanceRecord.class);
        verify(recordRepository).save(captor.capture());
        
        PerformanceRecord captured = captor.getValue();
        List<String> remainingUrls = objectMapper.readValue(
                captured.getAttachments(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );
        assertEquals(1, remainingUrls.size());
        assertFalse(remainingUrls.contains(fileUrl));
    }
}
