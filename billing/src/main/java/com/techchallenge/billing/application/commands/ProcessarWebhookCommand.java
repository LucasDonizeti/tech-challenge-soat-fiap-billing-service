package com.techchallenge.billing.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ProcessarWebhookCommand(
        @NotBlank String mercadoPagoPaymentId,
        @NotBlank String action,
        @NotNull LocalDateTime dataRecebimento,
        @NotNull Map<String, Object> payloadOriginal
) {
}
