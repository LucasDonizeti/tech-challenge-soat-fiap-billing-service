package com.techchallenge.billing.domain.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrcamentoProntoEvent(
        UUID ordemServicoId,
        UUID clienteId,
        UUID veiculoId,
        BigDecimal valorTotal,
        String descricaoServicos,
        LocalDateTime dataOrcamento,
        String payerEmail,
        String payerIdentificationType,
        String payerIdentificationNumber
) implements DomainEvent {

    @Override
    public LocalDateTime getTimestamp() {
        return dataOrcamento;
    }
}
