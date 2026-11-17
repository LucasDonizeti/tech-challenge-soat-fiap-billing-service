package com.techchallenge.billing.application.usecases.assemblers;

import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.domain.models.Orcamento;
import org.springframework.stereotype.Component;

@Component
public class OrcamentoResponseAssembler {

    public OrcamentoResponse toResponse(Orcamento orcamento) {
        return new OrcamentoResponse(
                orcamento.getId(),
                orcamento.getOsId(),
                orcamento.getClienteId(),
                orcamento.getValorTotal(),
                orcamento.getStatus().name(),
                orcamento.getDataCriacao(),
                orcamento.getDataAtualizacao()
        );
    }
}
