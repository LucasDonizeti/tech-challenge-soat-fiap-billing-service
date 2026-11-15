package com.techchallenge.billing.domain.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - CobrancaPixGeradaEvent")
class CobrancaPixGeradaEventTest {

    @Test
    @DisplayName("Deve criar evento com todos os campos usando construtor completo")
    void deveCriarEventoComTodosOsCamposUsandoConstrutorCompleto() {
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String qrCodePix = "pix-code";
        String qrCodeBase64 = "base64";
        BigDecimal valorCobrado = new BigDecimal("100.00");
        LocalDateTime dataExpiracao = LocalDateTime.now();
        String externalReference = "ref-123";

        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                qrCodePix,
                qrCodeBase64,
                valorCobrado,
                dataExpiracao,
                externalReference
        );

        assertEquals(pagamentoId, event.getPagamentoId());
        assertEquals(orcamentoId, event.getOrcamentoId());
        assertEquals(ordemServicoId, event.getOrdemServicoId());
        assertEquals(clienteId, event.getClienteId());
        assertEquals(qrCodePix, event.getQrCodePix());
        assertEquals(qrCodeBase64, event.getQrCodeBase64());
        assertEquals(valorCobrado, event.getValorCobrado());
        assertEquals(dataExpiracao, event.getDataExpiracao());
        assertEquals(externalReference, event.getExternalReference());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve criar evento com timestamp automático")
    void deveCriarEventoComTimestampAutomatico() {
        LocalDateTime beforeCreation = LocalDateTime.now();
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code",
                "base64",
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "ref-123"
        );
        LocalDateTime afterCreation = LocalDateTime.now();

        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(event.getTimestamp().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    @DisplayName("Deve implementar DomainEvent")
    void deveImplementarDomainEvent() {
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code",
                "base64",
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "ref-123"
        );

        assertTrue(event instanceof DomainEvent);
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter construtor padrão para deserialização")
    void deveTerConstrutorPadraoParaDeserializacao() {
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent();

        assertNotNull(event);
        assertNull(event.getPagamentoId());
        assertNull(event.getOrcamentoId());
        assertNull(event.getOrdemServicoId());
        assertNull(event.getClienteId());
        assertNull(event.getQrCodePix());
        assertNull(event.getQrCodeBase64());
        assertNull(event.getValorCobrado());
        assertNull(event.getDataExpiracao());
        assertNull(event.getExternalReference());
        assertNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter construtor all-args para deserialização")
    void deveTerConstrutorAllArgsParaDeserializacao() {
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String qrCodePix = "pix-code";
        String qrCodeBase64 = "base64";
        BigDecimal valorCobrado = new BigDecimal("100.00");
        LocalDateTime dataExpiracao = LocalDateTime.now();
        String externalReference = "ref-123";
        LocalDateTime timestamp = LocalDateTime.now();

        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                qrCodePix,
                qrCodeBase64,
                valorCobrado,
                dataExpiracao,
                externalReference,
                timestamp
        );

        assertEquals(pagamentoId, event.getPagamentoId());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve implementar Serializable")
    void deveImplementarSerializable() {
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code",
                "base64",
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "ref-123"
        );

        assertTrue(event instanceof java.io.Serializable);
    }



    @Test
    @DisplayName("Deve ter getters para todos os campos")
    void deveTerGettersParaTodosOsCampos() {
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code",
                "base64",
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "ref-123"
        );

        assertNotNull(event.getPagamentoId());
        assertNotNull(event.getOrcamentoId());
        assertNotNull(event.getOrdemServicoId());
        assertNotNull(event.getClienteId());
        assertNotNull(event.getQrCodePix());
        assertNotNull(event.getQrCodeBase64());
        assertNotNull(event.getValorCobrado());
        assertNotNull(event.getDataExpiracao());
        assertNotNull(event.getExternalReference());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter toString informativo")
    void deveTerToStringInformativo() {
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code",
                "base64",
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "ref-123"
        );

        String toString = event.toString();
        assertTrue(toString.contains("CobrancaPixGeradaEvent"));
        assertTrue(toString.contains("pagamentoId="));
        assertTrue(toString.contains("ordemServicoId="));
        assertTrue(toString.contains("clienteId="));
        assertTrue(toString.contains("valorCobrado="));
        assertTrue(toString.contains("dataExpiracao="));
    }
}
