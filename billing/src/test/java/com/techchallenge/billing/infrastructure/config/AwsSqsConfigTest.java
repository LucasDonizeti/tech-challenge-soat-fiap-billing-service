package com.techchallenge.billing.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - AwsSqsConfig")
class AwsSqsConfigTest {

    private AwsSqsConfig config;

    @BeforeEach
    void setUp() {
        config = new AwsSqsConfig();
    }

    @Test
    @DisplayName("Deve lançar exceção quando queue é nulo")
    void deveLancarExcecaoQuandoQueueENulo() {
        ReflectionTestUtils.setField(config, "queue", null);

        assertThrows(IllegalStateException.class, config::validateQueue);
    }

    @Test
    @DisplayName("Deve lançar exceção quando queue é vazio")
    void deveLancarExcecaoQuandoQueueEVazio() {
        ReflectionTestUtils.setField(config, "queue", "");

        assertThrows(IllegalStateException.class, config::validateQueue);
    }

    @Test
    @DisplayName("Deve lançar exceção quando queue é apenas espaços")
    void deveLancarExcecaoQuandoQueueEApenasEspacos() {
        ReflectionTestUtils.setField(config, "queue", "   ");

        assertThrows(IllegalStateException.class, config::validateQueue);
    }

    @Test
    @DisplayName("Deve validar quando queue é válido")
    void deveValidarQuandoQueueEValido() {
        ReflectionTestUtils.setField(config, "queue", "https://sqs.us-east-1.amazonaws.com/123456789/orcamento-pronto");

        assertDoesNotThrow(config::validateQueue);
    }

    @Test
    @DisplayName("Deve ter mensagem de erro específica")
    void deveTerMensagemDeErroEspecifica() {
        ReflectionTestUtils.setField(config, "queue", null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, config::validateQueue);

        assertTrue(exception.getMessage().contains("AWS_SQS_QUEUE_ORCAMENTO_PRONTO"));
    }
}
