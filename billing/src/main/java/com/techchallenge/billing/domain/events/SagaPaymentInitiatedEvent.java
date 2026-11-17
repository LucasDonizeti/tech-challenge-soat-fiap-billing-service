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
 * Evento publicado quando uma saga de pagamento PIX é iniciada
 * Marca o início do fluxo de pagamento distribuído com sagaId único
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SagaPaymentInitiatedEvent implements DomainEvent, Serializable {
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
    private String qrCodePix;

    @JsonProperty
    private String qrCodeBase64;

    @JsonProperty
    private BigDecimal valorCobrado;

    @JsonProperty
    private LocalDateTime dataExpiracao;

    @JsonProperty
    private LocalDateTime timestamp;

    public SagaPaymentInitiatedEvent(
            UUID sagaId,
            UUID pagamentoId,
            UUID orcamentoId,
            UUID ordemServicoId,
            UUID clienteId,
            String mercadoPagoPaymentId,
            String qrCodePix,
            String qrCodeBase64,
            BigDecimal valorCobrado,
            LocalDateTime dataExpiracao) {
        this.sagaId = sagaId;
        this.pagamentoId = pagamentoId;
        this.orcamentoId = orcamentoId;
        this.ordemServicoId = ordemServicoId;
        this.clienteId = clienteId;
        this.mercadoPagoPaymentId = mercadoPagoPaymentId;
        this.qrCodePix = qrCodePix;
        this.qrCodeBase64 = qrCodeBase64;
        this.valorCobrado = valorCobrado;
        this.dataExpiracao = dataExpiracao;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SagaPaymentInitiatedEvent{" +
                "sagaId=" + sagaId +
                ", ordemServicoId=" + ordemServicoId +
                ", mercadoPagoPaymentId='" + mercadoPagoPaymentId + '\'' +
                ", dataExpiracao=" + dataExpiracao +
                ", timestamp=" + timestamp +
                '}';
    }
}
