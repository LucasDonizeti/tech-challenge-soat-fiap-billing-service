package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.gateway.ports.output.SagaEventPublisher;
import com.techchallenge.billing.domain.events.SagaPaymentInitiatedEvent;
import com.techchallenge.billing.domain.events.SagaPaymentExpiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implementação de publicador de eventos de Saga usando RabbitMQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbit")
public class RabbitSagaEventPublisher implements SagaEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${messaging.rabbit.exchange:billing.events}")
    private String exchange;

    @Value("${messaging.rabbit.routing-keys.saga-initiated:saga.payment.initiated}")
    private String sagaInitiatedRoutingKey;

    @Value("${messaging.rabbit.routing-keys.saga-expired:saga.payment.expired}")
    private String sagaExpiredRoutingKey;

    @Override
    public void publishSagaPaymentInitiated(SagaPaymentInitiatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, sagaInitiatedRoutingKey, message);
            log.info("SagaPaymentInitiatedEvent publicado: sagaId={}, ordemServicoId={}",
                    event.getSagaId(), event.getOrdemServicoId());
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar SagaPaymentInitiatedEvent", e);
            throw new RuntimeException("Falha ao publicar evento de saga iniciada", e);
        }
    }

    @Override
    public void publishSagaPaymentExpired(SagaPaymentExpiredEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, sagaExpiredRoutingKey, message);
            log.info("SagaPaymentExpiredEvent publicado: sagaId={}, ordemServicoId={}",
                    event.getSagaId(), event.getOrdemServicoId());
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar SagaPaymentExpiredEvent", e);
            throw new RuntimeException("Falha ao publicar evento de saga expirada", e);
        }
    }
}
