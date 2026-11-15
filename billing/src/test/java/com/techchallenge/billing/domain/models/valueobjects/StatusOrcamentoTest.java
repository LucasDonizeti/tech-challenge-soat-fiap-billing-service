package com.techchallenge.billing.domain.models.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - StatusOrcamento")
class StatusOrcamentoTest {

    @Test
    @DisplayName("Deve ter todos os valores do enum definidos")
    void deveTerTodosOsValoresDoEnumDefinidos() {
        StatusOrcamento[] statuses = StatusOrcamento.values();

        assertEquals(5, statuses.length);
        assertTrue(containsStatus(statuses, StatusOrcamento.GERADO));
        assertTrue(containsStatus(statuses, StatusOrcamento.AGUARDANDO_PAGAMENTO));
        assertTrue(containsStatus(statuses, StatusOrcamento.PAGO));
        assertTrue(containsStatus(statuses, StatusOrcamento.RECUSADO));
        assertTrue(containsStatus(statuses, StatusOrcamento.CANCELADO));
    }

    @Test
    @DisplayName("Deve converter string para enum corretamente")
    void deveConverterStringParaEnumCorretamente() {
        assertEquals(StatusOrcamento.GERADO, StatusOrcamento.valueOf("GERADO"));
        assertEquals(StatusOrcamento.AGUARDANDO_PAGAMENTO, StatusOrcamento.valueOf("AGUARDANDO_PAGAMENTO"));
        assertEquals(StatusOrcamento.PAGO, StatusOrcamento.valueOf("PAGO"));
        assertEquals(StatusOrcamento.RECUSADO, StatusOrcamento.valueOf("RECUSADO"));
        assertEquals(StatusOrcamento.CANCELADO, StatusOrcamento.valueOf("CANCELADO"));
    }

    @Test
    @DisplayName("Deve lançar exceção para string inválida")
    void deveLancarExcecaoParaStringInvalida() {
        assertThrows(IllegalArgumentException.class, () -> StatusOrcamento.valueOf("INVALIDO"));
    }

    @Test
    @DisplayName("Deve ter nome correto para cada valor")
    void deveTerNomeCorretoParaCadaValor() {
        assertEquals("GERADO", StatusOrcamento.GERADO.name());
        assertEquals("AGUARDANDO_PAGAMENTO", StatusOrcamento.AGUARDANDO_PAGAMENTO.name());
        assertEquals("PAGO", StatusOrcamento.PAGO.name());
        assertEquals("RECUSADO", StatusOrcamento.RECUSADO.name());
        assertEquals("CANCELADO", StatusOrcamento.CANCELADO.name());
    }

    @Test
    @DisplayName("Deve ter ordinal correto para cada valor")
    void deveTerOrdinalCorretoParaCadaValor() {
        assertEquals(0, StatusOrcamento.GERADO.ordinal());
        assertEquals(1, StatusOrcamento.AGUARDANDO_PAGAMENTO.ordinal());
        assertEquals(2, StatusOrcamento.PAGO.ordinal());
        assertEquals(3, StatusOrcamento.RECUSADO.ordinal());
        assertEquals(4, StatusOrcamento.CANCELADO.ordinal());
    }

    @Test
    @DisplayName("Deve permitir comparação de valores")
    void devePermitirComparacaoDeValores() {
        assertEquals(StatusOrcamento.GERADO, StatusOrcamento.GERADO);
        assertNotEquals(StatusOrcamento.GERADO, StatusOrcamento.PAGO);
    }

    @Test
    @DisplayName("Deve ter toString retornando o nome do enum")
    void deveTerToStringRetornandoONomeDoEnum() {
        assertEquals("GERADO", StatusOrcamento.GERADO.toString());
        assertEquals("PAGO", StatusOrcamento.PAGO.toString());
    }

    private boolean containsStatus(StatusOrcamento[] statuses, StatusOrcamento status) {
        for (StatusOrcamento s : statuses) {
            if (s == status) {
                return true;
            }
        }
        return false;
    }
}
