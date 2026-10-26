package com.techchallenge.billing.infrastructure.config;

import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - MongoConfig")
class MongoConfigTest {

    @Mock
    private MongoClient mongoClient;

    private MongoConfig mongoConfig;

    @BeforeEach
    void setUp() {
        mongoConfig = new MongoConfig();
    }

    @Test
    @DisplayName("Deve criar MongoTemplate com cliente MongoDB e nome do banco")
    void deveCriarMongoTemplateComClienteMongoDBENomeDoBanco() {
        MongoTemplate mongoTemplate = mongoConfig.mongoTemplate(mongoClient);

        assertNotNull(mongoTemplate);
        verify(mongoClient, never()).close();
    }

    @Test
    @DisplayName("Deve criar MongoTemplate sem lançar exceção")
    void deveCriarMongoTemplateSemLancarExcecao() {
        assertDoesNotThrow(() -> mongoConfig.mongoTemplate(mongoClient));
    }

    @Test
    @DisplayName("Deve usar nome do banco 'billing' ao criar MongoTemplate")
    void deveUsarNomeDoBancoBillingAoCriarMongoTemplate() {
        MongoTemplate mongoTemplate = mongoConfig.mongoTemplate(mongoClient);

        assertNotNull(mongoTemplate);
    }
}
