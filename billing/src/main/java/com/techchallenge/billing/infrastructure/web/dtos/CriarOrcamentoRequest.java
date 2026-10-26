package com.techchallenge.billing.infrastructure.web.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.UUID;

public record CriarOrcamentoRequest(
        @NotNull UUID ordemServicoId,
        @NotNull UUID clienteId,
        @NotNull UUID veiculoId,
        @NotNull @Positive BigDecimal valorTotal,
        @NotBlank String descricaoServicos,
        @NotBlank @Email String payerEmail,
        @NotBlank String payerIdentificationType,
        @NotBlank String payerIdentificationNumber
) {
}
