package com.iyunxin.jxkh.infra.storage;

import com.iyunxin.jxkh.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    
    @PostConstruct
    public void init() {
        if (!minioConfig.isEnabled()) {
            log.info("MinIO 存储未启用");
            return;
        }
        
        try {
            // 检查 bucket 是否存在，不存在则创建
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );
            
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );
                log.info("MinIO Bucket 创建成功: {}", minioConfig.getBucketName());
            }
            
            log.info("MinIO 存储服务初始化成功");
        } catch (Exception e) {
            log.error("MinIO 初始化失败", e);
        }
    }
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file) {
        validateFile(file);
        
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            log.info("文件上传成功: {}", fileName);
            return getFileUrl(fileName);
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量上传文件
     *
     * @param files 文件列表
     * @return 文件 URL 列表
     */
    public List<String> uploadFiles(MultipartFile[] files) {
        return Arrays.stream(files)
                .map(this::uploadFile)
                .toList();
    }
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件 URL
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileName(fileUrl);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
            log.info("文件删除成功: {}", fileName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件下载 URL（临时链接，7天有效）
     *
     * @param fileName 文件名
     * @return 下载 URL
     */
    public String getDownloadUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取下载 URL 失败: {}", fileName, e);
            throw new RuntimeException("获取下载 URL 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件流
     *
     * @param fileName 文件名
     * @return 文件流
     */
    public InputStream getFileStream(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件流失败: {}", fileName, e);
            throw new RuntimeException("获取文件流失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 检查文件大小
        String maxSizeStr = minioConfig.getUpload().getMaxFileSize();
        long maxSize = parseSize(maxSizeStr);
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + maxSizeStr);
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        String allowedTypes = minioConfig.getUpload().getAllowedTypes();
        List<String> allowedTypesList = Arrays.asList(allowedTypes.split(","));
        
        if (!allowedTypesList.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
    }
    
    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
    
    /**
     * 获取文件访问 URL
     */
    private String getFileUrl(String fileName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + fileName;
    }
    
    /**
     * 从 URL 中提取文件名
     */
    private String extractFileName(String fileUrl) {
        String prefix = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/";
        return fileUrl.replace(prefix, "");
    }
    
    /**
     * 解析文件大小字符串（如 "10MB" -> 10485760）
     */
    private long parseSize(String sizeStr) {
        sizeStr = sizeStr.toUpperCase().trim();
        if (sizeStr.endsWith("KB")) {
            return Long.parseLong(sizeStr.replace("KB", "").trim()) * 1024;
        } else if (sizeStr.endsWith("MB")) {
            return Long.parseLong(sizeStr.replace("MB", "").trim()) * 1024 * 1024;
        } else if (sizeStr.endsWith("GB")) {
            return Long.parseLong(sizeStr.replace("GB", "").trim()) * 1024 * 1024 * 1024;
        } else {
            return Long.parseLong(sizeStr);
        }
    }
}
