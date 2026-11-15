package com.techchallenge.billing.application.responses;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - PagamentoResponse")
class PagamentoResponseTest {

    @Test
    @DisplayName("Deve criar record com todos os campos")
    void deveCriarRecordComTodosOsCampos() {
        UUID id = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        String qrCodePix = "pix-code";
        String qrCodeBase64 = "base64";
        String status = "APROVADO";
        LocalDateTime dataExpiracao = LocalDateTime.now();
        LocalDateTime dataPagamento = LocalDateTime.now();

        PagamentoResponse response = new PagamentoResponse(
                id,
                orcamentoId,
                osId,
                qrCodePix,
                qrCodeBase64,
                status,
                dataExpiracao,
                dataPagamento
        );

        assertEquals(id, response.id());
        assertEquals(orcamentoId, response.orcamentoId());
        assertEquals(osId, response.ordemServicoId());
        assertEquals(qrCodePix, response.qrCodePix());
        assertEquals(qrCodeBase64, response.qrCodeBase64());
        assertEquals(status, response.status());
        assertEquals(dataExpiracao, response.dataExpiracao());
        assertEquals(dataPagamento, response.dataPagamento());
    }

    @Test
    @DisplayName("Deve criar record com campos nulos opcionais")
    void deveCriarRecordComCamposNulosOpcionais() {
        PagamentoResponse response = new PagamentoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                "PENDENTE",
                null,
                null
        );

        assertNull(response.qrCodePix());
        assertNull(response.qrCodeBase64());
        assertNull(response.dataExpiracao());
        assertNull(response.dataPagamento());
    }

    @Test
    @DisplayName("Deve ter record imutável")
    void deveTerRecordImutavel() {
        UUID id = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        String qrCodePix = "pix-code";
        String qrCodeBase64 = "base64";
        String status = "APROVADO";
        LocalDateTime dataExpiracao = LocalDateTime.now();
        LocalDateTime dataPagamento = LocalDateTime.now();

        PagamentoResponse response = new PagamentoResponse(
                id,
                orcamentoId,
                osId,
                qrCodePix,
                qrCodeBase64,
                status,
                dataExpiracao,
                dataPagamento
        );

        assertEquals(id, response.id());
        assertEquals(orcamentoId, response.orcamentoId());
        assertEquals(osId, response.ordemServicoId());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para records iguais")
    void devePermitirEqualsEHashCodeParaRecordsIguais() {
        UUID id = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now();

        PagamentoResponse response1 = new PagamentoResponse(
                id,
                orcamentoId,
                osId,
                "pix-code",
                "base64",
                "APROVADO",
                data,
                data
        );

        PagamentoResponse response2 = new PagamentoResponse(
                id,
                orcamentoId,
                osId,
                "pix-code",
                "base64",
                "APROVADO",
                data,
                data
        );

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("Deve diferenciar records com valores diferentes")
    void deveDiferenciarRecordsComValoresDiferentes() {
        PagamentoResponse response1 = new PagamentoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code-1",
                "base64-1",
                "APROVADO",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        PagamentoResponse response2 = new PagamentoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code-2",
                "base64-2",
                "RECUSADO",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertNotEquals(response1, response2);
    }
}
