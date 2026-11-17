package com.techchallenge.billing.domain.models.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - StatusPagamento")
class StatusPagamentoTest {

    @Test
    @DisplayName("Deve ter todos os valores do enum definidos")
    void deveTerTodosOsValoresDoEnumDefinidos() {
        StatusPagamento[] statuses = StatusPagamento.values();

        assertEquals(5, statuses.length);
        assertTrue(containsStatus(statuses, StatusPagamento.PENDENTE));
        assertTrue(containsStatus(statuses, StatusPagamento.APROVADO));
        assertTrue(containsStatus(statuses, StatusPagamento.RECUSADO));
        assertTrue(containsStatus(statuses, StatusPagamento.CANCELADO));
        assertTrue(containsStatus(statuses, StatusPagamento.EXPIRADO));
    }

    @Test
    @DisplayName("Deve converter string para enum corretamente")
    void deveConverterStringParaEnumCorretamente() {
        assertEquals(StatusPagamento.PENDENTE, StatusPagamento.valueOf("PENDENTE"));
        assertEquals(StatusPagamento.APROVADO, StatusPagamento.valueOf("APROVADO"));
        assertEquals(StatusPagamento.RECUSADO, StatusPagamento.valueOf("RECUSADO"));
        assertEquals(StatusPagamento.CANCELADO, StatusPagamento.valueOf("CANCELADO"));
        assertEquals(StatusPagamento.EXPIRADO, StatusPagamento.valueOf("EXPIRADO"));
    }

    @Test
    @DisplayName("Deve lançar exceção para string inválida")
    void deveLancarExcecaoParaStringInvalida() {
        assertThrows(IllegalArgumentException.class, () -> StatusPagamento.valueOf("INVALIDO"));
    }

    @Test
    @DisplayName("Deve ter nome correto para cada valor")
    void deveTerNomeCorretoParaCadaValor() {
        assertEquals("PENDENTE", StatusPagamento.PENDENTE.name());
        assertEquals("APROVADO", StatusPagamento.APROVADO.name());
        assertEquals("RECUSADO", StatusPagamento.RECUSADO.name());
        assertEquals("CANCELADO", StatusPagamento.CANCELADO.name());
        assertEquals("EXPIRADO", StatusPagamento.EXPIRADO.name());
    }

    @Test
    @DisplayName("Deve ter ordinal correto para cada valor")
    void deveTerOrdinalCorretoParaCadaValor() {
        assertEquals(0, StatusPagamento.PENDENTE.ordinal());
        assertEquals(1, StatusPagamento.APROVADO.ordinal());
        assertEquals(2, StatusPagamento.RECUSADO.ordinal());
        assertEquals(3, StatusPagamento.CANCELADO.ordinal());
        assertEquals(4, StatusPagamento.EXPIRADO.ordinal());
    }

    @Test
    @DisplayName("Deve permitir comparação de valores")
    void devePermitirComparacaoDeValores() {
        assertEquals(StatusPagamento.PENDENTE, StatusPagamento.PENDENTE);
        assertNotEquals(StatusPagamento.PENDENTE, StatusPagamento.APROVADO);
    }

    @Test
    @DisplayName("Deve ter toString retornando o nome do enum")
    void deveTerToStringRetornandoONomeDoEnum() {
        assertEquals("PENDENTE", StatusPagamento.PENDENTE.toString());
        assertEquals("APROVADO", StatusPagamento.APROVADO.toString());
    }

    private boolean containsStatus(StatusPagamento[] statuses, StatusPagamento status) {
        for (StatusPagamento s : statuses) {
            if (s == status) {
                return true;
            }
        }
        return false;
    }
}
