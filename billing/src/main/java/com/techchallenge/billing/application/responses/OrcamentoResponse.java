package com.techchallenge.billing.application.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrcamentoResponse(
        UUID id,
        UUID ordemServicoId,
        UUID clienteId,
        BigDecimal valorTotal,
        String status,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
