package com.iyunxin.jxkh.module.performance.controller;

import com.iyunxin.jxkh.common.response.ApiResponse;
import com.iyunxin.jxkh.infra.security.VirusScanService;
import com.iyunxin.jxkh.infra.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 文件管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、删除")
public class FileController {
    
    private final FileStorageService fileStorageService;
    private final VirusScanService virusScanService;
    
    // 允许的文件类型
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "jpg", "jpeg", "png", "gif", "bmp",
        "txt", "csv", "zip", "rar"
    );
    
    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("文件大小超过限制（最大 %dMB）", MAX_FILE_SIZE / 1024 / 1024)
            );
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                String.format("不支持的文件类型: %s，支持的类型: %s", 
                    extension, String.join(", ", ALLOWED_EXTENSIONS))
            );
        }
    }
    
    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件到 MinIO，自动进行病毒扫描。支持格式：PDF、DOC、XLS、PPT、图片等，最大10MB")
    public ApiResponse<String> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file) {
        
        try {
            // 验证文件
            validateFile(file);
            
            log.info("开始上传文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            
            // 病毒扫描
            VirusScanService.ScanResult scanResult = virusScanService.scanFile(
                    file.getInputStream(), 
                    file.getOriginalFilename()
            );
            
            if (!scanResult.isClean()) {
                log.warn("文件病毒扫描未通过: {} - {}", file.getOriginalFilename(), scanResult.getMessage());
                return ApiResponse.error("VIRUS_DETECTED", scanResult.getMessage());
            }
            
            // 上传文件
            String fileUrl = fileStorageService.uploadFile(file);
            log.info("文件上传成功: {}", fileUrl);
            
            return ApiResponse.success(fileUrl);
            
        } catch (IllegalArgumentException e) {
            log.warn("文件验证失败: {}", e.getMessage());
            return ApiResponse.error("INVALID_FILE", e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("UPLOAD_FAILED", "文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件，每个文件最大10MB")
    public ApiResponse<List<String>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files) {
        
        try {
            // 验证所有文件
            for (MultipartFile file : files) {
                validateFile(file);
            }
            
            log.info("开始批量上传 {} 个文件", files.length);
            
            // 病毒扫描
            for (MultipartFile file : files) {
                VirusScanService.ScanResult scanResult = virusScanService.scanFile(
                        file.getInputStream(), 
                        file.getOriginalFilename()
                );
                
                if (!scanResult.isClean()) {
                    log.warn("文件病毒扫描未通过: {} - {}", file.getOriginalFilename(), scanResult.getMessage());
                    return ApiResponse.error("VIRUS_DETECTED", 
                            "文件 " + file.getOriginalFilename() + " 未通过病毒扫描");
                }
            }
            
            // 批量上传
            List<String> fileUrls = fileStorageService.uploadFiles(files);
            log.info("批量上传成功，共 {} 个文件", fileUrls.size());
            
            return ApiResponse.success(fileUrls);
            
        } catch (IllegalArgumentException e) {
            log.warn("文件验证失败: {}", e.getMessage());
            return ApiResponse.error("INVALID_FILE", e.getMessage());
        } catch (Exception e) {
            log.error("批量上传失败", e);
            return ApiResponse.error("UPLOAD_FAILED", "批量上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping
    @Operation(summary = "删除文件", description = "从 MinIO 删除文件")
    public ApiResponse<Void> deleteFile(
            @Parameter(description = "文件URL") @RequestParam String fileUrl) {
        
        fileStorageService.deleteFile(fileUrl);
        
        return ApiResponse.success();
    }
    
    /**
     * 获取下载链接
     */
    @GetMapping("/download-url")
    @Operation(summary = "获取下载链接", description = "获取文件的临时下载链接（7天有效）")
    public ApiResponse<String> getDownloadUrl(
            @Parameter(description = "文件名") @RequestParam String fileName) {
        
        String downloadUrl = fileStorageService.getDownloadUrl(fileName);
        
        return ApiResponse.success(downloadUrl);
    }
}
