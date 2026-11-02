package com.techchallenge.billing.application.gateway.ports.output.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Command para criar um pagamento PIX no Mercado Pago via Orders API.
 * Este comando encapsula todos os dados necessários para criar uma order com payment PIX.
 */
public record CreatePixPaymentCommand(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String description,
        @NotBlank @Email String payerEmail,
        @NotBlank String payerIdentificationType,
        @NotBlank String payerIdentificationNumber,
        @NotBlank String externalReference,
        @NotBlank String notificationUrl
) {
}
