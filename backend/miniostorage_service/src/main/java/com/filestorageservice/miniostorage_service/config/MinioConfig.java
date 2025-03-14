package com.filestorageservice.miniostorage_service.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.server-address}")
    private String serverAddress;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(serverAddress)
                .credentials(accessKey, secretKey).build();
    }

}
