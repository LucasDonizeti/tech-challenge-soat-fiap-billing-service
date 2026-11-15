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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billings/webhooks/mercadopago")
@RequiredArgsConstructor
@Tag(name = "Webhooks")
public class WebhookMercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookMercadoPagoController.class);

    private final MercadoPagoWebhookSignatureValidator signatureValidator;
    private final ProcessarWebhookInput processarWebhookInput;
    private final ObjectMapper objectMapper;

    @PostMapping
    @Operation(summary = "Recebe notificações de orders do Mercado Pago via Orders API")
    public ResponseEntity<WebhookReceipt> receive(
            @RequestHeader(name = "x-signature", required = false) String signature,
            @RequestHeader(name = "x-request-id", required = false) String requestId,
            @RequestParam(name = "data.id", required = false) String signedDataId,
            @RequestBody String rawBody
    ) {
        logger.info("=== WEBHOOK MERCADO PAGO RECEBIDO (Orders API) ===");
        logger.info("Headers - x-signature: {}, x-request-id: {}", signature, requestId);
        logger.info("Query param - data.id: {}", signedDataId);
        logger.info("Raw body: {}", rawBody);
        
        // Deserializar o JSON
        MercadoPagoWebhookNotification notification;
        try {
            notification = objectMapper.readValue(rawBody, MercadoPagoWebhookNotification.class);
        } catch (Exception e) {
            logger.error("Erro ao deserializar webhook", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON inválido");
        }
        
        logger.info("Webhook recebido - action: {}, type: {}", 
                notification.action(), notification.type());
        
        // A Orders API can notify both general updates and final order transitions.
        if (!isValidOrderAction(notification.action())) {
            logger.warn("Action inválido para webhook de order: {}", notification.action());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "action inválido. Valores aceitos: order.updated, order.processed, order.canceled");
        }
        
        // Validar que type é 'order' (e não 'payment')
        if (!"order".equals(notification.type())) {
            logger.warn("Payload do webhook inválido: type não é 'order', recebido: {}", notification.type());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type inválido");
        }
        
        // Na Orders API, o ID vem em notification.orderId() ou notification.data().id()
        String orderId = notification.orderId();
        if (orderId == null || orderId.isBlank()) {
            logger.warn("Payload do webhook inválido: orderId ausente.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order_id inválido");
        }
        if (signedDataId != null && !signedDataId.equals(orderId)) {
            logger.warn("ID da order diverge do data.id assinado: body={}, query={}", orderId, signedDataId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order_id divergente");
        }
        
        if (signature == null || signature.isBlank()
                || requestId == null || requestId.isBlank()
                || signedDataId == null || signedDataId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Dados de assinatura do webhook ausentes");
        }

        try {
            logger.info("Validando assinatura do webhook...");
            boolean isValid = signatureValidator.isValid(
                notification,
                signedDataId,
                signature,
                requestId
            );

            if (!isValid) {
                logger.error("Assinatura do webhook inválida");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Webhook signature inválida");
            }

            logger.info("Assinatura do webhook validada com sucesso");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao validar assinatura do webhook: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Erro na validação de assinatura");
        }

        LocalDateTime receivedAt = LocalDateTime.now();

        try {
            logger.info("Processando webhook de Order - action: {}, orderId: {}", 
                    notification.action(), orderId);
            
            logger.info("Consultando status atualizado da order no Mercado Pago: orderId={}", orderId);
            processarWebhookInput.execute(new ProcessarWebhookCommand(
                    orderId,
                    notification.action(),
                    receivedAt,
                    objectMapper.convertValue(notification, new TypeReference<Map<String, Object>>() {})
            ));
            
            logger.info("✅ Webhook processado com sucesso");
        } catch (Exception e) {
            logger.error("❌ Erro ao processar webhook de order", e);
            throw e;
        }

        return ResponseEntity.ok(new WebhookReceipt("received"));
    }
    
    /**
     * Restrict processing to order notifications; the current order state is fetched from Mercado Pago.
     */
    private boolean isValidOrderAction(String action) {
        return "order.updated".equals(action)
                || "order.processed".equals(action)
                || "order.canceled".equals(action);
    }

    public record WebhookReceipt(String status) {
        public WebhookReceipt {
            // Garante que sempre retorna um JSON bem-formado
            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException("Status não pode ser vazio");
            }
        }
    }
}
