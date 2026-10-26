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
 * Cobrança PIX Gerada Event
 * Publicado quando um PIX é gerado com sucesso no Mercado Pago
 * Consumido por: OS-Service (para notificar cliente com QR Code)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CobrancaPixGeradaEvent implements DomainEvent, Serializable {
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
    private String qrCodePix;

    @JsonProperty
    private String qrCodeBase64;

    @JsonProperty
    private BigDecimal valorCobrado;

    @JsonProperty
    private LocalDateTime dataExpiracao;

    @JsonProperty
    private String externalReference; // osId enviado ao Mercado Pago

    @JsonProperty
    private LocalDateTime timestamp;

    public CobrancaPixGeradaEvent(
            UUID pagamentoId,
            UUID orcamentoId,
            UUID ordemServicoId,
            UUID clienteId,
            String qrCodePix,
            String qrCodeBase64,
            BigDecimal valorCobrado,
            LocalDateTime dataExpiracao,
            String externalReference) {
        this.pagamentoId = pagamentoId;
        this.orcamentoId = orcamentoId;
        this.ordemServicoId = ordemServicoId;
        this.clienteId = clienteId;
        this.qrCodePix = qrCodePix;
        this.qrCodeBase64 = qrCodeBase64;
        this.valorCobrado = valorCobrado;
        this.dataExpiracao = dataExpiracao;
        this.externalReference = externalReference;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "CobrancaPixGeradaEvent{" +
                "pagamentoId=" + pagamentoId +
                ", ordemServicoId=" + ordemServicoId +
                ", clienteId=" + clienteId +
                ", valorCobrado=" + valorCobrado +
                ", dataExpiracao=" + dataExpiracao +
                ", timestamp=" + timestamp +
                '}';
    }
}
