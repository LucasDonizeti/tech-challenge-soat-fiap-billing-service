package com.techchallenge.billing.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado quando uma saga de pagamento é confirmada
 * (seja aprovada, recusada ou expirada após sincronização com Mercado Pago)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SagaPaymentConfirmedEvent implements DomainEvent, Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private UUID sagaId;

    @JsonProperty
    private UUID pagamentoId;

    @JsonProperty
    private UUID orcamentoId;

    @JsonProperty
    private UUID ordemServicoId;

    @JsonProperty
    private UUID clienteId;

    @JsonProperty
    private String mercadoPagoPaymentId;

    @JsonProperty
    private String statusFinal; // APROVADO, RECUSADO, EXPIRADO

    @JsonProperty
    private BigDecimal valorPago;

    @JsonProperty
    private LocalDateTime dataPagamento;

    @JsonProperty
    private String motivo; // Motivo de rejeição/expiração

    @JsonProperty
    private LocalDateTime timestamp;

    public SagaPaymentConfirmedEvent(
            UUID sagaId,
            UUID pagamentoId,
            UUID orcamentoId,
            UUID ordemServicoId,
            UUID clienteId,
            String mercadoPagoPaymentId,
            String statusFinal,
            BigDecimal valorPago,
            LocalDateTime dataPagamento,
            String motivo) {
        this.sagaId = sagaId;
        this.pagamentoId = pagamentoId;
        this.orcamentoId = orcamentoId;
        this.ordemServicoId = ordemServicoId;
        this.clienteId = clienteId;
        this.mercadoPagoPaymentId = mercadoPagoPaymentId;
        this.statusFinal = statusFinal;
        this.valorPago = valorPago;
        this.dataPagamento = dataPagamento;
        this.motivo = motivo;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SagaPaymentConfirmedEvent{" +
                "sagaId=" + sagaId +
                ", ordemServicoId=" + ordemServicoId +
                ", statusFinal='" + statusFinal + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
