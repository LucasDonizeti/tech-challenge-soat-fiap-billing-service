package com.techchallenge.billing.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "messaging.type", havingValue = "aws")
public class AwsSqsConfig {

    @Value("${aws.sqs.queues.orcamento-pronto:}")
    private String queue;

    @PostConstruct
    void validateQueue() {
        if (queue == null || queue.isBlank()) {
            throw new IllegalStateException(
                    "Configure AWS_SQS_QUEUE_ORCAMENTO_PRONTO para iniciar o listener SQS.");
        }
    }
}
