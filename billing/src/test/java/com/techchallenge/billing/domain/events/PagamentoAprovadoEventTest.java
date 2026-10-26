package com.techchallenge.billing.domain.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - PagamentoAprovadoEvent")
class PagamentoAprovadoEventTest {

    @Test
    @DisplayName("Deve criar evento com todos os campos usando construtor completo")
    void deveCriarEventoComTodosOsCamposUsandoConstrutorCompleto() {
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String mercadoPagoPaymentId = "mp-payment-123";
        BigDecimal valorPago = new BigDecimal("100.00");
        LocalDateTime dataPagamento = LocalDateTime.now();

        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                mercadoPagoPaymentId,
                valorPago,
                dataPagamento
        );

        assertEquals(pagamentoId, event.getPagamentoId());
        assertEquals(orcamentoId, event.getOrcamentoId());
        assertEquals(ordemServicoId, event.getOrdemServicoId());
        assertEquals(clienteId, event.getClienteId());
        assertEquals(mercadoPagoPaymentId, event.getMercadoPagoPaymentId());
        assertEquals(valorPago, event.getValorPago());
        assertEquals(dataPagamento, event.getDataPagamento());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve criar evento com timestamp automático")
    void deveCriarEventoComTimestampAutomatico() {
        LocalDateTime beforeCreation = LocalDateTime.now();
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "mp-payment-123",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );
        LocalDateTime afterCreation = LocalDateTime.now();

        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(event.getTimestamp().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    @DisplayName("Deve implementar DomainEvent")
    void deveImplementarDomainEvent() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "mp-payment-123",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        assertTrue(event instanceof DomainEvent);
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter construtor padrão para deserialização")
    void deveTerConstrutorPadraoParaDeserializacao() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent();

        assertNotNull(event);
        assertNull(event.getPagamentoId());
        assertNull(event.getOrcamentoId());
        assertNull(event.getOrdemServicoId());
        assertNull(event.getClienteId());
        assertNull(event.getMercadoPagoPaymentId());
        assertNull(event.getValorPago());
        assertNull(event.getDataPagamento());
        assertNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter construtor all-args para deserialização")
    void deveTerConstrutorAllArgsParaDeserializacao() {
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String mercadoPagoPaymentId = "mp-payment-123";
        BigDecimal valorPago = new BigDecimal("100.00");
        LocalDateTime dataPagamento = LocalDateTime.now();
        LocalDateTime timestamp = LocalDateTime.now();

        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                mercadoPagoPaymentId,
                valorPago,
                dataPagamento,
                timestamp
        );

        assertEquals(pagamentoId, event.getPagamentoId());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve implementar Serializable")
    void deveImplementarSerializable() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "mp-payment-123",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        assertTrue(event instanceof java.io.Serializable);
    }



    @Test
    @DisplayName("Deve ter getters para todos os campos")
    void deveTerGettersParaTodosOsCampos() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "mp-payment-123",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        assertNotNull(event.getPagamentoId());
        assertNotNull(event.getOrcamentoId());
        assertNotNull(event.getOrdemServicoId());
        assertNotNull(event.getClienteId());
        assertNotNull(event.getMercadoPagoPaymentId());
        assertNotNull(event.getValorPago());
        assertNotNull(event.getDataPagamento());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("Deve ter toString informativo")
    void deveTerToStringInformativo() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "mp-payment-123",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        String toString = event.toString();
        assertTrue(toString.contains("PagamentoAprovadoEvent"));
        assertTrue(toString.contains("pagamentoId="));
        assertTrue(toString.contains("ordemServicoId="));
        assertTrue(toString.contains("clienteId="));
        assertTrue(toString.contains("mercadoPagoPaymentId="));
        assertTrue(toString.contains("valorPago="));
    }
}
