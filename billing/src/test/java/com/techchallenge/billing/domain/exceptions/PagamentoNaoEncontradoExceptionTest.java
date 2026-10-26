package com.techchallenge.billing.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - PagamentoNaoEncontradoException")
class PagamentoNaoEncontradoExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com ID")
    void deveCriarExcecaoComID() {
        UUID id = UUID.randomUUID();
        PagamentoNaoEncontradoException exception = new PagamentoNaoEncontradoException(id);

        assertTrue(exception.getMessage().contains(id.toString()));
        assertEquals("PAGAMENTO_NAO_ENCONTRADO", exception.getErrorCode());
    }

    @Test
    @DisplayName("Deve prefixar mensagem com texto padrão")
    void devePrefixarMensagemComTextoPadrao() {
        UUID id = UUID.randomUUID();
        PagamentoNaoEncontradoException exception = new PagamentoNaoEncontradoException(id);

        assertTrue(exception.getMessage().startsWith("Pagamento não encontrado com ID: "));
    }

    @Test
    @DisplayName("Deve ter código de erro fixo")
    void deveTerCodigoDeErroFixo() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        PagamentoNaoEncontradoException exception1 = new PagamentoNaoEncontradoException(id1);
        PagamentoNaoEncontradoException exception2 = new PagamentoNaoEncontradoException(id2);

        assertEquals("PAGAMENTO_NAO_ENCONTRADO", exception1.getErrorCode());
        assertEquals("PAGAMENTO_NAO_ENCONTRADO", exception2.getErrorCode());
    }

    @Test
    @DisplayName("Deve estender DomainException")
    void deveEstenderDomainException() {
        PagamentoNaoEncontradoException exception = new PagamentoNaoEncontradoException(UUID.randomUUID());

        assertTrue(exception instanceof DomainException);
    }

    @Test
    @DisplayName("Deve lançar exceção quando capturada")
    void deveLancarExcecaoQuandoCapturada() {
        UUID id = UUID.randomUUID();

        assertThrows(PagamentoNaoEncontradoException.class, () -> {
            throw new PagamentoNaoEncontradoException(id);
        });
    }
}
