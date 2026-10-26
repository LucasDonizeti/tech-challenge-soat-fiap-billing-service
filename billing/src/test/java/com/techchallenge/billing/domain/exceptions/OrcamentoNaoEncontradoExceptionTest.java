package com.techchallenge.billing.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - OrcamentoNaoEncontradoException")
class OrcamentoNaoEncontradoExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com ID")
    void deveCriarExcecaoComID() {
        UUID id = UUID.randomUUID();
        OrcamentoNaoEncontradoException exception = new OrcamentoNaoEncontradoException(id);

        assertTrue(exception.getMessage().contains(id.toString()));
        assertEquals("ORCAMENTO_NAO_ENCONTRADO", exception.getErrorCode());
    }

    @Test
    @DisplayName("Deve prefixar mensagem com texto padrão")
    void devePrefixarMensagemComTextoPadrao() {
        UUID id = UUID.randomUUID();
        OrcamentoNaoEncontradoException exception = new OrcamentoNaoEncontradoException(id);

        assertTrue(exception.getMessage().startsWith("Orçamento não encontrado com ID: "));
    }

    @Test
    @DisplayName("Deve ter código de erro fixo")
    void deveTerCodigoDeErroFixo() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        OrcamentoNaoEncontradoException exception1 = new OrcamentoNaoEncontradoException(id1);
        OrcamentoNaoEncontradoException exception2 = new OrcamentoNaoEncontradoException(id2);

        assertEquals("ORCAMENTO_NAO_ENCONTRADO", exception1.getErrorCode());
        assertEquals("ORCAMENTO_NAO_ENCONTRADO", exception2.getErrorCode());
    }

    @Test
    @DisplayName("Deve estender DomainException")
    void deveEstenderDomainException() {
        OrcamentoNaoEncontradoException exception = new OrcamentoNaoEncontradoException(UUID.randomUUID());

        assertTrue(exception instanceof DomainException);
    }

    @Test
    @DisplayName("Deve lançar exceção quando capturada")
    void deveLancarExcecaoQuandoCapturada() {
        UUID id = UUID.randomUUID();

        assertThrows(OrcamentoNaoEncontradoException.class, () -> {
            throw new OrcamentoNaoEncontradoException(id);
        });
    }
}
