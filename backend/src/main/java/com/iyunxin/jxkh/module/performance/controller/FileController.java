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

import java.util.List;

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
    
    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件到 MinIO，自动进行病毒扫描")
    public ApiResponse<String> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file) {
        
        // 病毒扫描
        try {
            VirusScanService.ScanResult scanResult = virusScanService.scanFile(
                    file.getInputStream(), 
                    file.getOriginalFilename()
            );
            
            if (!scanResult.isClean()) {
                log.warn("文件病毒扫描未通过: {} - {}", file.getOriginalFilename(), scanResult.getMessage());
                return ApiResponse.error("VIRUS_DETECTED", scanResult.getMessage());
            }
        } catch (Exception e) {
            log.error("病毒扫描失败", e);
            return ApiResponse.error("VIRUS_SCAN_FAILED", "病毒扫描失败: " + e.getMessage());
        }
        
        // 上传文件
        String fileUrl = fileStorageService.uploadFile(file);
        
        return ApiResponse.success(fileUrl);
    }
    
    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    public ApiResponse<List<String>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files) {
        
        // 病毒扫描
        for (MultipartFile file : files) {
            try {
                VirusScanService.ScanResult scanResult = virusScanService.scanFile(
                        file.getInputStream(), 
                        file.getOriginalFilename()
                );
                
                if (!scanResult.isClean()) {
                    log.warn("文件病毒扫描未通过: {} - {}", file.getOriginalFilename(), scanResult.getMessage());
                    return ApiResponse.error("VIRUS_DETECTED", 
                            "文件 " + file.getOriginalFilename() + " 未通过病毒扫描");
                }
            } catch (Exception e) {
                log.error("病毒扫描失败", e);
                return ApiResponse.error("VIRUS_SCAN_FAILED", "病毒扫描失败: " + e.getMessage());
            }
        }
        
        // 批量上传
        List<String> fileUrls = fileStorageService.uploadFiles(files);
        
        return ApiResponse.success(fileUrls);
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
