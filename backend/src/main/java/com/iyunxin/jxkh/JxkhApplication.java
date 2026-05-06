package com.iyunxin.jxkh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 绩效考核系统启动类
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry // 启用重试机制
public class JxkhApplication {

    public static void main(String[] args) {
        SpringApplication.run(JxkhApplication.class, args);
    }

}
