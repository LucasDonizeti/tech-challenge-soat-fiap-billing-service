package com.techchallenge.billing.domain.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - PagamentoRecusadoEvent")
class PagamentoRecusadoEventTest {

    @Test
    @DisplayName("Deve criar evento com todos os campos usando construtor")
    void deveCriarEventoComTodosOsCamposUsandoConstrutor() {
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String motivo = "Cartão inválido";
        String statusMercadoPago = "rejected";

        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                motivo,
                statusMercadoPago
        );

        assertEquals(pagamentoId, event.getPagamentoId());
        assertEquals(orcamentoId, event.getOrcamentoId());
        assertEquals(ordemServicoId, event.getOrdemServicoId());
        assertEquals(clienteId, event.getClienteId());
        assertEquals(motivo, event.getMotivo());
        assertEquals(statusMercadoPago, event.getStatusMercadoPago());
        assertNotNull(event.getDataRejeicao());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve criar evento com timestamp e dataRejeicao automáticos")
    void deveCriarEventoComTimestampEDataRejeicaoAutomaticos() {
        LocalDateTime beforeCreation = LocalDateTime.now();
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Cartão expirado",
                "rejected"
        );
        LocalDateTime afterCreation = LocalDateTime.now();

        assertNotNull(event.getDataRejeicao());
        assertNotNull(event.getTimestamp());
        assertTrue(event.getDataRejeicao().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(event.getDataRejeicao().isBefore(afterCreation.plusSeconds(1)));
        assertTrue(event.getTimestamp().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(event.getTimestamp().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    @DisplayName("Deve implementar DomainEvent")
    void deveImplementarDomainEvent() {
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Motivo",
                "status"
        );

        assertTrue(event instanceof DomainEvent);
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter construtor padrão para deserialização")
    void deveTerConstrutorPadraoParaDeserializacao() {
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent();

        assertNotNull(event);
        assertNull(event.getPagamentoId());
        assertNull(event.getOrcamentoId());
        assertNull(event.getOrdemServicoId());
        assertNull(event.getClienteId());
        assertNull(event.getMotivo());
        assertNull(event.getStatusMercadoPago());
        assertNull(event.getDataRejeicao());
        assertNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter construtor all-args para deserialização")
    void deveTerConstrutorAllArgsParaDeserializacao() {
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String motivo = "Motivo";
        String statusMercadoPago = "status";
        LocalDateTime dataRejeicao = LocalDateTime.now();
        LocalDateTime timestamp = LocalDateTime.now();

        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                motivo,
                statusMercadoPago,
                dataRejeicao,
                timestamp
        );

        assertEquals(pagamentoId, event.getPagamentoId());
        assertEquals(dataRejeicao, event.getDataRejeicao());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve implementar Serializable")
    void deveImplementarSerializable() {
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Motivo",
                "status"
        );

        assertTrue(event instanceof java.io.Serializable);
    }



    @Test
    @DisplayName("Deve ter getters para todos os campos")
    void deveTerGettersParaTodosOsCampos() {
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Motivo de rejeição",
                "rejected"
        );

        assertNotNull(event.getPagamentoId());
        assertNotNull(event.getOrcamentoId());
        assertNotNull(event.getOrdemServicoId());
        assertNotNull(event.getClienteId());
        assertNotNull(event.getMotivo());
        assertNotNull(event.getStatusMercadoPago());
        assertNotNull(event.getDataRejeicao());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter toString informativo")
    void deveTerToStringInformativo() {
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Cartão inválido",
                "rejected"
        );

        String toString = event.toString();
        assertTrue(toString.contains("PagamentoRecusadoEvent"));
        assertTrue(toString.contains("pagamentoId="));
        assertTrue(toString.contains("ordemServicoId="));
        assertTrue(toString.contains("clienteId="));
        assertTrue(toString.contains("motivo="));
        assertTrue(toString.contains("statusMercadoPago="));
        assertTrue(toString.contains("dataRejeicao="));
    }
}
