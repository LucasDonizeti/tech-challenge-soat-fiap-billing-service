package com.techchallenge.billing.infrastructure.web.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.commands.ProcessarWebhookCommand;
import com.techchallenge.billing.application.gateway.ports.input.ProcessarWebhookInput;
import com.techchallenge.billing.infrastructure.external.dto.MercadoPagoWebhookNotification;
import com.techchallenge.billing.infrastructure.web.security.MercadoPagoWebhookSignatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - WebhookMercadoPagoController")
class WebhookMercadoPagoControllerTest {

    @Mock
    private MercadoPagoWebhookSignatureValidator signatureValidator;

    @Mock
    private ProcessarWebhookInput processarWebhookInput;

    @Mock
    private ObjectMapper objectMapper;

    private WebhookMercadoPagoController controller;

    @BeforeEach
    void setUp() {
        controller = new WebhookMercadoPagoController(signatureValidator, processarWebhookInput, objectMapper);
    }

    @Test
    @DisplayName("Deve processar webhook com assinatura válida")
    void deveProcessarWebhookComAssinaturaValida() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        when(signatureValidator.isValid(any(), any(), any())).thenReturn(true);
        when(objectMapper.convertValue(any(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(Map.of("key", "value"));

        ResponseEntity<WebhookMercadoPagoController.WebhookReceipt> response = controller.receive(
                "signature",
                "request-id",
                notification
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("received", response.getBody().status());
        verify(processarWebhookInput).execute(any(ProcessarWebhookCommand.class));
    }

    @Test
    @DisplayName("Deve rejeitar webhook com assinatura inválida")
    void deveRejeitarWebhookComAssinaturaInvalida() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        when(signatureValidator.isValid(any(), any(), any())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("invalid-signature", "request-id", notification)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Assinatura do webhook inválida.", exception.getReason());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve rejeitar webhook sem paymentId")
    void deveRejeitarWebhookSemPaymentId() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                null,
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data(null)
        );

        when(signatureValidator.isValid(any(), any(), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", notification)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Notificação de pagamento inválida.", exception.getReason());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve rejeitar webhook com tipo diferente de payment")
    void deveRejeitarWebhookComTipoDiferenteDePayment() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.updated",
                "payment-123",
                1234567890L,
                "other-type",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        when(signatureValidator.isValid(any(), any(), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", notification)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Notificação de pagamento inválida.", exception.getReason());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve rejeitar webhook com action inválido")
    void deveRejeitarWebhookComActionInvalido() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "invalid-action",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        when(signatureValidator.isValid(any(), any(), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", notification)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Notificação de pagamento inválida.", exception.getReason());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve processar webhook com action válido")
    void deveProcessarWebhookComActionValido() {
        MercadoPagoWebhookNotification notification = new MercadoPagoWebhookNotification(
                "payment.created",
                "payment-123",
                1234567890L,
                "payment",
                new MercadoPagoWebhookNotification.Data("payment-456")
        );

        when(signatureValidator.isValid(any(), any(), any())).thenReturn(true);
        when(objectMapper.convertValue(any(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(Map.of("key", "value"));

        ResponseEntity<WebhookMercadoPagoController.WebhookReceipt> response = controller.receive(
                "signature",
                "request-id",
                notification
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(processarWebhookInput).execute(any(ProcessarWebhookCommand.class));

        ArgumentCaptor<ProcessarWebhookCommand> commandCaptor = ArgumentCaptor.forClass(ProcessarWebhookCommand.class);
        verify(processarWebhookInput).execute(commandCaptor.capture());
        assertEquals("payment.created", commandCaptor.getValue().action());
    }

    @Test
    @DisplayName("Deve ter RequestMapping anotação correta")
    void deveTerRequestMappingAnotacaoCorreta() {
        RequestMapping mapping = controller.getClass().getAnnotation(RequestMapping.class);

        assertNotNull(mapping);
        assertEquals("/api/v1/billings/webhooks/mercadopago", mapping.value()[0]);
    }
}
