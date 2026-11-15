package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.domain.models.SagaContext;
import com.techchallenge.billing.infrastructure.persistence.DynamoDbPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaContextRepositoryImplTest {

    @Mock
    private DynamoDbPersistence dynamo;

    private SagaContextRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new SagaContextRepositoryImpl(dynamo);
    }

    @Test
    void savesSagaContextSuccessfully() {
        UUID sagaId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);

        SagaContext sagaContext = SagaContext.criar(
                ordemServicoId,
                pagamentoId,
                orcamentoId,
                mercadoPagoPaymentId,
                dataExpiracao
        );
        sagaContext.setStatus("INICIADA");

        when(dynamo.save(anyString(), anyString(), anyString(), any(SagaContext.class), anyMap(), anyMap()))
                .thenReturn(sagaContext);

        SagaContext result = repository.save(sagaContext);

        assertNotNull(result);
        verify(dynamo).save(
                eq("billing-saga-contexts"),
                eq("sagaId"),
                anyString(),
                eq(sagaContext),
                anyMap(),
                eq(Map.of())
        );
    }

    @Test
    void findBySagaIdReturnsSagaContextWhenExists() {
        UUID sagaId = UUID.randomUUID();
        SagaContext sagaContext = SagaContext.builder()
                .sagaId(sagaId)
                .ordemServicoId(UUID.randomUUID())
                .pagamentoId(UUID.randomUUID())
                .orcamentoId(UUID.randomUUID())
                .mercadoPagoPaymentId("ORD123")
                .status("INICIADA")
                .build();

        when(dynamo.get(eq("billing-saga-contexts"), eq("sagaId"), eq(sagaId.toString()), eq(SagaContext.class)))
                .thenReturn(Optional.of(sagaContext));

        Optional<SagaContext> result = repository.findBySagaId(sagaId);

        assertTrue(result.isPresent());
        assertEquals(sagaId, result.get().getSagaId());
    }

    @Test
    void findBySagaIdReturnsEmptyWhenNotExists() {
        UUID sagaId = UUID.randomUUID();

        when(dynamo.get(eq("billing-saga-contexts"), eq("sagaId"), eq(sagaId.toString()), eq(SagaContext.class)))
                .thenReturn(Optional.empty());

        Optional<SagaContext> result = repository.findBySagaId(sagaId);

        assertFalse(result.isPresent());
    }

    @Test
    void findAllByPagamentoIdReturnsListOfSagas() {
        UUID pagamentoId = UUID.randomUUID();
        SagaContext saga1 = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .pagamentoId(pagamentoId)
                .status("INICIADA")
                .build();
        SagaContext saga2 = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .pagamentoId(pagamentoId)
                .status("PENDENTE")
                .build();

        when(dynamo.query(
                eq("billing-saga-contexts"),
                eq("pagamentoId-index"),
                eq("pagamentoId"),
                eq(pagamentoId.toString()),
                isNull(),
                isNull(),
                isNull(),
                eq(SagaContext.class)
        )).thenReturn(List.of(saga1, saga2));

        List<SagaContext> result = repository.findAllByPagamentoId(pagamentoId);

        assertEquals(2, result.size());
        assertEquals(pagamentoId, result.get(0).getPagamentoId());
        assertEquals(pagamentoId, result.get(1).getPagamentoId());
    }

    @Test
    void findAllByPagamentoIdReturnsEmptyListWhenNoSagas() {
        UUID pagamentoId = UUID.randomUUID();

        when(dynamo.query(
                eq("billing-saga-contexts"),
                eq("pagamentoId-index"),
                eq("pagamentoId"),
                eq(pagamentoId.toString()),
                isNull(),
                isNull(),
                isNull(),
                eq(SagaContext.class)
        )).thenReturn(List.of());

        List<SagaContext> result = repository.findAllByPagamentoId(pagamentoId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByOrdemServicoIdAndStatusPendenteReturnsSagaWhenExists() {
        UUID ordemServicoId = UUID.randomUUID();
        SagaContext saga = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .ordemServicoId(ordemServicoId)
                .pagamentoId(UUID.randomUUID())
                .status("INICIADA")
                .build();

        when(dynamo.query(
                eq("billing-saga-contexts"),
                eq("ordemServicoId-status-index"),
                eq("ordemServicoId"),
                eq(ordemServicoId.toString()),
                isNull(),
                isNull(),
                isNull(),
                eq(SagaContext.class)
        )).thenReturn(List.of(saga));

        Optional<SagaContext> result = repository.findByOrdemServicoIdAndStatusPendente(ordemServicoId);

        assertTrue(result.isPresent());
        assertEquals("INICIADA", result.get().getStatus());
    }

    @Test
    void findByOrdemServicoIdAndStatusPendenteReturnsSagaWithPendenteStatus() {
        UUID ordemServicoId = UUID.randomUUID();
        SagaContext saga = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .ordemServicoId(ordemServicoId)
                .pagamentoId(UUID.randomUUID())
                .status("PENDENTE")
                .build();

        when(dynamo.query(
                eq("billing-saga-contexts"),
                eq("ordemServicoId-status-index"),
                eq("ordemServicoId"),
                eq(ordemServicoId.toString()),
                isNull(),
                isNull(),
                isNull(),
                eq(SagaContext.class)
        )).thenReturn(List.of(saga));

        Optional<SagaContext> result = repository.findByOrdemServicoIdAndStatusPendente(ordemServicoId);

        assertTrue(result.isPresent());
        assertEquals("PENDENTE", result.get().getStatus());
    }

    @Test
    void findByOrdemServicoIdAndStatusPendenteFiltersOutNonPendentStatuses() {
        UUID ordemServicoId = UUID.randomUUID();
        SagaContext saga1 = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .ordemServicoId(ordemServicoId)
                .status("APROVADA")
                .build();
        SagaContext saga2 = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .ordemServicoId(ordemServicoId)
                .status("EXPIRADA")
                .build();

        when(dynamo.query(
                eq("billing-saga-contexts"),
                eq("ordemServicoId-status-index"),
                eq("ordemServicoId"),
                eq(ordemServicoId.toString()),
                isNull(),
                isNull(),
                isNull(),
                eq(SagaContext.class)
        )).thenReturn(List.of(saga1, saga2));

        Optional<SagaContext> result = repository.findByOrdemServicoIdAndStatusPendente(ordemServicoId);

        assertFalse(result.isPresent());
    }

    @Test
    void findExpiredSagasReturnsExpiredSagas() {
        LocalDateTime now = LocalDateTime.now();
        SagaContext saga1 = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .status("INICIADA")
                .dataExpiracao(now.minusMinutes(10))
                .build();
        SagaContext saga2 = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .status("INICIADA")
                .dataExpiracao(now.minusHours(1))
                .build();

        when(dynamo.query(
                eq("billing-saga-contexts"),
                eq("status-dataExpiracao-index"),
                eq("status"),
                eq("INICIADA"),
                eq("dataExpiracao"),
                eq("<"),
                anyString(),
                eq(SagaContext.class)
        )).thenReturn(List.of(saga1, saga2));

        List<SagaContext> result = repository.findExpiredSagas(now);

        assertEquals(2, result.size());
        assertEquals("INICIADA", result.get(0).getStatus());
        assertEquals("INICIADA", result.get(1).getStatus());
    }

    @Test
    void findExpiredSagasReturnsEmptyListWhenNoExpiredSagas() {
        LocalDateTime now = LocalDateTime.now();

        when(dynamo.query(
                eq("billing-saga-contexts"),
                eq("status-dataExpiracao-index"),
                eq("status"),
                eq("INICIADA"),
                eq("dataExpiracao"),
                eq("<"),
                anyString(),
                eq(SagaContext.class)
        )).thenReturn(List.of());

        List<SagaContext> result = repository.findExpiredSagas(now);

        assertTrue(result.isEmpty());
    }

    @Test
    void saveAddsIndexAttributes() {
        UUID sagaId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);

        SagaContext sagaContext = SagaContext.builder()
                .sagaId(sagaId)
                .ordemServicoId(ordemServicoId)
                .pagamentoId(pagamentoId)
                .orcamentoId(UUID.randomUUID())
                .mercadoPagoPaymentId("ORD123")
                .status("INICIADA")
                .dataExpiracao(dataExpiracao)
                .build();

        when(dynamo.save(anyString(), anyString(), anyString(), any(SagaContext.class), anyMap(), anyMap()))
                .thenReturn(sagaContext);

        repository.save(sagaContext);

        verify(dynamo).save(
                eq("billing-saga-contexts"),
                eq("sagaId"),
                eq(sagaId.toString()),
                eq(sagaContext),
                argThat((Map<String, String> attributes) ->
                        attributes.containsKey("pagamentoId") &&
                        pagamentoId.toString().equals(attributes.get("pagamentoId")) &&
                        attributes.containsKey("ordemServicoId") &&
                        ordemServicoId.toString().equals(attributes.get("ordemServicoId")) &&
                        attributes.containsKey("status") &&
                        "INICIADA".equals(attributes.get("status")) &&
                        attributes.containsKey("dataExpiracao")
                ),
                eq(Map.of())
        );
    }

    @Test
    void saveHandlesNullIndexAttributes() {
        UUID sagaId = UUID.randomUUID();
        SagaContext sagaContext = SagaContext.builder()
                .sagaId(sagaId)
                .ordemServicoId(UUID.randomUUID())
                .pagamentoId(UUID.randomUUID())
                .orcamentoId(UUID.randomUUID())
                .mercadoPagoPaymentId("ORD123")
                .status("INICIADA")
                .dataExpiracao(null)
                .build();

        when(dynamo.save(anyString(), anyString(), anyString(), any(SagaContext.class), anyMap(), anyMap()))
                .thenReturn(sagaContext);

        repository.save(sagaContext);

        verify(dynamo).save(
                eq("billing-saga-contexts"),
                eq("sagaId"),
                eq(sagaId.toString()),
                eq(sagaContext),
                argThat((Map<String, String> attributes) -> !attributes.containsKey("dataExpiracao")),
                eq(Map.of())
        );
    }
}
