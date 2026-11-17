package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ConsultarOrcamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.ConsultarOrcamentoInput;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.application.usecases.assemblers.OrcamentoResponseAssembler;
import com.techchallenge.billing.domain.exceptions.OrcamentoNaoEncontradoException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultarOrcamentoUseCase implements ConsultarOrcamentoInput {

    private final OrcamentoGateway orcamentoGateway;
    private final OrcamentoResponseAssembler responseAssembler;
    private final Validator validator;

    @Override
    public OrcamentoResponse execute(ConsultarOrcamentoCommand command) {
        UseCaseValidation.validate(command, validator);
        return orcamentoGateway.findById(command.orcamentoId())
                .map(responseAssembler::toResponse)
                .orElseThrow(() -> new OrcamentoNaoEncontradoException(command.orcamentoId()));
    }
}
