package com.techchallenge.billing.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pagamento Recusado Event
 * Publicado quando pagamento é recusado ou expirado
 * Consumido por: OS-Service (para transicionar OS para CANCELADA)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoRecusadoEvent implements DomainEvent, Serializable {
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
    private String motivo;

    @JsonProperty
    private String statusMercadoPago;

    @JsonProperty
    private LocalDateTime dataRejeicao;

    @JsonProperty
    private LocalDateTime timestamp;

    public PagamentoRecusadoEvent(
            UUID pagamentoId,
            UUID orcamentoId,
            UUID ordemServicoId,
            UUID clienteId,
            String motivo,
            String statusMercadoPago) {
        this.pagamentoId = pagamentoId;
        this.orcamentoId = orcamentoId;
        this.ordemServicoId = ordemServicoId;
        this.clienteId = clienteId;
        this.motivo = motivo;
        this.statusMercadoPago = statusMercadoPago;
        this.dataRejeicao = LocalDateTime.now();
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "PagamentoRecusadoEvent{" +
                "pagamentoId=" + pagamentoId +
                ", ordemServicoId=" + ordemServicoId +
                ", clienteId=" + clienteId +
                ", motivo='" + motivo + '\'' +
                ", statusMercadoPago='" + statusMercadoPago + '\'' +
                ", dataRejeicao=" + dataRejeicao +
                ", timestamp=" + timestamp +
                '}';
    }
}
