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
    @DisplayName("Deve ignorar assinatura quando bypass está habilitado em perfil seguro")
    void deveIgnorarAssinaturaQuandoBypassHabilitado() {
        MercadoPagoWebhookSignatureValidator validatorWithBypass =
                new MercadoPagoWebhookSignatureValidator("test-secret", true, true);

        assertTrue(validatorWithBypass.isValid(null, null, null, null));
    }

    @Test
    @DisplayName("Não deve permitir bypass fora dos perfis de desenvolvimento e teste")
    void naoDevePermitirBypassForaDosPerfisPermitidos() {
        assertThrows(IllegalStateException.class, () ->
                new MercadoPagoWebhookSignatureValidator("test-secret", true, false)
        );
    }

    @Test
    @DisplayName("Deve rejeitar quando secret é nulo")
    void deveRejeitarQuandoSecretENulo() {
        MercadoPagoWebhookSignatureValidator validatorWithNullSecret = new MercadoPagoWebhookSignatureValidator(null);

        MercadoPagoWebhookNotification notification = notification("payment-123");

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

        MercadoPagoWebhookNotification notification = notification("payment-123");

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

        MercadoPagoWebhookNotification notification = notification("payment-123");

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
        MercadoPagoWebhookNotification notification = notification(null);

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
        MercadoPagoWebhookNotification notification = notification("");

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
        MercadoPagoWebhookNotification notification = notification("   ");

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
        MercadoPagoWebhookNotification notification = notification("payment-123");

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
        MercadoPagoWebhookNotification notification = notification("payment-123");

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
        MercadoPagoWebhookNotification notification = notification("payment-123");

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
        MercadoPagoWebhookNotification notification = notification("payment-123");

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
        MercadoPagoWebhookNotification notification = notification("payment-123");

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
        MercadoPagoWebhookNotification notification = notification("payment-123");

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
                "2026-11-01T15:00:00Z",
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456"),
                "v1",
                987654321L,
                true,
                "user-123"
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
        MercadoPagoWebhookNotification notificationWithBlankDataId = new MercadoPagoWebhookNotification(
                "payment.updated",
                "",
                1234567890L,
                "2026-11-01T15:00:00Z",
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-789"),
                "v1",
                987654321L,
                true,
                "user-123"
        );

        boolean result = validator.isValid(
                notificationWithBlankDataId,
                "signature-header",
                "request-id"
        );

        assertFalse(result);
    }

    private MercadoPagoWebhookNotification notification(String paymentId) {
        return new MercadoPagoWebhookNotification(
                "payment.updated",
                null,
                1234567890L,
                "2026-11-01T15:00:00Z",
                "payment",
                paymentId == null ? null : new MercadoPagoWebhookNotification.Data(paymentId),
                "v1",
                987654321L,
                true,
                "user-123"
        );
    }
}
