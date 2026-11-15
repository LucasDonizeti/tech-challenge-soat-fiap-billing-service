package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ListarOrcamentosCommand;
import com.techchallenge.billing.application.gateway.ports.input.ListarOrcamentosInput;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.application.usecases.assemblers.OrcamentoResponseAssembler;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarOrcamentosUseCase implements ListarOrcamentosInput {

    private final OrcamentoGateway orcamentoGateway;
    private final OrcamentoResponseAssembler responseAssembler;
    private final Validator validator;

    @Override
    public Page<OrcamentoResponse> execute(ListarOrcamentosCommand command) {
        UseCaseValidation.validate(command, validator);
        return orcamentoGateway.findPageByClienteIdAndStatus(
                        command.clienteId(), command.status(), command.pageable())
                .map(responseAssembler::toResponse);
    }
}
