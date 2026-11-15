package com.techchallenge.billing.application.responses;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - OrcamentoResponse")
class OrcamentoResponseTest {

    @Test
    @DisplayName("Deve criar record com todos os campos")
    void deveCriarRecordComTodosOsCampos() {
        UUID id = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("100.00");
        String status = "GERADO";
        LocalDateTime dataCriacao = LocalDateTime.now();
        LocalDateTime dataAtualizacao = LocalDateTime.now();

        OrcamentoResponse response = new OrcamentoResponse(
                id,
                osId,
                clienteId,
                valor,
                status,
                dataCriacao,
                dataAtualizacao
        );

        assertEquals(id, response.id());
        assertEquals(osId, response.ordemServicoId());
        assertEquals(clienteId, response.clienteId());
        assertEquals(valor, response.valorTotal());
        assertEquals(status, response.status());
        assertEquals(dataCriacao, response.dataCriacao());
        assertEquals(dataAtualizacao, response.dataAtualizacao());
    }

    @Test
    @DisplayName("Deve criar record com campos nulos opcionais")
    void deveCriarRecordComCamposNulosOpcionais() {
        OrcamentoResponse response = new OrcamentoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                null,
                null,
                null
        );

        assertNull(response.status());
        assertNull(response.dataCriacao());
        assertNull(response.dataAtualizacao());
    }

    @Test
    @DisplayName("Deve ter record imutável")
    void deveTerRecordImutavel() {
        UUID id = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("150.00");
        String status = "PAGO";
        LocalDateTime dataCriacao = LocalDateTime.now();
        LocalDateTime dataAtualizacao = LocalDateTime.now();

        OrcamentoResponse response = new OrcamentoResponse(
                id,
                osId,
                clienteId,
                valor,
                status,
                dataCriacao,
                dataAtualizacao
        );

        assertEquals(id, response.id());
        assertEquals(osId, response.ordemServicoId());
        assertEquals(clienteId, response.clienteId());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para records iguais")
    void devePermitirEqualsEHashCodeParaRecordsIguais() {
        UUID id = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now();

        OrcamentoResponse response1 = new OrcamentoResponse(
                id,
                osId,
                clienteId,
                new BigDecimal("100.00"),
                "GERADO",
                data,
                data
        );

        OrcamentoResponse response2 = new OrcamentoResponse(
                id,
                osId,
                clienteId,
                new BigDecimal("100.00"),
                "GERADO",
                data,
                data
        );

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("Deve diferenciar records com valores diferentes")
    void deveDiferenciarRecordsComValoresDiferentes() {
        OrcamentoResponse response1 = new OrcamentoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "GERADO",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        OrcamentoResponse response2 = new OrcamentoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("200.00"),
                "PAGO",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertNotEquals(response1, response2);
    }
}
