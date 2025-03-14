package com.filestorageservice.miniostorage_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MiniostorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniostorageServiceApplication.class, args);
    }

}
