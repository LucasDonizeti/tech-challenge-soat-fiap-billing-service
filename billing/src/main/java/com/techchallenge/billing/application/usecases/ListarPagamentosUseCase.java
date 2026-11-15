package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ListarPagamentosCommand;
import com.techchallenge.billing.application.gateway.ports.input.ListarPagamentosInput;
import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.application.responses.PagamentoResponse;
import com.techchallenge.billing.application.usecases.assemblers.PagamentoResponseAssembler;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarPagamentosUseCase implements ListarPagamentosInput {

    private final PagamentoGateway pagamentoGateway;
    private final PagamentoResponseAssembler responseAssembler;
    private final Validator validator;

    @Override
    public List<PagamentoResponse> execute(ListarPagamentosCommand command) {
        UseCaseValidation.validate(command, validator);
        return pagamentoGateway.findByOsId(command.ordemServicoId()).stream()
                .map(responseAssembler::toResponse)
                .toList();
    }
}
