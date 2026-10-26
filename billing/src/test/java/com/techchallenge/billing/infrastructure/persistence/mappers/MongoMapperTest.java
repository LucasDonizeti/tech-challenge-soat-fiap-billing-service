package com.techchallenge.billing.infrastructure.persistence.mappers;

import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.domain.models.WebhookEventLog;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamentoChange;
import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class MongoMapperTest {

    private final OrcamentoMongoMapper orcamentoMapper = new OrcamentoMongoMapper();
    private final PagamentoMongoMapper pagamentoMapper = new PagamentoMongoMapper();
    private final WebhookEventLogMongoMapper webhookMapper = new WebhookEventLogMongoMapper();

    @Test
    void mapsOrcamentoAndStatusHistoryInBothDirections() {
        var id = UUID.randomUUID();
        var osId = UUID.randomUUID();
        var clienteId = UUID.randomUUID();
        var createdAt = LocalDateTime.now();
        var statusHistory = List.of(new StatusOrcamentoChange(StatusOrcamento.GERADO, createdAt));
        var orcamento = new Orcamento();
        orcamento.setId(id);
        orcamento.setOsId(osId);
        orcamento.setClienteId(clienteId);
        orcamento.setVeiculoId(UUID.randomUUID());
        orcamento.setValorTotal(new BigDecimal("125.50"));
        orcamento.setStatus(StatusOrcamento.GERADO);
        orcamento.setDataCriacao(createdAt);
        orcamento.setDataAtualizacao(createdAt);
        orcamento.setDescricaoServicos("Revisao");
        orcamento.setHistoricoStatusChanges(statusHistory);

        var document = orcamentoMapper.toDocument(orcamento);
        var restored = orcamentoMapper.toDomain(document);

        assertEquals(id, restored.getId());
        assertEquals(osId, restored.getOsId());
        assertEquals(clienteId, restored.getClienteId());
        assertEquals(new BigDecimal("125.50"), restored.getValorTotal());
        assertEquals(StatusOrcamento.GERADO, restored.getStatus());
        assertEquals("Revisao", restored.getDescricaoServicos());
        assertEquals(statusHistory, restored.getHistoricoStatusChanges());
        assertNotSame(statusHistory, document.getHistoricoStatusChanges());
    }

    @Test
    void mapsPagamentoFieldsInBothDirections() {
        var id = UUID.randomUUID();
        var createdAt = LocalDateTime.now();
        var pagamento = new Pagamento();
        pagamento.setId(id);
        pagamento.setOrcamentoId(UUID.randomUUID());
        pagamento.setOsId(UUID.randomUUID());
        pagamento.setMercadoPagoPaymentId("mp-payment-123");
        pagamento.setQrCodePix("pix-code");
        pagamento.setQrCodeBase64("base64");
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValorCobrado(new BigDecimal("32.10"));
        pagamento.setValorPago(new BigDecimal("32.10"));
        pagamento.setMotivoRejeicao("motivo");
        pagamento.setDataCriacao(createdAt);
        pagamento.setDataExpiracao(createdAt.plusMinutes(30));
        pagamento.setDataPagamento(createdAt);
        pagamento.setDataAtualizacao(createdAt);
        pagamento.setTentativasSincronizacao(2);
        pagamento.setUltimaSincronizacaoMercadoPago(createdAt);

        var restored = pagamentoMapper.toDomain(pagamentoMapper.toDocument(pagamento));

        assertEquals(id, restored.getId());
        assertEquals(pagamento.getOrcamentoId(), restored.getOrcamentoId());
        assertEquals(pagamento.getOsId(), restored.getOsId());
        assertEquals("mp-payment-123", restored.getMercadoPagoPaymentId());
        assertEquals("pix-code", restored.getQrCodePix());
        assertEquals("base64", restored.getQrCodeBase64());
        assertEquals(StatusPagamento.PENDENTE, restored.getStatus());
        assertEquals(new BigDecimal("32.10"), restored.getValorCobrado());
        assertEquals(new BigDecimal("32.10"), restored.getValorPago());
        assertEquals(createdAt, restored.getDataPagamento());
        assertEquals("motivo", restored.getMotivoRejeicao());
        assertEquals(createdAt, restored.getDataAtualizacao());
        assertEquals(2, restored.getTentativasSincronizacao());
        assertEquals(createdAt, restored.getUltimaSincronizacaoMercadoPago());
    }

    @Test
    void mapsWebhookLogAndCopiesOriginalPayload() {
        var payload = Map.<String, Object>of("action", "payment.updated");
        var eventLog = new WebhookEventLog(
                UUID.randomUUID(),
                "mp-payment-456",
                "payment.updated",
                LocalDateTime.now(),
                payload,
                "PENDENTE_RETRY",
                1,
                LocalDateTime.now().plusMinutes(1)
        );

        var document = webhookMapper.toDocument(eventLog);
        var restored = webhookMapper.toDomain(document);

        assertEquals(eventLog.id(), restored.id());
        assertEquals(eventLog.mercadoPagoPaymentId(), restored.mercadoPagoPaymentId());
        assertEquals(eventLog.action(), restored.action());
        assertEquals(eventLog.payloadOriginal(), restored.payloadOriginal());
        assertEquals(eventLog.statusProcessamento(), restored.statusProcessamento());
        assertNotSame(payload, document.getPayloadOriginal());
    }

}
