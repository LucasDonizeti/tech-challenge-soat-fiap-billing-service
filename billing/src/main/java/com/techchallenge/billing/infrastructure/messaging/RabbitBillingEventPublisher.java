package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.domain.events.CobrancaPixGeradaEvent;
import com.techchallenge.billing.domain.events.PagamentoAprovadoEvent;
import com.techchallenge.billing.domain.events.PagamentoRecusadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbit")
public class RabbitBillingEventPublisher implements BillingEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${messaging.rabbit.exchange}")
    private String exchange;

    @Value("${messaging.rabbit.routing-keys.cobranca-pix-gerada}")
    private String cobrancaPixGeradaRoutingKey;

    @Value("${messaging.rabbit.routing-keys.pagamento-aprovado}")
    private String pagamentoAprovadoRoutingKey;

    @Value("${messaging.rabbit.routing-keys.pagamento-recusado}")
    private String pagamentoRecusadoRoutingKey;

    @Override
    public void publishCobrancaPixGerada(CobrancaPixGeradaEvent event) {
        publish(event, cobrancaPixGeradaRoutingKey);
    }

    @Override
    public void publishPagamentoAprovado(PagamentoAprovadoEvent event) {
        publish(event, pagamentoAprovadoRoutingKey);
    }

    @Override
    public void publishPagamentoRecusado(PagamentoRecusadoEvent event) {
        publish(event, pagamentoRecusadoRoutingKey);
    }

    private void publish(Object event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, objectMapper.writeValueAsString(event));
            log.info("Evento {} publicado no RabbitMQ com routingKey={}",
                    event.getClass().getSimpleName(), routingKey);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar evento de billing.", exception);
        }
    }
}
