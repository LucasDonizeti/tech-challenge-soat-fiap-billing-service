package com.techchallenge.billing.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbit")
public class RabbitMessagingConfig {

    @Bean
    TopicExchange billingEventsExchange(@Value("${messaging.rabbit.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue billingOrcamentoProntoQueue(
            @Value("${messaging.rabbit.queues.orcamento-pronto}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding billingOrcamentoProntoBinding(
            Queue billingOrcamentoProntoQueue,
            TopicExchange billingEventsExchange,
            @Value("${messaging.rabbit.routing-keys.orcamento-pronto}") String routingKey) {
        return BindingBuilder.bind(billingOrcamentoProntoQueue)
                .to(billingEventsExchange)
                .with(routingKey);
    }
}
