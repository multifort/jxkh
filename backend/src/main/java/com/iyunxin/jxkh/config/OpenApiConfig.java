package com.iyunxin.jxkh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置
 * 
 * @author JXKH Team
 * @since 2026-04-15
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("企业绩效考核系统 API")
                        .description("企业绩效考核系统的 RESTful API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("JXKH Team")
                                .email("support@iyunxin.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
