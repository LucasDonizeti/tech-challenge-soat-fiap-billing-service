package com.techchallenge.billing.infrastructure.web.security;

import com.techchallenge.billing.application.commands.ConfirmarPagamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.ConfirmarPagamentoInput;
import com.techchallenge.billing.application.gateway.ports.output.WebhookEventLogGateway;
import com.techchallenge.billing.domain.models.WebhookEventLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoWebhookProcessorTest {

    @Mock
    private ConfirmarPagamentoInput confirmarPagamentoInput;

    @Mock
    private WebhookEventLogGateway webhookEventLogGateway;

    private MercadoPagoWebhookProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MercadoPagoWebhookProcessor(confirmarPagamentoInput, webhookEventLogGateway);
    }

    @Test
    void processesWebhookEventSuccessfully() {
        UUID eventLogId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", "12345");

        WebhookEventLog eventLog = new WebhookEventLog(
                eventLogId,
                mercadoPagoPaymentId,
                "payment.created",
                LocalDateTime.now(),
                payload,
                "PENDENTE",
                0,
                null
        );

        processor.processAsync(eventLog);

        verify(confirmarPagamentoInput).execute(argThat(command ->
                mercadoPagoPaymentId.equals(command.mercadoPagoPaymentId())
        ));
        verify(webhookEventLogGateway).save(argThat(savedLog ->
                "PROCESSADO".equals(savedLog.statusProcessamento()) &&
                savedLog.tentativas() == 1
        ));
    }

    @Test
    void updatesEventLogWithRetryStatusOnProcessingFailure() {
        UUID eventLogId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        Map<String, Object> payload = new HashMap<>();

        WebhookEventLog eventLog = new WebhookEventLog(
                eventLogId,
                mercadoPagoPaymentId,
                "payment.created",
                LocalDateTime.now(),
                payload,
                "PENDENTE",
                0,
                null
        );

        doThrow(new RuntimeException("Processing failed"))
                .when(confirmarPagamentoInput).execute(any(ConfirmarPagamentoCommand.class));

        processor.processAsync(eventLog);

        verify(webhookEventLogGateway).save(argThat(savedLog ->
                "PENDENTE_RETRY".equals(savedLog.statusProcessamento()) &&
                savedLog.tentativas() == 1 &&
                savedLog.proximaRetentativa() != null
        ));
    }

    @Test
    void incrementsAttemptCountOnRetry() {
        UUID eventLogId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        Map<String, Object> payload = new HashMap<>();

        WebhookEventLog eventLog = new WebhookEventLog(
                eventLogId,
                mercadoPagoPaymentId,
                "payment.created",
                LocalDateTime.now(),
                payload,
                "PENDENTE",
                2,
                null
        );

        doThrow(new RuntimeException("Processing failed"))
                .when(confirmarPagamentoInput).execute(any(ConfirmarPagamentoCommand.class));

        processor.processAsync(eventLog);

        verify(webhookEventLogGateway).save(argThat(savedLog ->
                savedLog.tentativas() == 3
        ));
    }

    @Test
    void suppressesPersistenceFailureWhenProcessingFails() {
        UUID eventLogId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        Map<String, Object> payload = new HashMap<>();

        WebhookEventLog eventLog = new WebhookEventLog(
                eventLogId,
                mercadoPagoPaymentId,
                "payment.created",
                LocalDateTime.now(),
                payload,
                "PENDENTE",
                0,
                null
        );

        doThrow(new RuntimeException("Processing failed"))
                .when(confirmarPagamentoInput).execute(any(ConfirmarPagamentoCommand.class));
        doThrow(new RuntimeException("Persistence failed"))
                .when(webhookEventLogGateway).save(any(WebhookEventLog.class));

        processor.processAsync(eventLog);

        verify(confirmarPagamentoInput).execute(any(ConfirmarPagamentoCommand.class));
        verify(webhookEventLogGateway).save(any(WebhookEventLog.class));
    }

    @Test
    void schedulesRetryOneMinuteInFuture() {
        UUID eventLogId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        Map<String, Object> payload = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        WebhookEventLog eventLog = new WebhookEventLog(
                eventLogId,
                mercadoPagoPaymentId,
                "payment.created",
                now,
                payload,
                "PENDENTE",
                0,
                null
        );

        doThrow(new RuntimeException("Processing failed"))
                .when(confirmarPagamentoInput).execute(any(ConfirmarPagamentoCommand.class));

        processor.processAsync(eventLog);

        verify(webhookEventLogGateway).save(argThat(savedLog ->
                savedLog.proximaRetentativa() != null &&
                savedLog.proximaRetentativa().isAfter(now) &&
                savedLog.proximaRetentativa().isBefore(now.plusMinutes(2))
        ));
    }

    @Test
    void doesNotScheduleRetryForProcessedStatus() {
        UUID eventLogId = UUID.randomUUID();
        String mercadoPagoPaymentId = "ORD123";
        Map<String, Object> payload = new HashMap<>();

        WebhookEventLog eventLog = new WebhookEventLog(
                eventLogId,
                mercadoPagoPaymentId,
                "payment.created",
                LocalDateTime.now(),
                payload,
                "PENDENTE",
                0,
                null
        );

        processor.processAsync(eventLog);

        verify(webhookEventLogGateway).save(argThat(savedLog ->
                "PROCESSADO".equals(savedLog.statusProcessamento()) &&
                savedLog.proximaRetentativa() == null
        ));
    }
}
