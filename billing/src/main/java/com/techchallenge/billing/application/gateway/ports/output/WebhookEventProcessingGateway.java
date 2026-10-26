package com.techchallenge.billing.application.gateway.ports.output;

import com.techchallenge.billing.domain.models.WebhookEventLog;

public interface WebhookEventProcessingGateway {

    void processAsync(WebhookEventLog eventLog);
}
