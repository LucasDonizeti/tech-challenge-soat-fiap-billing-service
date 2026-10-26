package com.techchallenge.billing.domain.exceptions;

import java.util.UUID;

public class PagamentoNaoEncontradoException extends DomainException {

    public PagamentoNaoEncontradoException(UUID id) {
        super("Pagamento não encontrado com ID: " + id, "PAGAMENTO_NAO_ENCONTRADO");
    }
}
