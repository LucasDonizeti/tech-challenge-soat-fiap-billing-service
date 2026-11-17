package com.techchallenge.billing.domain.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - WebhookEventLog")
class WebhookEventLogTest {

    @Test
    @DisplayName("Deve criar registro com todos os campos")
    void deveCriarRegistroComTodosOsCampos() {
        UUID id = UUID.randomUUID();
        String mercadoPagoPaymentId = "mp-payment-123";
        String action = "payment.updated";
        LocalDateTime dataRecebimento = LocalDateTime.now();
        Map<String, Object> payload = Map.of("type", "payment", "data_id", "mp-123");
        String statusProcessamento = "PENDENTE";
        int tentativas = 0;
        LocalDateTime proximaRetentativa = null;

        WebhookEventLog eventLog = new WebhookEventLog(
                id,
                mercadoPagoPaymentId,
                action,
                dataRecebimento,
                payload,
                statusProcessamento,
                tentativas,
                proximaRetentativa
        );

        assertEquals(id, eventLog.id());
        assertEquals(mercadoPagoPaymentId, eventLog.mercadoPagoPaymentId());
        assertEquals(action, eventLog.action());
        assertEquals(dataRecebimento, eventLog.dataRecebimento());
        assertEquals(payload, eventLog.payloadOriginal());
        assertEquals(statusProcessamento, eventLog.statusProcessamento());
        assertEquals(tentativas, eventLog.tentativas());
        assertEquals(proximaRetentativa, eventLog.proximaRetentativa());
    }

    @Test
    @DisplayName("Deve criar registro com campos nulos opcionais")
    void deveCriarRegistroComCamposNulosOpcionais() {
        WebhookEventLog eventLog = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-456",
                "payment.created",
                LocalDateTime.now(),
                Map.of("type", "payment"),
                "PROCESSADO",
                1,
                null
        );

