package com.techchallenge.billing.domain.models;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record WebhookEventLog(
        UUID id,
        String mercadoPagoPaymentId,
        String action,
        LocalDateTime dataRecebimento,
        Map<String, Object> payloadOriginal,
        String statusProcessamento,
        int tentativas,
        LocalDateTime proximaRetentativa
) {
}
