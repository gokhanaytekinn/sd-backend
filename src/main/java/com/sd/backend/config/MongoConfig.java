package com.sd.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@Slf4j
public class MongoConfig {

    @Bean
    public CommandLineRunner logMongoConnection(MongoTemplate mongoTemplate) {
        return args -> {
            try {
                // Perform a simple operation to verify connection
                mongoTemplate.getDb().listCollectionNames().first();
                log.info("Successfully connected to MongoDB database: {}", mongoTemplate.getDb().getName());
            } catch (Exception e) {
                log.error("Failed to connect to MongoDB: {}", e.getMessage());
            }
        };
    }
}
