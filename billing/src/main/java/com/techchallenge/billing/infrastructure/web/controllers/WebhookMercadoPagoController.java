package com.techchallenge.billing.infrastructure.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.techchallenge.billing.application.commands.ProcessarWebhookCommand;
import com.techchallenge.billing.application.gateway.ports.input.ProcessarWebhookInput;
import com.techchallenge.billing.infrastructure.external.dto.MercadoPagoWebhookNotification;
import com.techchallenge.billing.infrastructure.web.security.MercadoPagoWebhookSignatureValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billings/webhooks/mercadopago")
@RequiredArgsConstructor
@Tag(name = "Webhooks")
public class WebhookMercadoPagoController {

    private final MercadoPagoWebhookSignatureValidator signatureValidator;
    private final ProcessarWebhookInput processarWebhookInput;
    private final ObjectMapper objectMapper;

    @PostMapping
    @Operation(summary = "Recebe notificações de pagamento do Mercado Pago")
    public ResponseEntity<WebhookReceipt> receive(
            @RequestHeader(name = "x-signature", required = false) String signature,
            @RequestHeader(name = "x-request-id", required = false) String requestId,
            @RequestBody MercadoPagoWebhookNotification notification
    ) {
        if (!signatureValidator.isValid(notification, signature, requestId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Assinatura do webhook inválida.");
        }
        if (notification.paymentId() == null || notification.paymentId().isBlank()
                || !"payment".equals(notification.type())
                || notification.action() == null
                || !notification.action().startsWith("payment.")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notificação de pagamento inválida.");
        }

        LocalDateTime receivedAt = LocalDateTime.now();
        processarWebhookInput.execute(new ProcessarWebhookCommand(
                notification.paymentId(),
                notification.action(),
                receivedAt,
                objectMapper.convertValue(notification, new TypeReference<Map<String, Object>>() {})
        ));
        return ResponseEntity.ok(new WebhookReceipt("received"));
    }

    public record WebhookReceipt(String status) {
    }
}
