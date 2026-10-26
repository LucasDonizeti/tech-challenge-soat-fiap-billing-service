package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.domain.models.WebhookEventLog;
import com.techchallenge.billing.infrastructure.persistence.mappers.WebhookEventLogMongoMapper;
import com.techchallenge.billing.infrastructure.persistence.repositories.WebhookEventLogMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - WebhookEventLogRepositoryImpl")
class WebhookEventLogRepositoryImplTest {

    @Mock
    private WebhookEventLogMongoRepository repository;

    @Mock
    private WebhookEventLogMongoMapper mapper;

    @Mock
    private WebhookEventLog domainEventLog;

    @Mock
    private com.techchallenge.billing.infrastructure.persistence.documents.WebhookEventLogDocument document;

    private WebhookEventLogRepositoryImpl repositoryImpl;

    @BeforeEach
    void setUp() {
        repositoryImpl = new WebhookEventLogRepositoryImpl(repository, mapper);
    }

    @Test
    @DisplayName("Deve salvar webhook event log")
    void deveSalvarWebhookEventLog() {
        when(mapper.toDocument(domainEventLog)).thenReturn(document);
        when(repository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(domainEventLog);

        WebhookEventLog result = repositoryImpl.save(domainEventLog);

        assertEquals(domainEventLog, result);
        verify(mapper).toDocument(domainEventLog);
        verify(repository).save(document);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar webhook event log por ID")
    void deveEncontrarWebhookEventLogPorId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(domainEventLog);

        Optional<WebhookEventLog> result = repositoryImpl.findById(id);

        assertTrue(result.isPresent());
        assertEquals(domainEventLog, result.get());
        verify(repository).findById(id);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve retornar vazio quando webhook event log não encontrado por ID")
    void deveRetornarVazioQuandoWebhookEventLogNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<WebhookEventLog> result = repositoryImpl.findById(id);

        assertFalse(result.isPresent());
        verify(repository).findById(id);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Deve encontrar webhook event logs por mercadoPagoPaymentId")
    void deveEncontrarWebhookEventLogsPorMercadoPagoPaymentId() {
        String mercadoPagoPaymentId = "mp-payment-123";
        when(repository.findAllByMercadoPagoPaymentId(mercadoPagoPaymentId)).thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(domainEventLog);

        List<WebhookEventLog> result = repositoryImpl.findByMercadoPagoPaymentId(mercadoPagoPaymentId);

        assertEquals(1, result.size());
        assertEquals(domainEventLog, result.get(0));
        verify(repository).findAllByMercadoPagoPaymentId(mercadoPagoPaymentId);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar webhook event logs com pending retry status e timestamp")
    void deveEncontrarWebhookEventLogsWithPendingRetryStatusETimestamp() {
        LocalDateTime timestamp = LocalDateTime.now();
        when(repository.findAllByStatusProcessamentoAndProximaRetentativaLessThanEqual(
                "PENDENTE_RETRY", timestamp)).thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(domainEventLog);

        List<WebhookEventLog> result = repositoryImpl.findPendingRetriesDueAt(timestamp);

        assertEquals(1, result.size());
        assertEquals(domainEventLog, result.get(0));
        verify(repository).findAllByStatusProcessamentoAndProximaRetentativaLessThanEqual(
                "PENDENTE_RETRY", timestamp);
        verify(mapper).toDomain(document);
    }
}
