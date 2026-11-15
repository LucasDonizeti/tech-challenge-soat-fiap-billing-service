package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ProcessarWebhookCommand;
import com.techchallenge.billing.application.gateway.ports.input.ProcessarWebhookInput;
import com.techchallenge.billing.application.gateway.ports.output.WebhookEventLogGateway;
import com.techchallenge.billing.application.gateway.ports.output.WebhookEventProcessingGateway;
import com.techchallenge.billing.domain.models.WebhookEventLog;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessarWebhookUseCase implements ProcessarWebhookInput {

    private final WebhookEventLogGateway webhookEventLogGateway;
    private final WebhookEventProcessingGateway webhookEventProcessingGateway;
    private final Validator validator;

    @Override
    public void execute(ProcessarWebhookCommand command) {
        UseCaseValidation.validate(command, validator);
        var eventLog = webhookEventLogGateway.save(new WebhookEventLog(
                UUID.randomUUID(),
                command.mercadoPagoPaymentId(),
                command.action(),
                command.dataRecebimento(),
                command.payloadOriginal(),
                "PENDENTE",
                0,
                null
        ));
        webhookEventProcessingGateway.processAsync(eventLog);
    }
}
