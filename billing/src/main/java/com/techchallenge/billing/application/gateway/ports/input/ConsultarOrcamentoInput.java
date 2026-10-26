package com.techchallenge.billing.application.gateway.ports.input;

import com.techchallenge.billing.application.commands.ConsultarOrcamentoCommand;
import com.techchallenge.billing.application.responses.OrcamentoResponse;

public interface ConsultarOrcamentoInput {

    OrcamentoResponse execute(ConsultarOrcamentoCommand command);
}
