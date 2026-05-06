package com.iyunxin.jxkh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 绩效考核系统启动类
 */
@SpringBootApplication
@EnableScheduling
public class JxkhApplication {

    public static void main(String[] args) {
        SpringApplication.run(JxkhApplication.class, args);
    }

}
