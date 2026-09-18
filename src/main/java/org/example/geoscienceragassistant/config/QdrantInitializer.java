package org.example.geoscienceragassistant.config;

import org.example.geoscienceragassistant.service.QdrantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantInitializer {

    @Bean
    CommandLineRunner initializeQdrant(
            QdrantService qdrantService) {

        return args -> {
            qdrantService.createCollection();
        };
    }
}