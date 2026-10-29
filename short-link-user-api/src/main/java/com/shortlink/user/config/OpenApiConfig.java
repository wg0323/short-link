package com.shortlink.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 文档信息：OpenAPI3 下标题与版本必须由 {@link OpenAPI} Bean 设置，
 * yml 里的 {@code knife4j.openapi.*} 是 springfox 时代的键，不生效。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("短链系统 - 用户端 API")
                        .version("1.0.0")
                        .description("注册登录、短链管理、数据统计与个人中心接口"));
    }
}
