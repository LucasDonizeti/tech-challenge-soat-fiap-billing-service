package com.techchallenge.billing.infrastructure.persistence.mappers;

import com.techchallenge.billing.domain.models.WebhookEventLog;
import com.techchallenge.billing.infrastructure.persistence.documents.WebhookEventLogDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - WebhookEventLogMongoMapper")
class WebhookEventLogMongoMapperTest {

    private WebhookEventLogMongoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WebhookEventLogMongoMapper();
    }

    @Test
    @DisplayName("Deve converter WebhookEventLog para WebhookEventLogDocument")
    void deveConverterWebhookEventLogParaWebhookEventLogDocument() {
        WebhookEventLog eventLog = createWebhookEventLog();

        WebhookEventLogDocument document = mapper.toDocument(eventLog);

        assertNotNull(document);
        assertEquals(eventLog.id(), document.getId());
        assertEquals(eventLog.mercadoPagoPaymentId(), document.getMercadoPagoPaymentId());
        assertEquals(eventLog.action(), document.getAction());
        assertEquals(eventLog.dataRecebimento(), document.getDataRecebimento());
        assertEquals(eventLog.payloadOriginal(), document.getPayloadOriginal());
        assertEquals(eventLog.statusProcessamento(), document.getStatusProcessamento());
        assertEquals(eventLog.tentativas(), document.getTentativas());
        assertEquals(eventLog.proximaRetentativa(), document.getProximaRetentativa());
    }

    @Test
    @DisplayName("Deve converter WebhookEventLogDocument para WebhookEventLog")
    void deveConverterWebhookEventLogDocumentParaWebhookEventLog() {
        WebhookEventLogDocument document = createWebhookEventLogDocument();

        WebhookEventLog eventLog = mapper.toDomain(document);

        assertNotNull(eventLog);
        assertEquals(document.getId(), eventLog.id());
        assertEquals(document.getMercadoPagoPaymentId(), eventLog.mercadoPagoPaymentId());
        assertEquals(document.getAction(), eventLog.action());
        assertEquals(document.getDataRecebimento(), eventLog.dataRecebimento());
        assertEquals(document.getPayloadOriginal(), eventLog.payloadOriginal());
        assertEquals(document.getStatusProcessamento(), eventLog.statusProcessamento());
        assertEquals(document.getTentativas(), eventLog.tentativas());
        assertEquals(document.getProximaRetentativa(), eventLog.proximaRetentativa());
    }

    @Test
    @DisplayName("Deve lançar exceção quando WebhookEventLog é nulo ao converter para document")
    void deveLancarExcecaoQuandoWebhookEventLogENuloAoConverterParaDocument() {
        assertThrows(NullPointerException.class, () -> mapper.toDocument(null));
    }

    @Test
    @DisplayName("Deve lançar exceção quando WebhookEventLogDocument é nulo ao converter para domain")
    void deveLancarExcecaoQuandoWebhookEventLogDocumentENuloAoConverterParaDomain() {
        assertThrows(NullPointerException.class, () -> mapper.toDomain(null));
    }

    @Test
    @DisplayName("Deve manter consistência em conversão bidirecional")
    void deveManterConsistenciaEmConversaoBidirecional() {
        WebhookEventLog original = createWebhookEventLog();

        WebhookEventLogDocument document = mapper.toDocument(original);
        WebhookEventLog restored = mapper.toDomain(document);

        assertEquals(original.id(), restored.id());
        assertEquals(original.mercadoPagoPaymentId(), restored.mercadoPagoPaymentId());
        assertEquals(original.action(), restored.action());
        assertEquals(original.dataRecebimento(), restored.dataRecebimento());
        assertEquals(original.payloadOriginal(), restored.payloadOriginal());
        assertEquals(original.statusProcessamento(), restored.statusProcessamento());
        assertEquals(original.tentativas(), restored.tentativas());
        assertEquals(original.proximaRetentativa(), restored.proximaRetentativa());
    }

    @Test
    @DisplayName("Deve copiar payload ao converter para document")
    void deveCopiarPayloadAoConverterParaDocument() {
        Map<String, Object> payload = Map.of("key", "value", "type", "payment");
        WebhookEventLog eventLog = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                payload,
                "PENDENTE",
                0,
                null
        );

        WebhookEventLogDocument document = mapper.toDocument(eventLog);

        assertNotNull(document.getPayloadOriginal());
        assertEquals(payload, document.getPayloadOriginal());
        assertNotSame(payload, document.getPayloadOriginal());
    }

    @Test
    @DisplayName("Deve copiar payload ao converter para domain")
    void deveCopiarPayloadAoConverterParaDomain() {
        Map<String, Object> payload = Map.of("key", "value", "type", "payment");
        WebhookEventLogDocument document = new WebhookEventLogDocument(
                UUID.randomUUID(),
                "mp-payment-456",
                "payment.created",
                LocalDateTime.now(),
                payload,
                "PROCESSADO",
                1,
                null
        );

        WebhookEventLog eventLog = mapper.toDomain(document);

        assertNotNull(eventLog.payloadOriginal());
        assertEquals(payload, eventLog.payloadOriginal());
        assertNotSame(payload, eventLog.payloadOriginal());
    }

    @Test
    @DisplayName("Deve manter payload nulo quando original é nulo ao converter para document")
    void deveManterPayloadNuloQuandoOriginalENuloAoConverterParaDocument() {
        WebhookEventLog eventLog = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                null,
                "PENDENTE",
                0,
                null
        );

        WebhookEventLogDocument document = mapper.toDocument(eventLog);

        assertNull(document.getPayloadOriginal());
    }

    @Test
    @DisplayName("Deve manter payload nulo quando original é nulo ao converter para domain")
    void deveManterPayloadNuloQuandoOriginalENuloAoConverterParaDomain() {
        WebhookEventLogDocument document = new WebhookEventLogDocument(
                UUID.randomUUID(),
                "mp-payment-456",
                "payment.created",
                LocalDateTime.now(),
                null,
                "PROCESSADO",
                1,
                null
        );

        WebhookEventLog eventLog = mapper.toDomain(document);

        assertNull(eventLog.payloadOriginal());
    }

    @Test
    @DisplayName("Deve converter com payload vazio")
    void deveConverterComPayloadVazio() {
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

        WebhookEventLogDocument document = mapper.toDocument(eventLog);
        WebhookEventLog restored = mapper.toDomain(document);

        assertNotNull(restored.payloadOriginal());
        assertTrue(restored.payloadOriginal().isEmpty());
    }

    private WebhookEventLog createWebhookEventLog() {
        return new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment", "data_id", "mp-123"),
                "PENDENTE",
                0,
                null
        );
    }

    private WebhookEventLogDocument createWebhookEventLogDocument() {
        return new WebhookEventLogDocument(
                UUID.randomUUID(),
                "mp-payment-456",
                "payment.created",
                LocalDateTime.now(),
                Map.of("type", "payment", "action", "created"),
                "PROCESSADO",
                1,
                LocalDateTime.now().plusMinutes(5)
        );
    }
}
