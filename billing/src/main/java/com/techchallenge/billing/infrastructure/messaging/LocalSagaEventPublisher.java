package com.techchallenge.billing.infrastructure.messaging;

import com.techchallenge.billing.application.gateway.ports.output.SagaEventPublisher;
import com.techchallenge.billing.domain.events.SagaPaymentInitiatedEvent;
import com.techchallenge.billing.domain.events.SagaPaymentExpiredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implementação local de publicador de eventos de Saga (apenas logging, para testes)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "messaging.type", havingValue = "local", matchIfMissing = false)
public class LocalSagaEventPublisher implements SagaEventPublisher {

    @Override
    public void publishSagaPaymentInitiated(SagaPaymentInitiatedEvent event) {
        log.info("[LOCAL] SagaPaymentInitiatedEvent: {}", event);
    }

    @Override
    public void publishSagaPaymentExpired(SagaPaymentExpiredEvent event) {
        log.info("[LOCAL] SagaPaymentExpiredEvent: {}", event);
    }
}
