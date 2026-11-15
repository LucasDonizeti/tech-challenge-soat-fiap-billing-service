package com.techchallenge.billing.application.gateway.ports.output.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MercadoPagoPaymentResult(
        String paymentId,
        String status,
        BigDecimal amount,
        String externalReference,
        LocalDateTime dateApproved,
        String failureCode,
        String failureMessage,
        String qrCode,
        String qrCodeBase64
) {
}