        assertNotNull(eventLog.id());
        assertNotNull(eventLog.mercadoPagoPaymentId());
        assertNotNull(eventLog.action());
        assertNotNull(eventLog.dataRecebimento());
        assertNotNull(eventLog.payloadOriginal());
        assertNotNull(eventLog.statusProcessamento());
        assertEquals(1, eventLog.tentativas());
        assertNull(eventLog.proximaRetentativa());
    }

    @Test
    @DisplayName("Deve criar registro com proximaRetentativa definida")
    void deveCriarRegistroComProximaRetentativaDefinida() {
        LocalDateTime proximaRetentativa = LocalDateTime.now().plusMinutes(5);

        WebhookEventLog eventLog = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-789",
                "payment.failed",
                LocalDateTime.now(),
                Map.of("error", "timeout"),
                "PENDENTE_RETRY",
                2,
                proximaRetentativa
        );

        assertEquals(proximaRetentativa, eventLog.proximaRetentativa());
        assertEquals(2, eventLog.tentativas());
        assertEquals("PENDENTE_RETRY", eventLog.statusProcessamento());
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        UUID id = UUID.randomUUID();
        String paymentId = "mp-payment-123";
        String action = "payment.updated";
        LocalDateTime data = LocalDateTime.now();
        Map<String, Object> payload = Map.of("key", "value");
        String status = "PENDENTE";
        int tentativas = 0;
        LocalDateTime proximaRetentativa = null;

        WebhookEventLog eventLog = new WebhookEventLog(
                id,
                paymentId,
                action,
                data,
                payload,
                status,
                tentativas,
                proximaRetentativa
        );

        assertEquals(id, eventLog.id());
        assertEquals(paymentId, eventLog.mercadoPagoPaymentId());
        assertEquals(action, eventLog.action());
        assertEquals(data, eventLog.dataRecebimento());
        assertEquals(payload, eventLog.payloadOriginal());
        assertEquals(status, eventLog.statusProcessamento());
        assertEquals(tentativas, eventLog.tentativas());
        assertEquals(proximaRetentativa, eventLog.proximaRetentativa());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para registros iguais")
    void devePermitirEqualsEHashCodeParaRegistrosIguais() {
        UUID id = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now();
        Map<String, Object> payload = Map.of("key", "value");

        WebhookEventLog eventLog1 = new WebhookEventLog(
                id,
                "mp-payment-123",
                "payment.updated",
                data,
                payload,
                "PENDENTE",
                0,
                null
        );

        WebhookEventLog eventLog2 = new WebhookEventLog(
                id,
                "mp-payment-123",
                "payment.updated",
                data,
                payload,
                "PENDENTE",
                0,
                null
        );

        assertEquals(eventLog1, eventLog2);
        assertEquals(eventLog1.hashCode(), eventLog2.hashCode());
    }

    @Test
    @DisplayName("Deve diferenciar registros com IDs diferentes")
    void deveDiferenciarRegistrosComIDsDiferentes() {
        LocalDateTime data = LocalDateTime.now();
        Map<String, Object> payload = Map.of("key", "value");

        WebhookEventLog eventLog1 = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-123",
                "payment.updated",
                data,
                payload,
                "PENDENTE",
                0,
                null
        );

        WebhookEventLog eventLog2 = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-123",
                "payment.updated",
                data,
                payload,
                "PENDENTE",
                0,
                null
        );

        assertNotEquals(eventLog1, eventLog2);
    }

    @Test
    @DisplayName("Deve diferenciar registros com status diferente")
    void deveDiferenciarRegistrosComStatusDiferente() {
        UUID id = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now();
        Map<String, Object> payload = Map.of("key", "value");

        WebhookEventLog eventLog1 = new WebhookEventLog(
                id,
                "mp-payment-123",
                "payment.updated",
                data,
                payload,
                "PENDENTE",
                0,
                null
        );

        WebhookEventLog eventLog2 = new WebhookEventLog(
                id,
                "mp-payment-123",
                "payment.updated",
                data,
                payload,
                "PROCESSADO",
                1,
                null
        );

        assertNotEquals(eventLog1, eventLog2);
    }

    @Test
    @DisplayName("Deve ter métodos de acesso funcionando corretamente")
    void deveTerMetodosDeAcessoFuncionandoCorretamente() {
        UUID id = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now();
        Map<String, Object> payload = Map.of("key", "value");

        WebhookEventLog eventLog = new WebhookEventLog(
                id,
                "mp-payment-123",
                "payment.updated",
                data,
                payload,
                "PENDENTE",
                0,
                null
        );

        assertNotNull(eventLog.id());
        assertNotNull(eventLog.mercadoPagoPaymentId());
        assertNotNull(eventLog.action());
        assertNotNull(eventLog.dataRecebimento());
        assertNotNull(eventLog.payloadOriginal());
        assertNotNull(eventLog.statusProcessamento());
        assertEquals(0, eventLog.tentativas());
        assertNull(eventLog.proximaRetentativa());
    }

    @Test
    @DisplayName("Deve aceitar payload vazio")
    void deveAceitarPayloadVazio() {
        WebhookEventLog eventLog = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                Map.of(),
                "PENDENTE",
                0,
                null
        );

        assertNotNull(eventLog.payloadOriginal());
        assertTrue(eventLog.payloadOriginal().isEmpty());
    }

    @Test
    @DisplayName("Deve aceitar zero tentativas")
    void deveAceitarZeroTentativas() {
        WebhookEventLog eventLog = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("key", "value"),
                "PENDENTE",
                0,
                null
        );

        assertEquals(0, eventLog.tentativas());
    }

    @Test
    @DisplayName("Deve aceitar múltiplas tentativas")
    void deveAceitarMultiplasTentativas() {
        WebhookEventLog eventLog = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("key", "value"),
                "PENDENTE_RETRY",
                5,
                LocalDateTime.now().plusMinutes(10)
        );

        assertEquals(5, eventLog.tentativas());
        assertNotNull(eventLog.proximaRetentativa());
    }
}
