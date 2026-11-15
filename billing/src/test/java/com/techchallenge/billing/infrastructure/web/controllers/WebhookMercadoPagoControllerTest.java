package com.techchallenge.billing.infrastructure.web.controllers;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - WebhookMercadoPagoController")
class WebhookMercadoPagoControllerTest {
    /*

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
        MercadoPagoWebhookNotification notification = notification("payment.updated", "payment", "payment-123");

        when(signatureValidator.isValid(any(), any(), any(), any())).thenReturn(true);
        when(objectMapper.convertValue(any(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(Map.of("key", "value"));

        ResponseEntity<WebhookMercadoPagoController.WebhookReceipt> response = controller.receive(
                "signature",
                "request-id",
                "payment-123",
                notification
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("received", response.getBody().status());
        verify(processarWebhookInput).execute(any(ProcessarWebhookCommand.class));
    }

    @Test
    @DisplayName("Deve rejeitar webhook com assinatura inválida")
    void deveRejeitarWebhookComAssinaturaInvalida() {
        MercadoPagoWebhookNotification notification = notification("payment.updated", "payment", "payment-123");

        when(signatureValidator.isValid(any(), any(), any(), any())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("invalid-signature", "request-id", "payment-123", notification)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Assinatura do webhook inválida.", exception.getReason());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve rejeitar webhook sem paymentId")
    void deveRejeitarWebhookSemPaymentId() {
        MercadoPagoWebhookNotification notification = notification("payment.updated", "payment", null);

        when(signatureValidator.isValid(any(), any(), any(), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", null, notification)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Notificação de pagamento inválida.", exception.getReason());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve rejeitar webhook com tipo diferente de payment")
    void deveRejeitarWebhookComTipoDiferenteDePayment() {
        MercadoPagoWebhookNotification notification = notification("payment.updated", "other-type", "payment-123");

        when(signatureValidator.isValid(any(), any(), any(), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", "payment-123", notification)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Notificação de pagamento inválida.", exception.getReason());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve rejeitar webhook com action inválido")
    void deveRejeitarWebhookComActionInvalido() {
        MercadoPagoWebhookNotification notification = notification("invalid-action", "payment", "payment-123");

        when(signatureValidator.isValid(any(), any(), any(), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", "payment-123", notification)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Notificação de pagamento inválida.", exception.getReason());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve processar webhook com action válido")
    void deveProcessarWebhookComActionValido() {
        MercadoPagoWebhookNotification notification = notification("payment.created", "payment", "payment-123");

        when(signatureValidator.isValid(any(), any(), any(), any())).thenReturn(true);
        when(objectMapper.convertValue(any(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(Map.of("key", "value"));

        ResponseEntity<WebhookMercadoPagoController.WebhookReceipt> response = controller.receive(
                "signature",
                "request-id",
                "payment-123",
                notification
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(processarWebhookInput).execute(any(ProcessarWebhookCommand.class));

        ArgumentCaptor<ProcessarWebhookCommand> commandCaptor = ArgumentCaptor.forClass(ProcessarWebhookCommand.class);
        verify(processarWebhookInput).execute(commandCaptor.capture());
        assertEquals("payment.created", commandCaptor.getValue().action());
        verify(signatureValidator).isValid(notification, "payment-123", "signature", "request-id");
    }

    @Test
    @DisplayName("Deve rejeitar webhook quando data.id da URL difere do body")
    void deveRejeitarWebhookQuandoDataIdDiferenteDoBody() {
        MercadoPagoWebhookNotification notification = notification("payment.updated", "payment", "body-payment-id");

        when(signatureValidator.isValid(any(), any(), any(), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", "signed-payment-id", notification)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve ter RequestMapping anotação correta")
    void deveTerRequestMappingAnotacaoCorreta() {
        RequestMapping mapping = controller.getClass().getAnnotation(RequestMapping.class);

        assertNotNull(mapping);
        assertEquals("/api/v1/billings/webhooks/mercadopago", mapping.value()[0]);
    }

    private MercadoPagoWebhookNotification notification(String action, String type, String paymentId) {
        return new MercadoPagoWebhookNotification(
                action,
                null,
                1234567890L,
                "2026-11-01T15:00:00Z",
                type,
                paymentId == null ? null : new MercadoPagoWebhookNotification.Data(paymentId),
                "v1",
                987654321L,
                true,
                "user-123"
        );
    }

     */

