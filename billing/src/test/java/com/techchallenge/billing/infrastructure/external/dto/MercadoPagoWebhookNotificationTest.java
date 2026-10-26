package com.techchallenge.billing.infrastructure.external.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - MercadoPagoWebhookNotification")
class MercadoPagoWebhookNotificationTest {

    @Test
    @DisplayName("Deve criar notificação com todos os campos")
    void deveCriarNotificacaoComTodosOsCampos() {
        MercadoPagoWebhookNotification.Data data = new MercadoPagoWebhookNotification.Data("payment-123");
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-456",
                1234567890L,
                "payment",
                data
        );

        assertEquals("payment.updated", notification.action());
        assertEquals("payment-456", notification.dataId());
        assertEquals(1234567890L, notification.createdAt());
        assertEquals("payment", notification.type());
        assertEquals(data, notification.data());
    }

    @Test
    @DisplayName("Deve retornar paymentId de dataId quando presente")
    void deveRetornarPaymentIdDeDataIdQuandoPresente() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        assertEquals("payment-123", notification.paymentId());
    }

    @Test
    @DisplayName("Deve retornar paymentId de data quando dataId está vazio")
    void deveRetornarPaymentIdDeDataQuandoDataIdEstaVazio() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-789")
        );

        assertEquals("payment-789", notification.paymentId());
    }

    @Test
    @DisplayName("Deve retornar paymentId de data quando dataId é nulo")
    void deveRetornarPaymentIdDeDataQuandoDataIdENulo() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                null,
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-999")
        );

        assertEquals("payment-999", notification.paymentId());
    }

    @Test
    @DisplayName("Deve retornar null quando dataId e data são nulos")
    void deveRetornarNullQuandoDataIdEDataSaoNulos() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                null,
                1234567890L,
                "payment",
                null
        );

        assertNull(notification.paymentId());
    }

    @Test
    @DisplayName("Deve criar notificação com data nulo")
    void deveCriarNotificacaoComDataNulo() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.created",
                "payment-123",
                1234567890L,
                "payment",
                null
        );

        assertNotNull(notification);
        assertNull(notification.data());
    }

    @Test
    @DisplayName("Deve criar notificação com campos nulos opcionais")
    void deveCriarNotificacaoComCamposNulosOpcionais() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                null,
                null,
                null,
                null,
                null
        );

        assertNull(notification.action());
        assertNull(notification.dataId());
        assertNull(notification.createdAt());
        assertNull(notification.type());
        assertNull(notification.data());
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        String action = "payment.updated";
        String dataId = "payment-123";
        Long createdAt = 1234567890L;
        String type = "payment";
        MercadoPagoWebhookNotification.Data data = new MercadoPagoWebhookNotification.Data("payment-456");

        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                action,
                dataId,
                createdAt,
                type,
                data
        );

        assertEquals(action, notification.action());
        assertEquals(dataId, notification.dataId());
        assertEquals(createdAt, notification.createdAt());
        assertEquals(type, notification.type());
        assertEquals(data, notification.data());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para notificações iguais")
    void devePermitirEqualsEHashCodeParaNotificacoesIguais() {
        MercadoPagoWebhookNotification.Data data = new MercadoPagoWebhookNotification.Data("payment-123");

        MercadoPagoWebhookNotification notification1 = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-456",
                1234567890L,
                "payment",
                data
        );

        MercadoPagoWebhookNotification notification2 = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-456",
                1234567890L,
                "payment",
                data
        );

        assertEquals(notification1, notification2);
        assertEquals(notification1.hashCode(), notification2.hashCode());
    }

    @Test
    @DisplayName("Deve criar record Data com ID")
    void deveCriarRecordDataComID() {
        MercadoPagoWebhookNotification.Data data = new MercadoPagoWebhookNotification.Data("payment-123");

        assertEquals("payment-123", data.id());
    }

    @Test
    @DisplayName("Deve criar record Data com ID nulo")
    void deveCriarRecordDataComIDNulo() {
        MercadoPagoWebhookNotification.Data data = new MercadoPagoWebhookNotification.Data(null);

        assertNull(data.id());
    }
}
