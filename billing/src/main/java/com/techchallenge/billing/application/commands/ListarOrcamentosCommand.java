package com.techchallenge.billing.application.commands;

import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record ListarOrcamentosCommand(
        @NotNull UUID clienteId,
        StatusOrcamento status,
        @NotNull Pageable pageable
) {
}
