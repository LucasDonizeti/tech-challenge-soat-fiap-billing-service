package com.techchallenge.billing.application.gateway.ports.output.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

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
