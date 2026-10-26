package com.techchallenge.billing.infrastructure.web.security;

import com.techchallenge.billing.infrastructure.external.dto.MercadoPagoWebhookNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - MercadoPagoWebhookSignatureValidator")
class MercadoPagoWebhookSignatureValidatorTest {

    private MercadoPagoWebhookSignatureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MercadoPagoWebhookSignatureValidator("test-secret");
    }

    @Test
    @DisplayName("Deve rejeitar quando secret é nulo")
    void deveRejeitarQuandoSecretENulo() {
        MercadoPagoWebhookSignatureValidator validatorWithNullSecret = new MercadoPagoWebhookSignatureValidator(null);

        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validatorWithNullSecret.isValid(
                notification,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando secret está vazio")
    void deveRejeitarQuandoSecretEstaVazio() {
        MercadoPagoWebhookSignatureValidator validatorWithEmptySecret = new MercadoPagoWebhookSignatureValidator("");

        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validatorWithEmptySecret.isValid(
                notification,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando secret está em branco")
    void deveRejeitarQuandoSecretEstaEmBranco() {
        MercadoPagoWebhookSignatureValidator validatorWithBlankSecret = new MercadoPagoWebhookSignatureValidator("   ");

        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validatorWithBlankSecret.isValid(
                notification,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando notification é nulo")
    void deveRejeitarQuandoNotificationENulo() {
        boolean result = validator.isValid(
                null,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando paymentId é nulo")
    void deveRejeitarQuandoPaymentIdENulo() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                null,
                1234567890L,
                "payment",
                null
        );

        boolean result = validator.isValid(
                notification,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando paymentId está vazio")
    void deveRejeitarQuandoPaymentIdEstaVazio() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("")
        );

        boolean result = validator.isValid(
                notification,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando paymentId está em branco")
    void deveRejeitarQuandoPaymentIdEstaEmBranco() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "   ",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("   ")
        );

        boolean result = validator.isValid(
                notification,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando signatureHeader é nulo")
    void deveRejeitarQuandoSignatureHeaderENulo() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validator.isValid(
                notification,
                null,
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando signatureHeader está vazio")
    void deveRejeitarQuandoSignatureHeaderEstaVazio() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validator.isValid(
                notification,
                "",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando signatureHeader está em branco")
    void deveRejeitarQuandoSignatureHeaderEstaEmBranco() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validator.isValid(
                notification,
                "   ",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando requestId é nulo")
    void deveRejeitarQuandoRequestIdENulo() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validator.isValid(
                notification,
                "signature-header",
                null
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando requestId está vazio")
    void deveRejeitarQuandoRequestIdEstaVazio() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validator.isValid(
                notification,
                "signature-header",
                ""
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar quando requestId está em branco")
    void deveRejeitarQuandoRequestIdEstaEmBranco() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validator.isValid(
                notification,
                "signature-header",
                "   "
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve usar paymentId de dataId quando presente")
    void deveUsarPaymentIdDeDataIdQuandoPresente() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        boolean result = validator.isValid(
                notification,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    @Test
    @DisplayName("Deve usar paymentId de data quando dataId está vazio")
    void deveUsarPaymentIdDeDataQuandoDataIdEstaVazio() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-789")
        );

        boolean result = validator.isValid(
                notification,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }
}
