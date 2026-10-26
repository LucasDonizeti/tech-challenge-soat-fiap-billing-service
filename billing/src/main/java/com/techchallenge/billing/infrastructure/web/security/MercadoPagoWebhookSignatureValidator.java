package com.techchallenge.billing.infrastructure.web.security;

import com.techchallenge.billing.infrastructure.external.dto.MercadoPagoWebhookNotification;
import com.mercadopago.exceptions.MPInvalidWebhookSignatureException;
import com.mercadopago.webhook.WebhookSignatureValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MercadoPagoWebhookSignatureValidator {

    private static final Duration SIGNATURE_TOLERANCE = Duration.ofMinutes(5);

    private final String secret;

    public MercadoPagoWebhookSignatureValidator(
            @Value("${mercadopago.webhook-secret:}") String secret
    ) {
        this.secret = secret;
    }

    public boolean isValid(
            MercadoPagoWebhookNotification notification,
            String signatureHeader,
            String requestId
    ) {
        if (secret == null || secret.isBlank()
                || notification == null
                || notification.paymentId() == null
                || notification.paymentId().isBlank()
                || signatureHeader == null
                || signatureHeader.isBlank()
                || requestId == null
                || requestId.isBlank()) {
            return false;
        }

        try {
            WebhookSignatureValidator.validate(
                    signatureHeader,
                    requestId,
                    notification.paymentId(),
                    secret,
                    SIGNATURE_TOLERANCE
            );
            return true;
        } catch (MPInvalidWebhookSignatureException exception) {
            return false;
        }
    }
}
