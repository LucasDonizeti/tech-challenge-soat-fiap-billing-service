package com.techchallenge.billing.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - IntegracaoMercadoPagoException")
class IntegracaoMercadoPagoExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        IntegracaoMercadoPagoException exception = new IntegracaoMercadoPagoException("Test error");

        assertEquals("Erro na integração com Mercado Pago: Test error", exception.getMessage());
        assertEquals("ERRO_INTEGRACAO_MERCADO_PAGO", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve criar exceção com mensagem e causa")
    void deveCriarExcecaoComMensagemECausa() {
        Throwable cause = new RuntimeException("Cause error");
        IntegracaoMercadoPagoException exception = new IntegracaoMercadoPagoException("Test error", cause);

        assertEquals("Erro na integração com Mercado Pago: Test error", exception.getMessage());
        assertEquals("ERRO_INTEGRACAO_MERCADO_PAGO", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Deve prefixar mensagem com texto padrão")
    void devePrefixarMensagemComTextoPadrao() {
        IntegracaoMercadoPagoException exception = new IntegracaoMercadoPagoException("Falha ao criar pagamento");

        assertTrue(exception.getMessage().startsWith("Erro na integração com Mercado Pago: "));
        assertTrue(exception.getMessage().contains("Falha ao criar pagamento"));
    }

    @Test
    @DisplayName("Deve ter código de erro fixo")
    void deveTerCodigoDeErroFixo() {
        IntegracaoMercadoPagoException exception1 = new IntegracaoMercadoPagoException("Error 1");
        IntegracaoMercadoPagoException exception2 = new IntegracaoMercadoPagoException("Error 2");

        assertEquals("ERRO_INTEGRACAO_MERCADO_PAGO", exception1.getErrorCode());
        assertEquals("ERRO_INTEGRACAO_MERCADO_PAGO", exception2.getErrorCode());
    }

    @Test
    @DisplayName("Deve estender DomainException")
    void deveEstenderDomainException() {
        IntegracaoMercadoPagoException exception = new IntegracaoMercadoPagoException("Test");

        assertTrue(exception instanceof DomainException);
    }

    @Test
    @DisplayName("Deve lançar exceção quando capturada")
    void deveLancarExcecaoQuandoCapturada() {
        assertThrows(IntegracaoMercadoPagoException.class, () -> {
            throw new IntegracaoMercadoPagoException("Test error");
        });
    }

    @Test
    @DisplayName("Deve preservar causa quando fornecida")
    void devePreservarCausaQuandoFornecida() {
        Throwable cause = new RuntimeException("Original cause");
        IntegracaoMercadoPagoException exception = new IntegracaoMercadoPagoException("Test error", cause);

        assertSame(cause, exception.getCause());
    }
}
