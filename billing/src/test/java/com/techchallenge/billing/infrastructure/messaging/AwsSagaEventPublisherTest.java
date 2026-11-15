package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.domain.events.SagaPaymentExpiredEvent;
import com.techchallenge.billing.domain.events.SagaPaymentInitiatedEvent;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AwsSagaEventPublisherTest {

    private SnsTemplate snsTemplate;
    private AwsSagaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        snsTemplate = mock(SnsTemplate.class);
        publisher = new AwsSagaEventPublisher(snsTemplate, new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(publisher, "sagaInitiatedTopic", "saga-initiated-topic-arn");
        ReflectionTestUtils.setField(publisher, "sagaExpiredTopic", "saga-expired-topic-arn");
    }

    @Test
    void shouldPublishSagaInitiatedEventToConfiguredTopic() throws Exception {
        var event = new SagaPaymentInitiatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "mp-payment-id", "pix", "base64",
                java.math.BigDecimal.TEN, LocalDateTime.now());
        String payload = new ObjectMapper().findAndRegisterModules().writeValueAsString(event);

        publisher.publishSagaPaymentInitiated(event);

        verify(snsTemplate).convertAndSend(
                eq("saga-initiated-topic-arn"),
                eq(payload),
                eq(Map.of("EventType", "SagaPaymentInitiatedEvent")));
    }

    @Test
    void shouldPublishSagaExpiredEventToConfiguredTopic() throws Exception {
        var event = new SagaPaymentExpiredEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "mp-payment-id", LocalDateTime.now());
        String payload = new ObjectMapper().findAndRegisterModules().writeValueAsString(event);

        publisher.publishSagaPaymentExpired(event);

        verify(snsTemplate).convertAndSend(
                eq("saga-expired-topic-arn"),
                eq(payload),
                eq(Map.of("EventType", "SagaPaymentExpiredEvent")));
    }
}
