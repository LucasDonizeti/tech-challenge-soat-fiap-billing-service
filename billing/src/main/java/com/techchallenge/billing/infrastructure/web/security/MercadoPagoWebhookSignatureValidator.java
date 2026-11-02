package com.techchallenge.billing.infrastructure.web.security;

import com.techchallenge.billing.infrastructure.external.dto.MercadoPagoWebhookNotification;
import com.mercadopago.exceptions.MPInvalidWebhookSignatureException;
import com.mercadopago.webhook.WebhookSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;

@Component
public class MercadoPagoWebhookSignatureValidator {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoWebhookSignatureValidator.class);
    private static final Duration SIGNATURE_TOLERANCE = Duration.ofMinutes(5);
    private static final Set<String> BYPASS_ALLOWED_PROFILES = Set.of("local", "dev", "development", "test");

    private final String secret;
    private final boolean skipSignatureValidation;

    @Autowired
    public MercadoPagoWebhookSignatureValidator(
            @Value("${mercadopago.webhook-secret:}") String secret,
            @Value("${mercadopago.skip-signature-validation:false}") boolean skipSignatureValidation,
            Environment environment
    ) {
        this(secret, skipSignatureValidation, true);
    }

    MercadoPagoWebhookSignatureValidator(String secret) {
        this(secret, false, false);
    }

    MercadoPagoWebhookSignatureValidator(
            String secret,
            boolean skipSignatureValidation,
            boolean bypassAllowed
    ) {
        if (skipSignatureValidation && !bypassAllowed) {
            throw new IllegalStateException(
                    "mercadopago.skip-signature-validation só pode ser ativado nos perfis local, dev, development ou test."
            );
        }

        this.secret = secret;
        this.skipSignatureValidation = skipSignatureValidation;

        if (skipSignatureValidation) {
            logger.warn("Validação de assinatura do Mercado Pago desativada para ambiente de desenvolvimento/teste.");
        }
    }

    public boolean isValid(
            MercadoPagoWebhookNotification notification,
            String signatureHeader,
            String requestId
    ) {
        return isValid(
                notification,
                notification == null ? null : notification.paymentId(),
                signatureHeader,
                requestId
        );
    }

    public boolean isValid(
            MercadoPagoWebhookNotification notification,
            String signedDataId,
            String signatureHeader,
            String requestId
    ) {
        if (skipSignatureValidation) {
            return true;
        }

        if (secret == null || secret.isBlank()
                || notification == null
                || signedDataId == null
                || signedDataId.isBlank()
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
                    signedDataId,
                    secret,
                    SIGNATURE_TOLERANCE
            );
            return true;
        } catch (MPInvalidWebhookSignatureException exception) {
            return false;
        }
    }
}
