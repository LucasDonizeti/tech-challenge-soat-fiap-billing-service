package com.techchallenge.billing.application.gateway.ports.input;

import com.techchallenge.billing.application.commands.ConfirmarPagamentoCommand;

public interface ConfirmarPagamentoInput {

    void execute(ConfirmarPagamentoCommand command);
}
