package com.techchallenge.billing.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MercadoPagoWebhookNotification(
        String action,
        @JsonProperty("data_id") String dataId,
        @JsonProperty("created_at") Long createdAt,
        String type,
        Data data
) {

    public String paymentId() {
        if (dataId != null && !dataId.isBlank()) {
            return dataId;
        }
        return data == null ? null : data.id();
    }

    public record Data(String id) {
    }
}
