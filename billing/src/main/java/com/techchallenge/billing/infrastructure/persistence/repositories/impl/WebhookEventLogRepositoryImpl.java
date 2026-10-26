package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.application.gateway.ports.output.WebhookEventLogGateway;
import com.techchallenge.billing.domain.models.WebhookEventLog;
import com.techchallenge.billing.infrastructure.persistence.mappers.WebhookEventLogMongoMapper;
import com.techchallenge.billing.infrastructure.persistence.repositories.WebhookEventLogMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WebhookEventLogRepositoryImpl implements WebhookEventLogGateway {

    private static final String PENDING_RETRY_STATUS = "PENDENTE_RETRY";

    private final WebhookEventLogMongoRepository repository;
    private final WebhookEventLogMongoMapper mapper;

    @Override
    public WebhookEventLog save(WebhookEventLog eventLog) {
        return mapper.toDomain(repository.save(mapper.toDocument(eventLog)));
    }

    @Override
    public Optional<WebhookEventLog> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<WebhookEventLog> findByMercadoPagoPaymentId(String mercadoPagoPaymentId) {
        return repository.findAllByMercadoPagoPaymentId(mercadoPagoPaymentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<WebhookEventLog> findPendingRetriesDueAt(LocalDateTime timestamp) {
        return repository.findAllByStatusProcessamentoAndProximaRetentativaLessThanEqual(
                        PENDING_RETRY_STATUS,
                        timestamp
                ).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
