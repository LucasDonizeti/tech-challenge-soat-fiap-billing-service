package com.techchallenge.billing.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - MongoIndexInitializer")
class MongoIndexInitializerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("Deve criar MongoIndexInitializer com MongoTemplate")
    void deveCriarMongoIndexInitializerComMongoTemplate() {
        assertDoesNotThrow(() -> new MongoIndexInitializer(mongoTemplate));
    }

    @Test
    @DisplayName("Deve lançar exceção quando MongoTemplate é nulo")
    void deveLancarExcecaoQuandoMongoTemplateENulo() {
        assertThrows(NullPointerException.class, () -> new MongoIndexInitializer(null));
    }
}
