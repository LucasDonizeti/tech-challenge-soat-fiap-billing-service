package com.techchallenge.billing.application.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CriarOrcamentoCommand(
        @NotNull UUID ordemServicoId,
        @NotNull UUID clienteId,
        @NotNull UUID veiculoId,
        @NotNull @Positive BigDecimal valorTotal,
        @NotBlank String descricaoServicos,
        LocalDateTime dataOrcamento,
        @NotBlank @Email String payerEmail,
        @NotBlank String payerIdentificationType,
        @NotBlank String payerIdentificationNumber
) {
}
