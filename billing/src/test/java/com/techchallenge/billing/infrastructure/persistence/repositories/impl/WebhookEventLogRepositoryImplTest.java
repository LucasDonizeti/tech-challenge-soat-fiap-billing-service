package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.domain.models.WebhookEventLog;
import com.techchallenge.billing.infrastructure.persistence.DynamoDbPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookEventLogRepositoryImplTest {

    @Mock
    private DynamoDbPersistence dynamo;

    private WebhookEventLogRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new WebhookEventLogRepositoryImpl(dynamo);
    }

    @Test
    void savesWebhookEventLogSuccessfully() {
        UUID id = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", "12345");

        WebhookEventLog eventLog = new WebhookEventLog(
                id,
                mercadoPagoPaymentId,
                "payment.created",
                LocalDateTime.now(),
                payload,
                "PENDENTE",
                0,
                null
        );

        when(dynamo.save(anyString(), anyString(), anyString(), any(WebhookEventLog.class), anyMap(), anyMap()))
                .thenReturn(eventLog);

        WebhookEventLog result = repository.save(eventLog);

        assertNotNull(result);
        verify(dynamo).save(
                eq("billing-webhook-event-log"),
                eq("id"),
                eq(id.toString()),
                eq(eventLog),
                anyMap(),
                eq(Map.of())
        );
    }

    @Test
    void saveAddsIndexAttributes() {
        UUID id = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> payload = new HashMap<>();

        WebhookEventLog eventLog = new WebhookEventLog(
                id,
                mercadoPagoPaymentId,
                "payment.created",
                now,
                payload,
                "PENDENTE",
                0,
                null
        );

        when(dynamo.save(anyString(), anyString(), anyString(), any(WebhookEventLog.class), anyMap(), anyMap()))
                .thenReturn(eventLog);

        repository.save(eventLog);

        verify(dynamo).save(
                eq("billing-webhook-event-log"),
                eq("id"),
                eq(id.toString()),
                eq(eventLog),
                argThat((Map<String, String> attributes) ->
                        attributes.containsKey("mercadoPagoPaymentId") &&
                        mercadoPagoPaymentId.equals(attributes.get("mercadoPagoPaymentId")) &&
                        attributes.containsKey("statusProcessamento") &&
                        "PENDENTE".equals(attributes.get("statusProcessamento"))
                ),
                eq(Map.of())
        );
    }

    @Test
    void saveDoesNotAddNullIndexAttributes() {
        UUID id = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();

        WebhookEventLog eventLog = new WebhookEventLog(
                id,
                null,
                "payment.created",
                LocalDateTime.now(),
                payload,
                "PENDENTE",
                0,
                null
        );

        when(dynamo.save(anyString(), anyString(), anyString(), any(WebhookEventLog.class), anyMap(), anyMap()))
                .thenReturn(eventLog);

        repository.save(eventLog);

        verify(dynamo).save(
                eq("billing-webhook-event-log"),
                eq("id"),
                eq(id.toString()),
                eq(eventLog),
                argThat((Map<String, String> attributes) -> !attributes.containsKey("mercadoPagoPaymentId")),
                eq(Map.of())
        );
    }

    @Test
    void findByIdReturnsEventLogWhenExists() {
        UUID id = UUID.randomUUID();
        WebhookEventLog eventLog = new WebhookEventLog(
                id,
                "ORD123",
                "payment.created",
                LocalDateTime.now(),
                new HashMap<>(),
                "PENDENTE",
                0,
                null
        );

        when(dynamo.get(eq("billing-webhook-event-log"), eq("id"), eq(id.toString()), eq(WebhookEventLog.class)))
                .thenReturn(Optional.of(eventLog));

        Optional<WebhookEventLog> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().id());
    }

    @Test
    void findByIdReturnsEmptyWhenNotExists() {
        UUID id = UUID.randomUUID();

        when(dynamo.get(eq("billing-webhook-event-log"), eq("id"), eq(id.toString()), eq(WebhookEventLog.class)))
                .thenReturn(Optional.empty());

        Optional<WebhookEventLog> result = repository.findById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void findByMercadoPagoPaymentIdReturnsListOfEventLogs() {
        String mercadoPagoPaymentId = "ORD123";
        WebhookEventLog event1 = new WebhookEventLog(
                UUID.randomUUID(),
                mercadoPagoPaymentId,
                "payment.created",
                LocalDateTime.now(),
                new HashMap<>(),
                "PENDENTE",
                0,
                null
        );
        WebhookEventLog event2 = new WebhookEventLog(
                UUID.randomUUID(),
                mercadoPagoPaymentId,
                "payment.updated",
                LocalDateTime.now(),
                new HashMap<>(),
                "PROCESSADO",
                1,
                null
        );

        when(dynamo.query(
                eq("billing-webhook-event-log"),
                eq("mercadoPagoPaymentId-index"),
                eq("mercadoPagoPaymentId"),
                eq(mercadoPagoPaymentId),
                isNull(),
                isNull(),
                isNull(),
                eq(WebhookEventLog.class)
        )).thenReturn(List.of(event1, event2));

        List<WebhookEventLog> result = repository.findByMercadoPagoPaymentId(mercadoPagoPaymentId);

        assertEquals(2, result.size());
        assertEquals(mercadoPagoPaymentId, result.get(0).mercadoPagoPaymentId());
        assertEquals(mercadoPagoPaymentId, result.get(1).mercadoPagoPaymentId());
    }

    @Test
    void findByMercadoPagoPaymentIdReturnsEmptyListWhenNoEvents() {
        String mercadoPagoPaymentId = "ORD123";

        when(dynamo.query(
                eq("billing-webhook-event-log"),
                eq("mercadoPagoPaymentId-index"),
                eq("mercadoPagoPaymentId"),
                eq(mercadoPagoPaymentId),
                isNull(),
                isNull(),
                isNull(),
                eq(WebhookEventLog.class)
        )).thenReturn(List.of());

        List<WebhookEventLog> result = repository.findByMercadoPagoPaymentId(mercadoPagoPaymentId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findPendingRetriesDueAtReturnsEventsDueForRetry() {
        LocalDateTime now = LocalDateTime.now();
        WebhookEventLog event1 = new WebhookEventLog(
                UUID.randomUUID(),
                "ORD123",
                "payment.created",
                LocalDateTime.now(),
                new HashMap<>(),
                "PENDENTE_RETRY",
                1,
                now.minusMinutes(5)
        );
        WebhookEventLog event2 = new WebhookEventLog(
                UUID.randomUUID(),
                "ORD456",
                "payment.updated",
                LocalDateTime.now(),
                new HashMap<>(),
                "PENDENTE_RETRY",
                2,
                now.minusMinutes(10)
        );

        when(dynamo.query(
                eq("billing-webhook-event-log"),
                eq("status-proximaRetentativa-index"),
                eq("statusProcessamento"),
                eq("PENDENTE_RETRY"),
                eq("proximaRetentativa"),
                eq("<="),
                anyString(),
                eq(WebhookEventLog.class)
        )).thenReturn(List.of(event1, event2));

        List<WebhookEventLog> result = repository.findPendingRetriesDueAt(now);

        assertEquals(2, result.size());
        assertEquals("PENDENTE_RETRY", result.get(0).statusProcessamento());
        assertEquals("PENDENTE_RETRY", result.get(1).statusProcessamento());
    }

    @Test
    void findPendingRetriesDueAtReturnsEmptyListWhenNoPendingRetries() {
        LocalDateTime now = LocalDateTime.now();

        when(dynamo.query(
                eq("billing-webhook-event-log"),
                eq("status-proximaRetentativa-index"),
                eq("statusProcessamento"),
                eq("PENDENTE_RETRY"),
                eq("proximaRetentativa"),
                eq("<="),
                anyString(),
                eq(WebhookEventLog.class)
        )).thenReturn(List.of());

        List<WebhookEventLog> result = repository.findPendingRetriesDueAt(now);

        assertTrue(result.isEmpty());
    }

    @Test
    void saveFormatsLocalDateTimeForIndex() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 30, 45, 123456789);
        Map<String, Object> payload = new HashMap<>();

        WebhookEventLog eventLog = new WebhookEventLog(
                id,
                "ORD123",
                "payment.created",
                now,
                payload,
                "PENDENTE",
                0,
                now.plusMinutes(5)
        );

        when(dynamo.save(anyString(), anyString(), anyString(), any(WebhookEventLog.class), anyMap(), anyMap()))
                .thenReturn(eventLog);

        repository.save(eventLog);

        verify(dynamo).save(
                eq("billing-webhook-event-log"),
                eq("id"),
                eq(id.toString()),
                eq(eventLog),
                argThat((Map<String, String> attributes) ->
                        attributes.containsKey("proximaRetentativa") &&
                        attributes.get("proximaRetentativa").contains("2024-01-15")
                ),
                eq(Map.of())
        );
    }
}
