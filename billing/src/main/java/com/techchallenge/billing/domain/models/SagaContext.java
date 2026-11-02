package com.techchallenge.billing.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contexto de uma Saga de Pagamento PIX
 * Rastreia o progresso de toda a transação distribuída com um único sagaId
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaContext {
    private UUID sagaId;
    private UUID ordemServicoId;
    private UUID pagamentoId;
    private UUID orcamentoId;
    private String mercadoPagoPaymentId;
    private String status; // INICIADA, PENDENTE, APROVADA, EXPIRADA, RECUSADA, COMPENSADA
    private LocalDateTime criadoEm;
    private LocalDateTime ultimaAtualizacao;
    private int tentativasWebhook;
    private LocalDateTime proximaRetentativaWebhook;
    private LocalDateTime dataExpiracao;
    private Map<String, Object> dados; // Dados intermediários da saga

    public static SagaContext criar(
            UUID ordemServicoId,
            UUID pagamentoId,
            UUID orcamentoId,
            String mercadoPagoPaymentId,
            LocalDateTime dataExpiracao) {
        return SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .ordemServicoId(ordemServicoId)
                .pagamentoId(pagamentoId)
                .orcamentoId(orcamentoId)
                .mercadoPagoPaymentId(mercadoPagoPaymentId)
                .status("INICIADA")
                .criadoEm(LocalDateTime.now())
                .ultimaAtualizacao(LocalDateTime.now())
                .tentativasWebhook(0)
                .dataExpiracao(dataExpiracao)
                .dados(new HashMap<>())
                .build();
    }

    public void marcarPendente() {
        this.status = "PENDENTE";
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void marcarAprovada() {
        this.status = "APROVADA";
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void marcarExpirada() {
        this.status = "EXPIRADA";
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void marcarRecusada() {
        this.status = "RECUSADA";
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void marcarCancelada() {
        this.status = "CANCELADA";
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void marcarCompensada() {
        this.status = "COMPENSADA";
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void incrementarTentativasWebhook() {
        this.tentativasWebhook++;
        this.proximaRetentativaWebhook = LocalDateTime.now().plusMinutes(1);
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public boolean estáExpirada() {
        return dataExpiracao != null && LocalDateTime.now().isAfter(dataExpiracao);
    }

    public void adicionarDado(String chave, Object valor) {
        this.dados.put(chave, valor);
    }

    public Object obterDado(String chave) {
        return this.dados.get(chave);
    }
}
