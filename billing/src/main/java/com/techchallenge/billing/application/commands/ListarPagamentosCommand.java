package com.techchallenge.billing.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ListarPagamentosCommand(@NotNull UUID ordemServicoId) {
}
