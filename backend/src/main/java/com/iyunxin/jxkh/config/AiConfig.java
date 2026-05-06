package com.iyunxin.jxkh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    
    /**
     * 是否启用 AI 服务
     */
    private boolean enabled = false;
    
    /**
     * AI 提供商：aliyun 或 openai
     */
    private String provider = "aliyun";
    
    /**
     * 阿里云配置
     */
    private AliyunConfig aliyun = new AliyunConfig();
    
    /**
     * OpenAI 配置
     */
    private OpenaiConfig openai = new OpenaiConfig();
    
    @Data
    public static class AliyunConfig {
        private String apiKey;
        private String model = "qwen-plus";
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private long timeout = 30000;
    }
    
    @Data
    public static class OpenaiConfig {
        private String apiKey;
        private String model = "gpt-4";
        private String baseUrl = "https://api.openai.com/v1";
        private long timeout = 30000;
    }
}
