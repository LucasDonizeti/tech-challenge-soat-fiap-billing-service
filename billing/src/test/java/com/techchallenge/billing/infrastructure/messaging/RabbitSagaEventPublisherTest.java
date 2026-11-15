package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.domain.events.SagaPaymentInitiatedEvent;
import com.techchallenge.billing.domain.events.SagaPaymentExpiredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitSagaEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private RabbitSagaEventPublisher publisher;

    @Test
    void publishesSagaPaymentInitiatedEventSuccessfully() throws JsonProcessingException {
        publisher = new RabbitSagaEventPublisher(rabbitTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "exchange", "billing.events");
        ReflectionTestUtils.setField(publisher, "sagaInitiatedRoutingKey", "saga.payment.initiated");

        UUID sagaId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        String qrCodePix = "qr-code-data";
        String qrCodeBase64 = "base64-data";
        BigDecimal valorCobrado = new BigDecimal("500.00");
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);

        SagaPaymentInitiatedEvent event = new SagaPaymentInitiatedEvent(
                sagaId,
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                mercadoPagoPaymentId,
                qrCodePix,
                qrCodeBase64,
                valorCobrado,
                dataExpiracao
        );

        String jsonMessage = "{\"sagaId\":\"" + sagaId + "\"}";
        when(objectMapper.writeValueAsString(event)).thenReturn(jsonMessage);

        publisher.publishSagaPaymentInitiated(event);

        verify(rabbitTemplate).convertAndSend(
                eq("billing.events"),
                eq("saga.payment.initiated"),
                eq(jsonMessage)
        );
    }

    @Test
    void throwsRuntimeExceptionWhenJsonSerializationFailsForInitiatedEvent() throws JsonProcessingException {
        publisher = new RabbitSagaEventPublisher(rabbitTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "exchange", "billing.events");
        ReflectionTestUtils.setField(publisher, "sagaInitiatedRoutingKey", "saga.payment.initiated");

        SagaPaymentInitiatedEvent event = new SagaPaymentInitiatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD123",
                "qr-code",
                "base64",
                new BigDecimal("500.00"),
                LocalDateTime.now()
        );

        when(objectMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                publisher.publishSagaPaymentInitiated(event)
        );

        assertTrue(exception.getMessage().contains("Falha ao publicar evento de saga iniciada"));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void publishesSagaPaymentExpiredEventSuccessfully() throws JsonProcessingException {
        publisher = new RabbitSagaEventPublisher(rabbitTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "exchange", "billing.events");
        ReflectionTestUtils.setField(publisher, "sagaExpiredRoutingKey", "saga.payment.expired");

        UUID sagaId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        LocalDateTime dataExpiracao = LocalDateTime.now();

        SagaPaymentExpiredEvent event = new SagaPaymentExpiredEvent(
                sagaId,
                pagamentoId,
                ordemServicoId,
                mercadoPagoPaymentId,
                dataExpiracao
        );

        String jsonMessage = "{\"sagaId\":\"" + sagaId + "\"}";
        when(objectMapper.writeValueAsString(event)).thenReturn(jsonMessage);

        publisher.publishSagaPaymentExpired(event);

        verify(rabbitTemplate).convertAndSend(
                eq("billing.events"),
                eq("saga.payment.expired"),
                eq(jsonMessage)
        );
    }

    @Test
    void throwsRuntimeExceptionWhenJsonSerializationFailsForExpiredEvent() throws JsonProcessingException {
        publisher = new RabbitSagaEventPublisher(rabbitTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "exchange", "billing.events");
        ReflectionTestUtils.setField(publisher, "sagaExpiredRoutingKey", "saga.payment.expired");

        SagaPaymentExpiredEvent event = new SagaPaymentExpiredEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD123",
                LocalDateTime.now()
        );

        when(objectMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                publisher.publishSagaPaymentExpired(event)
        );

        assertTrue(exception.getMessage().contains("Falha ao publicar evento de saga expirada"));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void usesDefaultExchangeWhenNotConfigured() throws JsonProcessingException {
        publisher = new RabbitSagaEventPublisher(rabbitTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "exchange", "billing.events");
        ReflectionTestUtils.setField(publisher, "sagaInitiatedRoutingKey", "saga.payment.initiated");

        SagaPaymentInitiatedEvent event = new SagaPaymentInitiatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD123",
                "qr-code",
                "base64",
                new BigDecimal("500.00"),
                LocalDateTime.now()
        );

        when(objectMapper.writeValueAsString(event)).thenReturn("{}");

        publisher.publishSagaPaymentInitiated(event);

        verify(rabbitTemplate).convertAndSend(eq("billing.events"), anyString(), anyString());
    }

    @Test
    void usesDefaultRoutingKeysWhenNotConfigured() throws JsonProcessingException {
        publisher = new RabbitSagaEventPublisher(rabbitTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "exchange", "billing.events");
        ReflectionTestUtils.setField(publisher, "sagaInitiatedRoutingKey", "saga.payment.initiated");
        ReflectionTestUtils.setField(publisher, "sagaExpiredRoutingKey", "saga.payment.expired");

        SagaPaymentInitiatedEvent initiatedEvent = new SagaPaymentInitiatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD123",
                "qr-code",
                "base64",
                new BigDecimal("500.00"),
                LocalDateTime.now()
        );

        when(objectMapper.writeValueAsString(initiatedEvent)).thenReturn("{}");

        publisher.publishSagaPaymentInitiated(initiatedEvent);

        verify(rabbitTemplate).convertAndSend(
                eq("billing.events"),
                eq("saga.payment.initiated"),
                anyString()
        );
    }
}
