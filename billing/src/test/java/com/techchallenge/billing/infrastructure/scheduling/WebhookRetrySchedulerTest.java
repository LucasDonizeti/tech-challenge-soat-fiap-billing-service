package com.techchallenge.billing.infrastructure.scheduling;

import com.techchallenge.billing.application.gateway.ports.output.WebhookEventLogGateway;
import com.techchallenge.billing.application.gateway.ports.output.WebhookEventProcessingGateway;
import com.techchallenge.billing.domain.models.WebhookEventLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookRetrySchedulerTest {

    @Test
    void retriesPendingWebhookEvents() {
        var logGateway = mock(WebhookEventLogGateway.class);
        var processingGateway = mock(WebhookEventProcessingGateway.class);
        var event = new WebhookEventLog(
                java.util.UUID.randomUUID(),
                "order-id",
                "order.processed",
                LocalDateTime.now(),
                java.util.Map.of(),
                "PENDENTE_RETRY",
                1,
                LocalDateTime.now()
        );
        when(logGateway.findPendingRetriesDueAt(any(LocalDateTime.class))).thenReturn(List.of(event));

        new WebhookRetryScheduler(logGateway, processingGateway).retryPendingWebhooks();

        verify(logGateway).findPendingRetriesDueAt(any(LocalDateTime.class));
        verify(processingGateway).processAsync(event);
    }
}
