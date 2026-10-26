package com.techchallenge.billing.application.gateway.ports.input;

import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.responses.OrcamentoResponse;

public interface CriarOrcamentoInput {

    OrcamentoResponse execute(CriarOrcamentoCommand command);
}
