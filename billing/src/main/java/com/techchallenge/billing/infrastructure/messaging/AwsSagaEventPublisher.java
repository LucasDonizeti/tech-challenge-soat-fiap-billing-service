package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.gateway.ports.output.SagaEventPublisher;
import com.techchallenge.billing.domain.events.SagaPaymentExpiredEvent;
import com.techchallenge.billing.domain.events.SagaPaymentInitiatedEvent;
import io.awspring.cloud.sns.core.SnsTemplate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class AwsSagaEventPublisher implements SagaEventPublisher {

    private final SnsTemplate snsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.topics.saga-initiated:}")
    private String sagaInitiatedTopic;

    @Value("${aws.sns.topics.saga-expired:}")
    private String sagaExpiredTopic;

    @PostConstruct
    void validateTopics() {
        requireTopic(sagaInitiatedTopic, "AWS_SNS_TOPIC_SAGA_INITIATED");
        requireTopic(sagaExpiredTopic, "AWS_SNS_TOPIC_SAGA_EXPIRED");
    }

    private void requireTopic(String topicArn, String environmentVariable) {
        if (topicArn == null || topicArn.isBlank()) {
            throw new IllegalStateException(
                    "Configure " + environmentVariable + " com o ARN do tópico SNS.");
        }
    }

    @Override
    public void publishSagaPaymentInitiated(SagaPaymentInitiatedEvent event) {
        publish(event, sagaInitiatedTopic);
    }

    @Override
    public void publishSagaPaymentExpired(SagaPaymentExpiredEvent event) {
        publish(event, sagaExpiredTopic);
    }

    private void publish(Object event, String topicArn) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            snsTemplate.convertAndSend(topicArn, payload,
                    Map.of("EventType", event.getClass().getSimpleName()));
            log.info("{} publicado no SNS", event.getClass().getSimpleName());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar evento de Saga.", exception);
        } catch (RuntimeException exception) {
            log.error("Falha ao publicar {} no SNS", event.getClass().getSimpleName(), exception);
            throw exception;
        }
    }
}
