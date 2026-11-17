package com.techchallenge.billing.infrastructure.external.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
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
                "2026-11-01T15:00:00Z",
                "payment",
                data,
                "v1",
                987654321L,
                true,
                "user-123"
        );

        assertEquals("payment.updated", notification.action());
        assertEquals("payment-456", notification.dataId());
        assertEquals(1234567890L, notification.createdAt());
        assertEquals("2026-11-01T15:00:00Z", notification.dateCreated());
        assertEquals("payment", notification.type());
        assertEquals(data, notification.data());
        assertEquals("v1", notification.apiVersion());
        assertEquals(987654321L, notification.id());
        assertTrue(notification.liveMode());
        assertEquals("user-123", notification.userId());
    }

    @Test
    @DisplayName("Deve desserializar o body atual do Mercado Pago")
    void deveDesserializarBodyAtualDoMercadoPago() throws Exception {
        String body = """
                {
                  "action": "payment.updated",
                  "api_version": "v1",
                  "data": {"id": "payment-123"},
                  "date_created": "2026-11-01T15:00:00Z",
                  "id": 987654321,
                  "live_mode": true,
                  "type": "payment",
                  "user_id": 123456789
                }
                """;

        MercadoPagoWebhookNotification notification =
                new ObjectMapper().readValue(body, MercadoPagoWebhookNotification.class);

        assertEquals("payment.updated", notification.action());
        assertNull(notification.dataId());
        assertEquals("payment-123", notification.paymentId());
        assertEquals("2026-11-01T15:00:00Z", notification.dateCreated());
        assertEquals("payment", notification.type());
        assertEquals("v1", notification.apiVersion());
        assertEquals(987654321L, notification.id());
        assertTrue(notification.liveMode());
        assertEquals("123456789", notification.userId());
    }

    @Test
    @DisplayName("Deve retornar paymentId de dataId quando presente")
    void deveRetornarPaymentIdDeDataIdQuandoPresente() {
        MercadoPagoWebhookNotification notification = notification(
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
        MercadoPagoWebhookNotification notification = notification(
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
        MercadoPagoWebhookNotification notification = notification(
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
        MercadoPagoWebhookNotification notification = notification(
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
        MercadoPagoWebhookNotification notification = notification(
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
                null, null, null, null, null, null, null, null, null, null
        );

        assertNull(notification.action());
        assertNull(notification.dataId());
        assertNull(notification.createdAt());
        assertNull(notification.dateCreated());
        assertNull(notification.type());
        assertNull(notification.data());
        assertNull(notification.apiVersion());
        assertNull(notification.id());
        assertNull(notification.liveMode());
        assertNull(notification.userId());
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        MercadoPagoWebhookNotification.Data data = new MercadoPagoWebhookNotification.Data("payment-456");
        MercadoPagoWebhookNotification notification = notification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                data
        );

        assertEquals("payment.updated", notification.action());
        assertEquals("payment-123", notification.dataId());
        assertEquals(1234567890L, notification.createdAt());
        assertEquals("2026-11-01T15:00:00Z", notification.dateCreated());
        assertEquals("payment", notification.type());
        assertEquals(data, notification.data());
        assertEquals("v1", notification.apiVersion());
        assertEquals(987654321L, notification.id());
        assertTrue(notification.liveMode());
        assertEquals("user-123", notification.userId());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para notificações iguais")
    void devePermitirEqualsEHashCodeParaNotificacoesIguais() {
        MercadoPagoWebhookNotification.Data data = new MercadoPagoWebhookNotification.Data("payment-123");
        MercadoPagoWebhookNotification notification1 = notification(
                "payment.updated", "payment-456", 1234567890L, "payment", data
        );
        MercadoPagoWebhookNotification notification2 = notification(
                "payment.updated", "payment-456", 1234567890L, "payment", data
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

    private MercadoPagoWebhookNotification notification(
            String action,
            String dataId,
            Long createdAt,
            String type,
            MercadoPagoWebhookNotification.Data data
    ) {
        return new MercadoPagoWebhookNotification(
                action,
                dataId,
                createdAt,
                "2026-11-01T15:00:00Z",
                type,
                data,
                "v1",
                987654321L,
                true,
                "user-123"
        );
    }
}
