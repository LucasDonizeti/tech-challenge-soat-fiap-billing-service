package com.techchallenge.billing.application.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,
        UUID orcamentoId,
        UUID ordemServicoId,
        String qrCodePix,
        String qrCodeBase64,
        String status,
        LocalDateTime dataExpiracao,
        LocalDateTime dataPagamento
) {
}
