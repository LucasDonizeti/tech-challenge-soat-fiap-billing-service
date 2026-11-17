package com.techchallenge.billing.infrastructure.scheduling;

import com.techchallenge.billing.application.gateway.ports.output.WebhookEventLogGateway;
import com.techchallenge.billing.application.gateway.ports.output.WebhookEventProcessingGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "billing.webhook.retry.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class WebhookRetryScheduler {

    private final WebhookEventLogGateway webhookEventLogGateway;
    private final WebhookEventProcessingGateway webhookEventProcessingGateway;

    @Scheduled(fixedDelayString = "${billing.webhook.retry.interval:60000}")
    public void retryPendingWebhooks() {
        try {
            var pendingRetries = webhookEventLogGateway.findPendingRetriesDueAt(LocalDateTime.now());
            pendingRetries.forEach(webhookEventProcessingGateway::processAsync);
            if (!pendingRetries.isEmpty()) {
                log.info("Agendadas {} tentativas de reprocessamento de webhook", pendingRetries.size());
            }
        } catch (RuntimeException exception) {
            log.error("Falha ao buscar webhooks pendentes de reprocessamento", exception);
        }
    }
}
