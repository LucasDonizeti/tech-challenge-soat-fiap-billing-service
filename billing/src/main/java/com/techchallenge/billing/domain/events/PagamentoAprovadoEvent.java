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
 * Pagamento Aprovado Event
 * Publicado quando o pagamento é aprovado no Mercado Pago
 * Consumido por: OS-Service (para transicionar OS para EM_EXECUCAO)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoAprovadoEvent implements DomainEvent, Serializable {
    private static final long serialVersionUID = 1L;

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
    private BigDecimal valorPago;

    @JsonProperty
    private LocalDateTime dataPagamento;

    @JsonProperty
    private LocalDateTime timestamp;

    public PagamentoAprovadoEvent(
            UUID pagamentoId,
            UUID orcamentoId,
            UUID ordemServicoId,
            UUID clienteId,
            String mercadoPagoPaymentId,
            BigDecimal valorPago,
            LocalDateTime dataPagamento) {
        this.pagamentoId = pagamentoId;
        this.orcamentoId = orcamentoId;
        this.ordemServicoId = ordemServicoId;
        this.clienteId = clienteId;
        this.mercadoPagoPaymentId = mercadoPagoPaymentId;
        this.valorPago = valorPago;
        this.dataPagamento = dataPagamento;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "PagamentoAprovadoEvent{" +
                "pagamentoId=" + pagamentoId +
                ", ordemServicoId=" + ordemServicoId +
                ", clienteId=" + clienteId +
                ", mercadoPagoPaymentId='" + mercadoPagoPaymentId + '\'' +
                ", valorPago=" + valorPago +
                ", dataPagamento=" + dataPagamento +
                ", timestamp=" + timestamp +
                '}';
    }
}
