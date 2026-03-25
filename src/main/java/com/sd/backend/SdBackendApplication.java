package com.sd.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableMongoAuditing
@org.springframework.scheduling.annotation.EnableScheduling
@EnableAsync
public class SdBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdBackendApplication.class, args);
    }
}
