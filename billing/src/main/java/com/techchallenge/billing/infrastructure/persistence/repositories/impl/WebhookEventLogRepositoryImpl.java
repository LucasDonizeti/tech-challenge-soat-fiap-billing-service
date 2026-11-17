package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.application.gateway.ports.output.WebhookEventLogGateway;
import com.techchallenge.billing.domain.models.WebhookEventLog;
import com.techchallenge.billing.infrastructure.persistence.DynamoDbPersistence;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class WebhookEventLogRepositoryImpl implements WebhookEventLogGateway {

    private static final String TABLE = "billing-webhook-event-log";
    private static final String RETRY_STATUS = "PENDENTE_RETRY";
    private static final DateTimeFormatter INDEX_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
    private final DynamoDbPersistence dynamo;

    public WebhookEventLogRepositoryImpl(DynamoDbPersistence dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public WebhookEventLog save(WebhookEventLog eventLog) {
        var indexAttributes = new LinkedHashMap<String, String>();
        add(indexAttributes, "mercadoPagoPaymentId", eventLog.mercadoPagoPaymentId());
        add(indexAttributes, "statusProcessamento", eventLog.statusProcessamento());
        add(indexAttributes, "proximaRetentativa", eventLog.proximaRetentativa());
        return dynamo.save(TABLE, "id", eventLog.id().toString(), eventLog,
                indexAttributes, Map.of());
    }

    @Override
    public Optional<WebhookEventLog> findById(UUID id) {
        return dynamo.get(TABLE, "id", id.toString(), WebhookEventLog.class);
    }

    @Override
    public List<WebhookEventLog> findByMercadoPagoPaymentId(String mercadoPagoPaymentId) {
        return dynamo.query(TABLE, "mercadoPagoPaymentId-index", "mercadoPagoPaymentId",
                mercadoPagoPaymentId, null, null, null, WebhookEventLog.class);
    }

    @Override
    public List<WebhookEventLog> findPendingRetriesDueAt(LocalDateTime timestamp) {
        return dynamo.query(TABLE, "status-proximaRetentativa-index", "statusProcessamento",
                RETRY_STATUS, "proximaRetentativa", "<=", indexDate(timestamp), WebhookEventLog.class);
    }

    private static void add(Map<String, String> attributes, String name, Object value) {
        if (value != null) {
            attributes.put(name, value instanceof LocalDateTime date ? indexDate(date) : value.toString());
        }
    }

    private static String indexDate(LocalDateTime date) {
        return date.format(INDEX_DATE_FORMAT);
    }
}
