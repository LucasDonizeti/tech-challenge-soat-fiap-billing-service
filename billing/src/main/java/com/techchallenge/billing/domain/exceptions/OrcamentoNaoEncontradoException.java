package com.techchallenge.billing.domain.exceptions;

import java.util.UUID;

public class OrcamentoNaoEncontradoException extends DomainException {

    public OrcamentoNaoEncontradoException(UUID id) {
        super("Orçamento não encontrado com ID: " + id, "ORCAMENTO_NAO_ENCONTRADO");
    }
}
