package com.techchallenge.billing.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - BusinessException")
class BusinessExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        BusinessException exception = new BusinessException("Business error");

        assertEquals("Business error", exception.getMessage());
        assertEquals("BusinessException", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e código de erro")
    void deveCriarExcecaoComMensagemECodigoDeErro() {
        BusinessException exception = new BusinessException("Business error", "BUS_ERROR");

        assertEquals("Business error", exception.getMessage());
        assertEquals("BUS_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve estender DomainException")
    void deveEstenderDomainException() {
        BusinessException exception = new BusinessException("Test error");

        assertTrue(exception instanceof DomainException);
    }

    @Test
    @DisplayName("Deve lançar exceção quando capturada")
    void deveLancarExcecaoQuandoCapturada() {
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException("Test error");
        });
    }

    @Test
    @DisplayName("Deve usar nome da classe como código padrão")
    void deveUsarNomeDaClasseComoCodigoPadrao() {
        BusinessException exception = new BusinessException("Test error");

        assertEquals("BusinessException", exception.getErrorCode());
    }
}
