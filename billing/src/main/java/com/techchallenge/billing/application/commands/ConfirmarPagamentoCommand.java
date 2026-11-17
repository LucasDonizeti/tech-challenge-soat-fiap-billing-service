package com.techchallenge.billing.application.commands;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarPagamentoCommand(@NotBlank String mercadoPagoPaymentId) {
}
