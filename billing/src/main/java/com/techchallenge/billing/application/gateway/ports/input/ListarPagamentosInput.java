package com.techchallenge.billing.application.gateway.ports.input;

import com.techchallenge.billing.application.commands.ListarPagamentosCommand;
import com.techchallenge.billing.application.responses.PagamentoResponse;

import java.util.List;

public interface ListarPagamentosInput {

    List<PagamentoResponse> execute(ListarPagamentosCommand command);
}
