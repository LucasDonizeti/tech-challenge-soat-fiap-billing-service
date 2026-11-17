package com.techchallenge.billing.domain.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SagaContextTest {

    @Test
    void createsSagaContextSuccessfully() {
        UUID ordemServicoId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);

        SagaContext sagaContext = SagaContext.criar(
                ordemServicoId,
                pagamentoId,
                orcamentoId,
                mercadoPagoPaymentId,
                dataExpiracao
        );

        assertNotNull(sagaContext.getSagaId());
        assertEquals(ordemServicoId, sagaContext.getOrdemServicoId());
        assertEquals(pagamentoId, sagaContext.getPagamentoId());
        assertEquals(orcamentoId, sagaContext.getOrcamentoId());
        assertEquals(mercadoPagoPaymentId, sagaContext.getMercadoPagoPaymentId());
        assertEquals("INICIADA", sagaContext.getStatus());
        assertNotNull(sagaContext.getCriadoEm());
        assertNotNull(sagaContext.getUltimaAtualizacao());
        assertEquals(0, sagaContext.getTentativasWebhook());
        assertEquals(dataExpiracao, sagaContext.getDataExpiracao());
        assertNotNull(sagaContext.getDados());
        assertTrue(sagaContext.getDados().isEmpty());
    }

    @Test
    void marksAsPendente() {
        SagaContext sagaContext = SagaContext.builder()
                .status("INICIADA")
                .build();

        sagaContext.marcarPendente();

        assertEquals("PENDENTE", sagaContext.getStatus());
        assertNotNull(sagaContext.getUltimaAtualizacao());
    }

    @Test
    void marksAsAprovada() {
        SagaContext sagaContext = SagaContext.builder()
                .status("INICIADA")
                .build();

        sagaContext.marcarAprovada();

        assertEquals("APROVADA", sagaContext.getStatus());
        assertNotNull(sagaContext.getUltimaAtualizacao());
    }

    @Test
    void marksAsExpirada() {
        SagaContext sagaContext = SagaContext.builder()
                .status("INICIADA")
                .build();

        sagaContext.marcarExpirada();

        assertEquals("EXPIRADA", sagaContext.getStatus());
        assertNotNull(sagaContext.getUltimaAtualizacao());
    }

    @Test
    void marksAsRecusada() {
        SagaContext sagaContext = SagaContext.builder()
                .status("INICIADA")
                .build();

        sagaContext.marcarRecusada();

        assertEquals("RECUSADA", sagaContext.getStatus());
        assertNotNull(sagaContext.getUltimaAtualizacao());
    }

    @Test
    void marksAsCancelada() {
        SagaContext sagaContext = SagaContext.builder()
                .status("INICIADA")
                .build();

        sagaContext.marcarCancelada();

        assertEquals("CANCELADA", sagaContext.getStatus());
        assertNotNull(sagaContext.getUltimaAtualizacao());
    }

    @Test
    void marksAsCompensada() {
        SagaContext sagaContext = SagaContext.builder()
                .status("INICIADA")
                .build();

        sagaContext.marcarCompensada();

        assertEquals("COMPENSADA", sagaContext.getStatus());
        assertNotNull(sagaContext.getUltimaAtualizacao());
    }

    @Test
    void incrementsWebhookAttempts() {
        SagaContext sagaContext = SagaContext.builder()
                .tentativasWebhook(0)
                .build();

        LocalDateTime before = LocalDateTime.now();
        sagaContext.incrementarTentativasWebhook();
        LocalDateTime after = LocalDateTime.now();

        assertEquals(1, sagaContext.getTentativasWebhook());
        assertNotNull(sagaContext.getProximaRetentativaWebhook());
        assertTrue(sagaContext.getProximaRetentativaWebhook().isAfter(before));
        assertTrue(sagaContext.getProximaRetentativaWebhook().isBefore(after.plusMinutes(2)));
    }

    @Test
    void incrementsWebhookAttemptsMultipleTimes() {
        SagaContext sagaContext = SagaContext.builder()
                .tentativasWebhook(2)
                .build();

        sagaContext.incrementarTentativasWebhook();

        assertEquals(3, sagaContext.getTentativasWebhook());
        assertNotNull(sagaContext.getProximaRetentativaWebhook());
    }

    @Test
    void isExpiredReturnsTrueWhenExpired() {
        SagaContext sagaContext = SagaContext.builder()
                .dataExpiracao(LocalDateTime.now().minusMinutes(1))
                .build();

        assertTrue(sagaContext.estáExpirada());
    }

    @Test
    void isExpiredReturnsFalseWhenNotExpired() {
        SagaContext sagaContext = SagaContext.builder()
                .dataExpiracao(LocalDateTime.now().plusMinutes(30))
                .build();

        assertFalse(sagaContext.estáExpirada());
    }

    @Test
    void isExpiredReturnsFalseWhenDataExpiracaoIsNull() {
        SagaContext sagaContext = SagaContext.builder()
                .dataExpiracao(null)
                .build();

        assertFalse(sagaContext.estáExpirada());
    }

    @Test
    void addsDataSuccessfully() {
        SagaContext sagaContext = SagaContext.builder()
                .dados(new java.util.HashMap<>())
                .build();

        String key = "clienteId";
        Object value = UUID.randomUUID();

        sagaContext.adicionarDado(key, value);

        assertEquals(value, sagaContext.obterDado(key));
    }

    @Test
    void obterDadoReturnsNullForNonExistentKey() {
        SagaContext sagaContext = SagaContext.builder()
                .dados(new java.util.HashMap<>())
                .build();

        Object result = sagaContext.obterDado("nonExistentKey");

        assertNull(result);
    }

    @Test
    void overwritesExistingData() {
        SagaContext sagaContext = SagaContext.builder()
                .dados(new java.util.HashMap<>())
                .build();

        String key = "clienteId";
        UUID value1 = UUID.randomUUID();
        UUID value2 = UUID.randomUUID();

        sagaContext.adicionarDado(key, value1);
        sagaContext.adicionarDado(key, value2);

        assertEquals(value2, sagaContext.obterDado(key));
    }

    @Test
    void builderCreatesSagaContextWithAllFields() {
        UUID sagaId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        String status = "INICIADA";
        LocalDateTime criadoEm = LocalDateTime.now();
        LocalDateTime ultimaAtualizacao = LocalDateTime.now();
        int tentativasWebhook = 5;
        LocalDateTime proximaRetentativaWebhook = LocalDateTime.now().plusMinutes(1);
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);
        java.util.Map<String, Object> dados = new java.util.HashMap<>();
        dados.put("key", "value");

        SagaContext sagaContext = SagaContext.builder()
                .sagaId(sagaId)
                .ordemServicoId(ordemServicoId)
                .pagamentoId(pagamentoId)
                .orcamentoId(orcamentoId)
                .mercadoPagoPaymentId(mercadoPagoPaymentId)
                .status(status)
                .criadoEm(criadoEm)
                .ultimaAtualizacao(ultimaAtualizacao)
                .tentativasWebhook(tentativasWebhook)
                .proximaRetentativaWebhook(proximaRetentativaWebhook)
                .dataExpiracao(dataExpiracao)
                .dados(dados)
                .build();

        assertEquals(sagaId, sagaContext.getSagaId());
        assertEquals(ordemServicoId, sagaContext.getOrdemServicoId());
        assertEquals(pagamentoId, sagaContext.getPagamentoId());
        assertEquals(orcamentoId, sagaContext.getOrcamentoId());
        assertEquals(mercadoPagoPaymentId, sagaContext.getMercadoPagoPaymentId());
        assertEquals(status, sagaContext.getStatus());
        assertEquals(criadoEm, sagaContext.getCriadoEm());
        assertEquals(ultimaAtualizacao, sagaContext.getUltimaAtualizacao());
        assertEquals(tentativasWebhook, sagaContext.getTentativasWebhook());
        assertEquals(proximaRetentativaWebhook, sagaContext.getProximaRetentativaWebhook());
        assertEquals(dataExpiracao, sagaContext.getDataExpiracao());
        assertEquals(dados, sagaContext.getDados());
    }

    @Test
    void settersWorkCorrectly() {
        SagaContext sagaContext = SagaContext.builder().build();

        UUID sagaId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD456";
        String status = "PENDENTE";
        LocalDateTime criadoEm = LocalDateTime.now();
        LocalDateTime ultimaAtualizacao = LocalDateTime.now();
        int tentativasWebhook = 3;
        LocalDateTime proximaRetentativaWebhook = LocalDateTime.now();
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);
        java.util.Map<String, Object> dados = new java.util.HashMap<>();

        sagaContext.setSagaId(sagaId);
        sagaContext.setOrdemServicoId(ordemServicoId);
        sagaContext.setPagamentoId(pagamentoId);
        sagaContext.setOrcamentoId(orcamentoId);
        sagaContext.setMercadoPagoPaymentId(mercadoPagoPaymentId);
        sagaContext.setStatus(status);
        sagaContext.setCriadoEm(criadoEm);
        sagaContext.setUltimaAtualizacao(ultimaAtualizacao);
        sagaContext.setTentativasWebhook(tentativasWebhook);
        sagaContext.setProximaRetentativaWebhook(proximaRetentativaWebhook);
        sagaContext.setDataExpiracao(dataExpiracao);
        sagaContext.setDados(dados);

        assertEquals(sagaId, sagaContext.getSagaId());
        assertEquals(ordemServicoId, sagaContext.getOrdemServicoId());
        assertEquals(pagamentoId, sagaContext.getPagamentoId());
        assertEquals(orcamentoId, sagaContext.getOrcamentoId());
        assertEquals(mercadoPagoPaymentId, sagaContext.getMercadoPagoPaymentId());
        assertEquals(status, sagaContext.getStatus());
        assertEquals(criadoEm, sagaContext.getCriadoEm());
        assertEquals(ultimaAtualizacao, sagaContext.getUltimaAtualizacao());
        assertEquals(tentativasWebhook, sagaContext.getTentativasWebhook());
        assertEquals(proximaRetentativaWebhook, sagaContext.getProximaRetentativaWebhook());
        assertEquals(dataExpiracao, sagaContext.getDataExpiracao());
        assertEquals(dados, sagaContext.getDados());
    }
}