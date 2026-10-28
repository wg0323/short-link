package com.shortlink.user.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端。
 *
 * <p>MinIO 实例可能被多个项目共用，本项目的对象统一放在 {@code shortlink.minio.bucket}
 * 指定的桶里，与其它项目隔离。</p>
 */
@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(@Value("${shortlink.minio.endpoint}") String endpoint,
                                   @Value("${shortlink.minio.access-key}") String accessKey,
                                   @Value("${shortlink.minio.secret-key}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
