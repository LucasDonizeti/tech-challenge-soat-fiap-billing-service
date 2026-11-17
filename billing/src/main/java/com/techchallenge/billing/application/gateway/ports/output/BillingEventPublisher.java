package com.techchallenge.billing.application.gateway.ports.output;

import com.techchallenge.billing.domain.events.CobrancaPixGeradaEvent;
import com.techchallenge.billing.domain.events.PagamentoAprovadoEvent;
import com.techchallenge.billing.domain.events.PagamentoRecusadoEvent;

/**
 * Event Publisher Interface - Fase A
 * Abstraction for publishing domain events
 * Implementations: LocalEventPublisher (mock), AwsSnsEventPublisher (AWS SNS)
 */
public interface BillingEventPublisher {

    /**
     * Publica CobrancaPixGeradaEvent
     */
    void publishCobrancaPixGerada(CobrancaPixGeradaEvent event);

    /**
     * Publica PagamentoAprovadoEvent
     */
    void publishPagamentoAprovado(PagamentoAprovadoEvent event);

    /**
     * Publica PagamentoRecusadoEvent
     */
    void publishPagamentoRecusado(PagamentoRecusadoEvent event);
}