    @Test
    @DisplayName("Aceita order.processed e agenda a consulta da order recebida")
    void acceptsProcessedOrderNotification() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var objectMapper = new ObjectMapper();
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, objectMapper);
        String orderId = "ORDTST01M6EXRT5E37H8P5VX32YF6E0K";
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);
        when(signatureValidator.isValid(
                any(MercadoPagoWebhookNotification.class),
                eq(orderId),
                eq("signature"),
                eq("request-id")
        )).thenReturn(true);

        ResponseEntity<WebhookMercadoPagoController.WebhookReceipt> response = controller.receive(
                "signature", "request-id", orderId, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<ProcessarWebhookCommand> command = ArgumentCaptor.forClass(ProcessarWebhookCommand.class);
        verify(processarWebhookInput).execute(command.capture());
        assertEquals(orderId, command.getValue().mercadoPagoPaymentId());
        assertEquals("order.processed", command.getValue().action());
    }

    @Test
    @DisplayName("Rejeita order cujo ID do body diverge do data.id da URL")
    void rejectsOrderIdDifferentFromSignedDataId() {
        var controller = new WebhookMercadoPagoController(
                mock(MercadoPagoWebhookSignatureValidator.class),
                mock(ProcessarWebhookInput.class),
                new ObjectMapper());
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "order-in-body"}
                }
                """;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", "signed-order-id", body));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("Rejeita webhook sem os dados necessários para validar a assinatura")
    void rejectsWebhookWithoutSignatureHeaders() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORDTST01M6EXRT5E37H8P5VX32YF6E0K";
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive(null, null, orderId, body));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook com JSON inválido")
    void rejectsWebhookWithInvalidJson() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String invalidJson = "{invalid json";

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", "order-id", invalidJson));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook com action inválido")
    void rejectsWebhookWithInvalidAction() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "payment.created",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", orderId, body));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook com type inválido")
    void rejectsWebhookWithInvalidType() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.processed",
                  "type": "payment",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", orderId, body));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook sem orderId")
    void rejectsWebhookWithoutOrderId() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {}
                }
                """;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", "order-id", body));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook com orderId em branco")
    void rejectsWebhookWithBlankOrderId() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "   "}
                }
                """;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", "order-id", body));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook quando signature está ausente")
    void rejectsWebhookWithoutSignature() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive(null, "request-id", orderId, body));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook quando requestId está ausente")
    void rejectsWebhookWithoutRequestId() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", null, orderId, body));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook quando signedDataId está ausente")
    void rejectsWebhookWithoutSignedDataId() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", null, body));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(signatureValidator, never()).isValid(any(), any(), any(), any());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook quando assinatura é inválida")
    void rejectsWebhookWithInvalidSignature() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        when(signatureValidator.isValid(any(), eq(orderId), eq("invalid-signature"), eq("request-id")))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("invalid-signature", "request-id", orderId, body));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Rejeita webhook quando validação de assinatura lança exceção")
    void rejectsWebhookWhenSignatureValidationThrowsException() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        when(signatureValidator.isValid(any(), eq(orderId), eq("signature"), eq("request-id")))
                .thenThrow(new RuntimeException("Validation error"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.receive("signature", "request-id", orderId, body));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(processarWebhookInput, never()).execute(any());
    }

    @Test
    @DisplayName("Propaga exceção quando processamento do webhook falha")
    void propagatesExceptionWhenWebhookProcessingFails() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.processed",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        when(signatureValidator.isValid(any(), eq(orderId), eq("signature"), eq("request-id")))
                .thenReturn(true);
        doThrow(new RuntimeException("Processing failed"))
                .when(processarWebhookInput).execute(any(ProcessarWebhookCommand.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                controller.receive("signature", "request-id", orderId, body));

        assertEquals("Processing failed", exception.getMessage());
    }

    @Test
    @DisplayName("Aceita order.updated")
    void acceptsOrderUpdatedAction() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.updated",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        when(signatureValidator.isValid(any(), eq(orderId), eq("signature"), eq("request-id")))
                .thenReturn(true);

        ResponseEntity<WebhookMercadoPagoController.WebhookReceipt> response =
                controller.receive("signature", "request-id", orderId, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(processarWebhookInput).execute(any(ProcessarWebhookCommand.class));
    }

    @Test
    @DisplayName("Aceita order.canceled")
    void acceptsOrderCanceledAction() {
        var signatureValidator = mock(MercadoPagoWebhookSignatureValidator.class);
        var processarWebhookInput = mock(ProcessarWebhookInput.class);
        var controller = new WebhookMercadoPagoController(
                signatureValidator, processarWebhookInput, new ObjectMapper());
        String orderId = "ORD123";
        String body = """
                {
                  "action": "order.canceled",
                  "type": "order",
                  "data": {"id": "%s"}
                }
                """.formatted(orderId);

        when(signatureValidator.isValid(any(), eq(orderId), eq("signature"), eq("request-id")))
                .thenReturn(true);

        ResponseEntity<WebhookMercadoPagoController.WebhookReceipt> response =
                controller.receive("signature", "request-id", orderId, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(processarWebhookInput).execute(any(ProcessarWebhookCommand.class));
    }

    @Test
    @DisplayName("WebhookReceipt rejeita status vazio")
    void webhookReceiptRejectsBlankStatus() {
        assertThrows(IllegalArgumentException.class, () ->
                new WebhookMercadoPagoController.WebhookReceipt("")
        );
    }

    @Test
    @DisplayName("WebhookReceipt rejeita status nulo")
    void webhookReceiptRejectsNullStatus() {
        assertThrows(IllegalArgumentException.class, () ->
                new WebhookMercadoPagoController.WebhookReceipt(null)
        );
    }

    @Test
    @DisplayName("WebhookReceipt aceita status válido")
    void webhookReceiptAcceptsValidStatus() {
        WebhookMercadoPagoController.WebhookReceipt receipt =
                new WebhookMercadoPagoController.WebhookReceipt("received");

        assertEquals("received", receipt.status());
    }
}
