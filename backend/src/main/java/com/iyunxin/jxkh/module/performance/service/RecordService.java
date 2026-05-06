package com.iyunxin.jxkh.module.performance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyunxin.jxkh.common.exception.BusinessException;
import com.iyunxin.jxkh.infra.ai.AISummaryService;
import com.iyunxin.jxkh.infra.security.VirusScanService;
import com.iyunxin.jxkh.infra.storage.FileStorageService;
import com.iyunxin.jxkh.module.performance.domain.IndicatorInstance;
import com.iyunxin.jxkh.module.performance.domain.InstanceStatus;
import com.iyunxin.jxkh.module.performance.domain.PerformanceRecord;
import com.iyunxin.jxkh.module.performance.domain.RecordType;
import com.iyunxin.jxkh.module.performance.repository.IndicatorInstanceRepository;
import com.iyunxin.jxkh.module.performance.repository.PerformanceRecordRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 绩效记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordService {
    
    private final PerformanceRecordRepository recordRepository;
    private final IndicatorInstanceRepository instanceRepository;
    private final FileStorageService fileStorageService;
    private final VirusScanService virusScanService;
    private final AISummaryService aiSummaryService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 创建记录
     */
    @Transactional
    public Long createRecord(RecordCreateRequest request, Long currentUserId) {
        // 验证进度范围
        if (request.getProgress() != null && 
            (request.getProgress().doubleValue() < 0 || request.getProgress().doubleValue() > 100)) {
            throw new BusinessException("INVALID_PROGRESS", "进度必须在 0-100 之间");
        }
        
        PerformanceRecord record = new PerformanceRecord();
        record.setPlanId(request.getPlanId());
        record.setUserId(currentUserId);
        record.setType(request.getType());
        record.setContent(request.getContent());
        record.setProgress(request.getProgress());
        record.setRecordDate(request.getRecordDate() != null ? request.getRecordDate() : LocalDate.now());
        record.setCreatedBy(currentUserId);
        record.setUpdatedBy(currentUserId);
        
        // 处理附件上传
        if (request.getFiles() != null && request.getFiles().length > 0) {
            List<String> fileUrls = fileStorageService.uploadFiles(request.getFiles());
            try {
                record.setAttachments(objectMapper.writeValueAsString(fileUrls));
            } catch (Exception e) {
                log.error("序列化附件列表失败", e);
            }
        }
        
        // AI 智能总结（如果启用）
        if (request.isEnableAiSummary()) {
            try {
                AISummaryService.AISummaryResult summary = aiSummaryService.generateWeeklySummary(request.getContent());
                try {
                    record.setAiSummary(String.join("\n", summary.getKeyAchievements()));
                    record.setAiSuggestions(objectMapper.writeValueAsString(summary));
                } catch (Exception e) {
                    log.error("序列化 AI 总结失败", e);
                }
            } catch (Exception e) {
                log.warn("AI 总结生成失败，不影响记录创建", e);
            }
        }
        
        PerformanceRecord saved = recordRepository.save(record);
        log.info("绩效记录创建成功: {}", saved.getId());
        
        // 更新指标进度
        if (request.getProgress() != null) {
            updateIndicatorProgress(request.getPlanId(), request.getProgress());
        }
        
        return saved.getId();
    }
    
    /**
     * 更新记录
     */
    @Transactional
    public void updateRecord(Long recordId, RecordUpdateRequest request, Long currentUserId) {
        PerformanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("RECORD_NOT_FOUND", "记录不存在"));
        
        // 权限检查：只能修改自己的记录
        if (!record.getUserId().equals(currentUserId)) {
            throw new BusinessException("PERMISSION_DENIED", "无权修改此记录");
        }
        
        if (request.getContent() != null) {
            record.setContent(request.getContent());
        }
        if (request.getProgress() != null) {
            record.setProgress(request.getProgress());
        }
        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        }
        
        // 处理新附件
        if (request.getFiles() != null && request.getFiles().length > 0) {
            List<String> newFileUrls = fileStorageService.uploadFiles(request.getFiles());
            try {
                // 合并旧附件和新附件
                List<String> allUrls = objectMapper.readValue(
                        record.getAttachments() != null ? record.getAttachments() : "[]",
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                allUrls.addAll(newFileUrls);
                record.setAttachments(objectMapper.writeValueAsString(allUrls));
            } catch (Exception e) {
                log.error("处理附件失败", e);
            }
        }
        
        record.setUpdatedBy(currentUserId);
        recordRepository.save(record);
        log.info("绩效记录更新成功: {}", recordId);
    }
    
    /**
     * 删除记录（逻辑删除）
     */
    @Transactional
    public void deleteRecord(Long recordId, Long currentUserId) {
        PerformanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("RECORD_NOT_FOUND", "记录不存在"));
        
        if (!record.getUserId().equals(currentUserId)) {
            throw new BusinessException("PERMISSION_DENIED", "无权删除此记录");
        }
        
        record.setIsDeleted(true);
        recordRepository.save(record);
        log.info("绩效记录删除成功: {}", recordId);
    }
    
    /**
     * 查询计划的记录列表
     */
    public Page<PerformanceRecord> getRecordsByPlan(Long planId, RecordType type, Pageable pageable) {
        if (type != null) {
            return recordRepository.findByPlanIdAndTypeOrderByRecordDateDesc(planId, type, pageable);
        } else {
            return recordRepository.findByPlanIdOrderByRecordDateDesc(planId, pageable);
        }
    }
    
    /**
     * 查询记录详情
     */
    public PerformanceRecord getRecordById(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("RECORD_NOT_FOUND", "记录不存在"));
    }
    
    /**
     * 删除附件
     */
    @Transactional
    public void deleteAttachment(Long recordId, String fileUrl, Long currentUserId) {
        PerformanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("RECORD_NOT_FOUND", "记录不存在"));
        
        if (!record.getUserId().equals(currentUserId)) {
            throw new BusinessException("PERMISSION_DENIED", "无权操作此记录");
        }
        
        try {
            List<String> urls = objectMapper.readValue(
                    record.getAttachments() != null ? record.getAttachments() : "[]",
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            
            if (urls.remove(fileUrl)) {
                record.setAttachments(objectMapper.writeValueAsString(urls));
                recordRepository.save(record);
                
                // 删除 MinIO 文件
                fileStorageService.deleteFile(fileUrl);
                log.info("附件删除成功: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("删除附件失败", e);
            throw new BusinessException("DELETE_ATTACHMENT_FAILED", "删除附件失败");
        }
    }
    
    /**
     * 更新指标进度
     */
    private void updateIndicatorProgress(Long planId, BigDecimal progress) {
        // 查询该计划的所有指标实例
        List<IndicatorInstance> instances = instanceRepository.findByPlanIdAndIsDeletedFalse(planId);
        
        for (IndicatorInstance instance : instances) {
            // 如果指标还没有进度，则更新为当前记录的进度
            if (instance.getProgress() == null || instance.getProgress().compareTo(BigDecimal.ZERO) == 0) {
                instance.setProgress(progress);
                
                // 根据进度更新状态
                if (progress.compareTo(new BigDecimal("100")) >= 0) {
                    instance.setStatus(InstanceStatus.COMPLETED);
                } else if (progress.compareTo(new BigDecimal("60")) < 0) {
                    // 进度低于 60% 标记为延期风险
                    instance.setStatus(InstanceStatus.DELAYED);
                } else {
                    instance.setStatus(InstanceStatus.IN_PROGRESS);
                }
                
                instanceRepository.save(instance);
                log.info("更新指标进度: instanceId={}, progress={}", instance.getId(), progress);
            }
        }
    }
    
    /**
     * 查询计划的风险预警
     */
    public List<IndicatorInstance> getRiskIndicators(Long planId) {
        List<IndicatorInstance> instances = instanceRepository.findByPlanIdAndIsDeletedFalse(planId);
        
        // 筛选出延期的指标
        return instances.stream()
                .filter(instance -> instance.getStatus() == InstanceStatus.DELAYED)
                .toList();
    }
    
    /**
     * 记录创建请求
     */
    @Data
    public static class RecordCreateRequest {
        private Long planId;
        private RecordType type;
        private String content;
        private java.math.BigDecimal progress;
        private LocalDate recordDate;
        private MultipartFile[] files;
        private boolean enableAiSummary = false;
    }
    
    /**
     * 记录更新请求
     */
    @Data
    public static class RecordUpdateRequest {
        private String content;
        private java.math.BigDecimal progress;
        private LocalDate recordDate;
        private MultipartFile[] files;
    }
}
