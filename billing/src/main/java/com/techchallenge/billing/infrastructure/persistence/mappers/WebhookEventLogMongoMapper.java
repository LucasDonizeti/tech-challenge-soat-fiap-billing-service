package com.techchallenge.billing.infrastructure.persistence.mappers;

import com.techchallenge.billing.domain.models.WebhookEventLog;
import com.techchallenge.billing.infrastructure.persistence.documents.WebhookEventLogDocument;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class WebhookEventLogMongoMapper {

    public WebhookEventLogDocument toDocument(WebhookEventLog eventLog) {
        Objects.requireNonNull(eventLog, "eventLog must not be null");

        return new WebhookEventLogDocument(
                eventLog.id(),
                eventLog.mercadoPagoPaymentId(),
                eventLog.action(),
                eventLog.dataRecebimento(),
                copyPayload(eventLog.payloadOriginal()),
                eventLog.statusProcessamento(),
                eventLog.tentativas(),
                eventLog.proximaRetentativa()
        );
    }

    public WebhookEventLog toDomain(WebhookEventLogDocument document) {
        Objects.requireNonNull(document, "document must not be null");

        return new WebhookEventLog(
                document.getId(),
                document.getMercadoPagoPaymentId(),
                document.getAction(),
                document.getDataRecebimento(),
                copyPayload(document.getPayloadOriginal()),
                document.getStatusProcessamento(),
                document.getTentativas(),
                document.getProximaRetentativa()
        );
    }

    private Map<String, Object> copyPayload(Map<String, Object> payload) {
        return payload == null ? null : new LinkedHashMap<>(payload);
    }
}
