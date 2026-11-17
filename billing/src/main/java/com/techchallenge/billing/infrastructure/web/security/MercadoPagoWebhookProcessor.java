package com.techchallenge.billing.infrastructure.web.security;

import com.techchallenge.billing.application.gateway.ports.output.WebhookEventLogGateway;
import com.techchallenge.billing.application.commands.ConfirmarPagamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.ConfirmarPagamentoInput;
import com.techchallenge.billing.application.gateway.ports.output.WebhookEventProcessingGateway;
import com.techchallenge.billing.domain.models.WebhookEventLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookProcessor implements WebhookEventProcessingGateway {

    private final ConfirmarPagamentoInput confirmarPagamentoInput;
    private final WebhookEventLogGateway webhookEventLogGateway;

    @Override
    @Async("webhookTaskExecutor")
    public void processAsync(WebhookEventLog eventLog) {
        try {
            confirmarPagamentoInput.execute(new ConfirmarPagamentoCommand(eventLog.mercadoPagoPaymentId()));
            webhookEventLogGateway.save(withStatus(eventLog, "PROCESSADO", eventLog.tentativas() + 1));
        } catch (RuntimeException processingFailure) {
            try {
                webhookEventLogGateway.save(withStatus(eventLog, "PENDENTE_RETRY", eventLog.tentativas() + 1));
            } catch (RuntimeException persistenceFailure) {
                processingFailure.addSuppressed(persistenceFailure);
            }
            log.error("Falha ao processar webhook {} para paymentId={}",
                    eventLog.id(), eventLog.mercadoPagoPaymentId(), processingFailure);
        }
    }

    private WebhookEventLog withStatus(WebhookEventLog eventLog, String status, int attempts) {
        return new WebhookEventLog(
                eventLog.id(),
                eventLog.mercadoPagoPaymentId(),
                eventLog.action(),
                eventLog.dataRecebimento(),
                eventLog.payloadOriginal(),
                status,
                attempts,
                status.equals("PENDENTE_RETRY") ? LocalDateTime.now().plusMinutes(1) : null
        );
    }
}
