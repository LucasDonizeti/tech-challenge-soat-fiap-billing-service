package com.techchallenge.billing.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado quando um PIX expira (após 30 minutos sem pagamento)
 * Dispara compensação automática na saga
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SagaPaymentExpiredEvent implements DomainEvent, Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private UUID sagaId;

    @JsonProperty
    private UUID pagamentoId;

    @JsonProperty
    private UUID ordemServicoId;

    @JsonProperty
    private String mercadoPagoPaymentId;

    @JsonProperty
    private LocalDateTime dataExpiracao;

    @JsonProperty
    private LocalDateTime timestamp;

    public SagaPaymentExpiredEvent(
            UUID sagaId,
            UUID pagamentoId,
            UUID ordemServicoId,
            String mercadoPagoPaymentId,
            LocalDateTime dataExpiracao) {
        this.sagaId = sagaId;
        this.pagamentoId = pagamentoId;
        this.ordemServicoId = ordemServicoId;
        this.mercadoPagoPaymentId = mercadoPagoPaymentId;
        this.dataExpiracao = dataExpiracao;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SagaPaymentExpiredEvent{" +
                "sagaId=" + sagaId +
                ", ordemServicoId=" + ordemServicoId +
                ", mercadoPagoPaymentId='" + mercadoPagoPaymentId + '\'' +
                ", dataExpiracao=" + dataExpiracao +
                ", timestamp=" + timestamp +
                '}';
    }
}
