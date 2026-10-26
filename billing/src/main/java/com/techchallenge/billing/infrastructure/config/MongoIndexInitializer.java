package com.techchallenge.billing.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    public MongoIndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "mongoTemplate must not be null");
        initializeIndexes();
    }

    private void initializeIndexes() {
        try {
            var mappingContext = mongoTemplate.getConverter().getMappingContext();
            IndexResolver indexResolver = new MongoPersistentEntityIndexResolver(mappingContext);

            mappingContext.getPersistentEntities().forEach(entity ->
                indexResolver.resolveIndexFor(entity.getType()).forEach(index ->
                    mongoTemplate.indexOps(entity.getType()).ensureIndex(index)
                )
            );
            log.info("MongoDB indexes initialized successfully");
        } catch (Exception e) {
            log.warn("Error initializing MongoDB indexes (may already exist): {}", e.getMessage());
        }
    }
}
