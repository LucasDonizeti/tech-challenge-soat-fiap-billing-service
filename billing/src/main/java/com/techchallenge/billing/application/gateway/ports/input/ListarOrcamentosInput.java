package com.techchallenge.billing.application.gateway.ports.input;

import com.techchallenge.billing.application.commands.ListarOrcamentosCommand;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import org.springframework.data.domain.Page;

public interface ListarOrcamentosInput {

    Page<OrcamentoResponse> execute(ListarOrcamentosCommand command);
}
