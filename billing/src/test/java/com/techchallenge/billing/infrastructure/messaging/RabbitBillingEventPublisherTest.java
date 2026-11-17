package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.domain.events.PagamentoAprovadoEvent;
import com.techchallenge.billing.domain.events.PagamentoRecusadoEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitBillingEventPublisherTest {

    @Test
    void publishesPaymentEventToConfiguredExchangeAndRoute() {
        var rabbitTemplate = mock(RabbitTemplate.class);
        var publisher = new RabbitBillingEventPublisher(rabbitTemplate, new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(publisher, "exchange", "billing.events");
        ReflectionTestUtils.setField(publisher, "pagamentoAprovadoRoutingKey", "pagamento.aprovado");
        var event = new PagamentoAprovadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "payment-123",
                new BigDecimal("25.50"),
                LocalDateTime.now()
        );

        publisher.publishPagamentoAprovado(event);

        verify(rabbitTemplate).convertAndSend(
                eq("billing.events"),
                eq("pagamento.aprovado"),
                eq(toJson(event))
        );
    }

    @Test
    void publishesRejectedPaymentEventToConfiguredExchangeAndRoute() {
        var rabbitTemplate = mock(RabbitTemplate.class);
        var publisher = new RabbitBillingEventPublisher(rabbitTemplate, new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(publisher, "exchange", "billing.events");
        ReflectionTestUtils.setField(publisher, "pagamentoRecusadoRoutingKey", "pagamento.recusado");
        var event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Pagamento recusado",
                "rejected"
        );

        publisher.publishPagamentoRecusado(event);

        verify(rabbitTemplate).convertAndSend(
                eq("billing.events"),
                eq("pagamento.recusado"),
                eq(toJson(event))
        );
    }

    private String toJson(Object event) {
        try {
            return new ObjectMapper().findAndRegisterModules().writeValueAsString(event);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
