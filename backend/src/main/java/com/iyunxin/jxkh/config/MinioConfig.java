package com.iyunxin.jxkh.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    
    /**
     * 是否启用 MinIO
     */
    private boolean enabled = true;
    
    /**
     * MinIO 服务端点
     */
    private String endpoint;
    
    /**
     * 访问密钥
     */
    private String accessKey;
    
    /**
     * 秘密密钥
     */
    private String secretKey;
    
    /**
     * Bucket 名称
     */
    private String bucketName = "jxkh-files";
    
    /**
     * 上传配置
     */
    private UploadConfig upload = new UploadConfig();
    
    @Data
    public static class UploadConfig {
        private String maxFileSize = "10MB";
        private String maxRequestSize = "50MB";
        private String allowedTypes = "pdf,doc,docx,xls,xlsx,png,jpg,jpeg,gif,txt";
    }
    
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
