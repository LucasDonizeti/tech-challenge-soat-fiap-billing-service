package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ProcessarWebhookCommand;
import com.techchallenge.billing.application.gateway.ports.output.WebhookEventLogGateway;
import com.techchallenge.billing.application.gateway.ports.output.WebhookEventProcessingGateway;
import com.techchallenge.billing.domain.models.WebhookEventLog;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessarWebhookUseCaseTest {

    @Mock
    private WebhookEventLogGateway webhookEventLogGateway;

    @Mock
    private WebhookEventProcessingGateway webhookEventProcessingGateway;

    private ValidatorFactory validatorFactory;
    private ProcessarWebhookUseCase useCase;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        useCase = new ProcessarWebhookUseCase(
                webhookEventLogGateway,
                webhookEventProcessingGateway,
                validatorFactory.getValidator()
        );
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void persistsPendingWebhookBeforeStartingAsyncProcessing() {
        var receivedAt = LocalDateTime.now();
        var command = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                receivedAt,
                Map.of("type", "payment", "data_id", "mp-payment-123")
        );
        when(webhookEventLogGateway.save(any(WebhookEventLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command);

        var eventLog = ArgumentCaptor.forClass(WebhookEventLog.class);
        verify(webhookEventLogGateway).save(eventLog.capture());
        assertEquals("PENDENTE", eventLog.getValue().statusProcessamento());
        assertEquals(0, eventLog.getValue().tentativas());
        assertEquals(command.mercadoPagoPaymentId(), eventLog.getValue().mercadoPagoPaymentId());
        assertEquals(command.action(), eventLog.getValue().action());
        assertEquals(command.dataRecebimento(), eventLog.getValue().dataRecebimento());
        assertEquals(command.payloadOriginal(), eventLog.getValue().payloadOriginal());
        verify(webhookEventProcessingGateway).processAsync(eventLog.getValue());
    }

    @Test
    void rejectsWebhookWithoutPaymentIdBeforePersistingOrDispatching() {
        var command = new ProcessarWebhookCommand(
                " ",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));

        verify(webhookEventLogGateway, never()).save(any());
        verify(webhookEventProcessingGateway, never()).processAsync(any());
    }
}
