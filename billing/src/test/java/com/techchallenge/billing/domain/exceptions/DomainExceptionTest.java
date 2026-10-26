package com.techchallenge.billing.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - DomainException")
class DomainExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        TestDomainException exception = new TestDomainException("Test error message");

        assertEquals("Test error message", exception.getMessage());
        assertEquals("TestDomainException", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e código de erro")
    void deveCriarExcecaoComMensagemECodigoDeErro() {
        TestDomainException exception = new TestDomainException("Test error message", "TEST_ERROR");

        assertEquals("Test error message", exception.getMessage());
        assertEquals("TEST_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem, código de erro e causa")
    void deveCriarExcecaoComMensagemCodigoDeErroECausa() {
        Throwable cause = new RuntimeException("Cause error");
        TestDomainException exception = new TestDomainException("Test error message", "TEST_ERROR", cause);

        assertEquals("Test error message", exception.getMessage());
        assertEquals("TEST_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Deve usar nome da classe como código de erro quando não fornecido")
    void deveUsarNomeDaClasseComoCodigoDeErroQuandoNaoFornecido() {
        TestDomainException exception = new TestDomainException("Test error message", null);

        assertEquals("Test error message", exception.getMessage());
        assertEquals("TestDomainException", exception.getErrorCode());
    }

    @Test
    @DisplayName("Deve estender RuntimeException")
    void deveEstenderRuntimeException() {
        assertTrue(RuntimeException.class.isAssignableFrom(DomainException.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando capturada")
    void deveLancarExcecaoQuandoCapturada() {
        assertThrows(TestDomainException.class, () -> {
            throw new TestDomainException("Test error");
        });
    }

    private static class TestDomainException extends DomainException {
        public TestDomainException(String message) {
            super(message);
        }

        public TestDomainException(String message, String errorCode) {
            super(message, errorCode);
        }

        public TestDomainException(String message, String errorCode, Throwable cause) {
            super(message, errorCode, cause);
        }
    }
}
