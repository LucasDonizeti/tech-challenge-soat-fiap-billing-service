package com.techchallenge.billing.domain.models.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - StatusOrcamentoChange")
class StatusOrcamentoChangeTest {

    @Test
    @DisplayName("Deve criar registro com status e timestamp")
    void deveCriarRegistroComStatusETimestamp() {
        LocalDateTime timestamp = LocalDateTime.now();
        StatusOrcamentoChange change = new StatusOrcamentoChange(StatusOrcamento.GERADO, timestamp);

        assertEquals(StatusOrcamento.GERADO, change.status());
        assertEquals(timestamp, change.timestamp());
    }

    @Test
    @DisplayName("Deve lançar exceção quando status é nulo")
    void deveLancarExcecaoQuandoStatusENulo() {
        LocalDateTime timestamp = LocalDateTime.now();

        assertThrows(NullPointerException.class, () -> new StatusOrcamentoChange(null, timestamp));
    }

    @Test
    @DisplayName("Deve lançar exceção quando timestamp é nulo")
    void deveLancarExcecaoQuandoTimestampENulo() {
        assertThrows(NullPointerException.class, () -> new StatusOrcamentoChange(StatusOrcamento.GERADO, null));
    }

    @Test
    @DisplayName("Deve lançar exceção quando ambos status e timestamp são nulos")
    void deveLancarExcecaoQuandoAmbosStatusETimestampSaoNulos() {
        assertThrows(NullPointerException.class, () -> new StatusOrcamentoChange(null, null));
    }

    @Test
    @DisplayName("Deve criar registro com diferentes status")
    void deveCriarRegistroComDiferentesStatus() {
        LocalDateTime timestamp = LocalDateTime.now();

        StatusOrcamentoChange change1 = new StatusOrcamentoChange(StatusOrcamento.GERADO, timestamp);
        StatusOrcamentoChange change2 = new StatusOrcamentoChange(StatusOrcamento.AGUARDANDO_PAGAMENTO, timestamp);
        StatusOrcamentoChange change3 = new StatusOrcamentoChange(StatusOrcamento.PAGO, timestamp);
        StatusOrcamentoChange change4 = new StatusOrcamentoChange(StatusOrcamento.RECUSADO, timestamp);
        StatusOrcamentoChange change5 = new StatusOrcamentoChange(StatusOrcamento.CANCELADO, timestamp);

        assertEquals(StatusOrcamento.GERADO, change1.status());
        assertEquals(StatusOrcamento.AGUARDANDO_PAGAMENTO, change2.status());
        assertEquals(StatusOrcamento.PAGO, change3.status());
        assertEquals(StatusOrcamento.RECUSADO, change4.status());
        assertEquals(StatusOrcamento.CANCELADO, change5.status());
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        StatusOrcamento status = StatusOrcamento.PAGO;
        LocalDateTime timestamp = LocalDateTime.now();

        StatusOrcamentoChange change = new StatusOrcamentoChange(status, timestamp);

        assertEquals(status, change.status());
        assertEquals(timestamp, change.timestamp());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para registros iguais")
    void devePermitirEqualsEHashCodeParaRegistrosIguais() {
        LocalDateTime timestamp = LocalDateTime.now();

        StatusOrcamentoChange change1 = new StatusOrcamentoChange(StatusOrcamento.GERADO, timestamp);
        StatusOrcamentoChange change2 = new StatusOrcamentoChange(StatusOrcamento.GERADO, timestamp);

        assertEquals(change1, change2);
        assertEquals(change1.hashCode(), change2.hashCode());
    }

    @Test
    @DisplayName("Deve diferenciar registros com status diferentes")
    void deveDiferenciarRegistrosComStatusDiferentes() {
        LocalDateTime timestamp = LocalDateTime.now();

        StatusOrcamentoChange change1 = new StatusOrcamentoChange(StatusOrcamento.GERADO, timestamp);
        StatusOrcamentoChange change2 = new StatusOrcamentoChange(StatusOrcamento.PAGO, timestamp);

        assertNotEquals(change1, change2);
    }

    @Test
    @DisplayName("Deve diferenciar registros com timestamp diferentes")
    void deveDiferenciarRegistrosComTimestampDiferentes() {
        LocalDateTime timestamp1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime timestamp2 = LocalDateTime.of(2024, 1, 1, 11, 0);

        StatusOrcamentoChange change1 = new StatusOrcamentoChange(StatusOrcamento.GERADO, timestamp1);
        StatusOrcamentoChange change2 = new StatusOrcamentoChange(StatusOrcamento.GERADO, timestamp2);

        assertNotEquals(change1, change2);
    }

    @Test
    @DisplayName("Deve ter métodos de acesso funcionando corretamente")
    void deveTerMetodosDeAcessoFuncionandoCorretamente() {
        LocalDateTime timestamp = LocalDateTime.now();
        StatusOrcamentoChange change = new StatusOrcamentoChange(StatusOrcamento.AGUARDANDO_PAGAMENTO, timestamp);

        assertNotNull(change.status());
        assertNotNull(change.timestamp());
    }
}
