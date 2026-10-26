package com.techchallenge.billing.infrastructure.config;

import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@Configuration
@EnableMongoRepositories(basePackages = "com.techchallenge.billing.infrastructure.persistence.repositories")
@ConditionalOnProperty(
    name = "spring.data.mongodb.uri",
    havingValue = "",
    matchIfMissing = false
)
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        log.info("Configuring MongoDB template");
        return new MongoTemplate(mongoClient, "billing");
    }
}
