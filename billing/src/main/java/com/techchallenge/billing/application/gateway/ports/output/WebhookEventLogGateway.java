package com.techchallenge.billing.application.gateway.ports.output;

import com.techchallenge.billing.domain.models.WebhookEventLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookEventLogGateway {

    WebhookEventLog save(WebhookEventLog eventLog);

    Optional<WebhookEventLog> findById(UUID id);

    List<WebhookEventLog> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);

    List<WebhookEventLog> findPendingRetriesDueAt(LocalDateTime timestamp);
}
