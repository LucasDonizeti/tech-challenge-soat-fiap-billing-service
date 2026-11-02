package com.techchallenge.billing.application.gateway.ports.output;

import com.techchallenge.billing.domain.events.SagaPaymentInitiatedEvent;
import com.techchallenge.billing.domain.events.SagaPaymentConfirmedEvent;
import com.techchallenge.billing.domain.events.SagaPaymentExpiredEvent;

/**
 * Gateway para publicação de eventos de Saga
 * Desacopla a aplicação da infraestrutura de mensageria
 */
public interface SagaEventPublisher {
    /**
     * Publica evento quando saga é iniciada
     */
    void publishSagaPaymentInitiated(SagaPaymentInitiatedEvent event);

    /**
     * Publica evento quando saga é confirmada (aprovada, recusada ou expirada)
     */
    void publishSagaPaymentConfirmed(SagaPaymentConfirmedEvent event);

    /**
     * Publica evento quando PIX expira
     */
    void publishSagaPaymentExpired(SagaPaymentExpiredEvent event);
}
