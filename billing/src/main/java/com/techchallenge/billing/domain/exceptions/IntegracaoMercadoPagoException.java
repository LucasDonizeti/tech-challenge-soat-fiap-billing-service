package com.techchallenge.billing.domain.exceptions;

import java.util.UUID;

public class IntegracaoMercadoPagoException extends DomainException {

    public IntegracaoMercadoPagoException(String message) {
        super("Erro na integração com Mercado Pago: " + message, "ERRO_INTEGRACAO_MERCADO_PAGO");
    }

    public IntegracaoMercadoPagoException(String message, Throwable cause) {
        super("Erro na integração com Mercado Pago: " + message, "ERRO_INTEGRACAO_MERCADO_PAGO", cause);
    }
}
