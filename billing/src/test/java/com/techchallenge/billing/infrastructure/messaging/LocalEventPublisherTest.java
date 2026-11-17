package com.techchallenge.billing.infrastructure.messaging;

import com.techchallenge.billing.domain.events.CobrancaPixGeradaEvent;
import com.techchallenge.billing.domain.events.PagamentoAprovadoEvent;
import com.techchallenge.billing.domain.events.PagamentoRecusadoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - LocalEventPublisher")
class LocalEventPublisherTest {

    private LocalEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new LocalEventPublisher();
    }

    @Test
    @DisplayName("Deve publicar CobrancaPixGeradaEvent sem lançar exceção")
    void devePublicarCobrancaPixGeradaEventSemLancarExcecao() {
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code",
                null,
                new BigDecimal("100.00"),
                LocalDateTime.now().plusMinutes(30),
                "os-reference"
        );

        assertDoesNotThrow(() -> publisher.publishCobrancaPixGerada(event));
    }

    @Test
    @DisplayName("Deve publicar PagamentoAprovadoEvent sem lançar exceção")
    void devePublicarPagamentoAprovadoEventSemLancarExcecao() {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "mp-payment-123",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> publisher.publishPagamentoAprovado(event));
    }

    @Test
    @DisplayName("Deve publicar PagamentoRecusadoEvent sem lançar exceção")
    void devePublicarPagamentoRecusadoEventSemLancarExcecao() {
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Cartão inválido",
                "rejected"
        );

        assertDoesNotThrow(() -> publisher.publishPagamentoRecusado(event));
    }

    @Test
    @DisplayName("Deve publicar eventos com QR code nulo")
    void devePublicarEventosComQRCodeNulo() {
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                new BigDecimal("100.00"),
                LocalDateTime.now().plusMinutes(30),
                "os-reference"
        );

        assertDoesNotThrow(() -> publisher.publishCobrancaPixGerada(event));
    }

    @Test
    @DisplayName("Deve publicar eventos com motivo nulo")
    void devePublicarEventosComMotivoNulo() {
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "rejected"
        );

        assertDoesNotThrow(() -> publisher.publishPagamentoRecusado(event));
    }
}
