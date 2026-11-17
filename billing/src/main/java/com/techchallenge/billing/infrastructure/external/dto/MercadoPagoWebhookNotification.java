package com.techchallenge.billing.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoWebhookNotification(
        String action,
        @JsonProperty("data_id") String dataId,
        @JsonProperty("created_at") Long createdAt,
        @JsonProperty("date_created") String dateCreated,
        String type,
        Data data,
        @JsonProperty("api_version") String apiVersion,
        Long id,
        @JsonProperty("live_mode") Boolean liveMode,
        @JsonProperty("user_id") String userId
) {

    /**
     * Retorna o ID do payment (compatível com Payments API).
     * Primeiro tenta dataId (campo legado), depois pega do data.id (campo atual).
     */
    public String paymentId() {
        // Primeiro tenta dataId (campo legado)
        if (dataId != null && !dataId.isBlank()) {
            return dataId;
        }
        // Se não tiver, pega do data.id (campo atual)
        return data == null ? null : data.id();
    }

    /**
     * Retorna o ID da order (compatível com Orders API).
     * Na Orders API, o ID da order vem em data.id ou no campo id raiz.
     */
    public String orderId() {
        // Primeiro tenta o ID da order em data.id
        if (data != null && data.id() != null && !data.id().isBlank()) {
            return data.id();
        }
        // Fallback: usa o ID raiz se disponível
        return id == null ? null : id.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String id) {
    }
}
