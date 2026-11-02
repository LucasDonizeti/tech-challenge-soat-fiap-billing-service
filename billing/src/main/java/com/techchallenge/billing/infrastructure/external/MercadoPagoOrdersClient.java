package com.techchallenge.billing.infrastructure.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class MercadoPagoOrdersClient {

    private static final String MERCADO_PAGO_API_BASE_URL = "https://api.mercadopago.com";
    private static final String ORDERS_API_ENDPOINT = MERCADO_PAGO_API_BASE_URL + "/v1/orders";

    private final RestTemplate restTemplate;
    private final String accessToken;
    private final int pixExpiresInMinutes;
    private final ObjectMapper objectMapper;

    public MercadoPagoOrdersClient(
            RestTemplate restTemplate,
            @Value("${mercadopago.access-token:}") String accessToken,
            @Value("${billing.pix.expires-in-minutes:30}") int pixExpiresInMinutes,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.accessToken = accessToken;
        this.pixExpiresInMinutes = pixExpiresInMinutes;
        this.objectMapper = objectMapper;
    }

    /**
     * Cria uma Order PIX no Mercado Pago via REST API (Orders API v1)
     * Usa REST API diretamente porque o SDK v3.7.0 não suporta transactions no OrderCreateRequest
     */
    public Map<String, Object> createPixOrder(
            String externalOrderId,
            String description,
            BigDecimal amount,
            String payerEmail,
            String payerIdentificationType,
            String payerIdentificationNumber,
            String notificationUrl
    ) {
        requireAccessToken();
        if (externalOrderId == null || externalOrderId.isBlank()) {
            throw new IntegracaoMercadoPagoException("O ID externo da order é obrigatório.");
        }

        String idempotencyKey = UUID.randomUUID().toString();

        log.info(
                "Criando Order PIX via REST API: externalOrderId={}, amount={}, description={}, idempotencyKey={}",
                externalOrderId,
                amount,
                sanitizeLogValue(description),
                idempotencyKey
        );

        try {
            // Construir payload JSON conforme Orders API v1 spec
            Map<String, Object> orderPayload = buildOrderPayload(
                    externalOrderId,
                    description,
                    amount,
                    payerEmail,
                    payerIdentificationType,
                    payerIdentificationNumber,
                    notificationUrl
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("X-Idempotency-Key", idempotencyKey);

            HttpEntity<String> request = new HttpEntity<>(
                    objectMapper.writeValueAsString(orderPayload),
                    headers
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    ORDERS_API_ENDPOINT,
                    request,
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new IntegracaoMercadoPagoException(
                        "Falha ao criar order PIX: HTTP " + response.getStatusCode()
                );
            }

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new IntegracaoMercadoPagoException("Resposta vazia do Mercado Pago");
            }

            String orderId = (String) responseBody.get("id");
            log.info("Order criada com sucesso via REST API: orderId={}", orderId);

            return responseBody;

        } catch (IntegracaoMercadoPagoException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IntegracaoMercadoPagoException(
                    "Falha ao criar order PIX: " + exception.getMessage(),
                    exception
            );
        }
    }

    public Map<String, Object> getOrder(String orderId) {
        requireAccessToken();
        if (orderId == null || orderId.isBlank()) {
            throw new IntegracaoMercadoPagoException("O ID da order é obrigatório.");
        }

        String url = UriComponentsBuilder.fromUriString(ORDERS_API_ENDPOINT)
                .pathSegment(orderId)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            Map<String, Object> order = response.getBody();
            if (order == null) {
                throw new IntegracaoMercadoPagoException(
                        "Resposta vazia ao consultar order no Mercado Pago: " + orderId);
            }
            return order;
        } catch (RestClientResponseException exception) {
            throw new IntegracaoMercadoPagoException(
                    "Falha ao consultar order no Mercado Pago: HTTP "
                            + exception.getStatusCode().value() + " - "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        } catch (RestClientException exception) {
            throw new IntegracaoMercadoPagoException(
                    "Falha de comunicação ao consultar order no Mercado Pago: " + exception.getMessage(),
                    exception
            );
        }
    }

    /**
     * Constrói o payload JSON para criar order conforme Orders API v1 specification
     * Baseado na documentação oficial e teste com Postman
     */
    private Map<String, Object> buildOrderPayload(
            String externalOrderId,
            String description,
            BigDecimal amount,
            String payerEmail,
            String payerIdentificationType,
            String payerIdentificationNumber,
            String notificationUrl
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "online");
        payload.put("external_reference", externalOrderId);
        payload.put("processing_mode", "automatic");
        payload.put("total_amount", amount.toPlainString());
        payload.put("description", description);

        // Payer info VA NO NÍVEL DA ORDER, não dentro do transaction
        Map<String, Object> payer = new LinkedHashMap<>();
        payer.put("email", payerEmail);
        payer.put("first_name", "Cliente");  // Mercado Pago requer first_name
        // Não incluir identification aqui - pode causar problemas
        payload.put("payer", payer);

        // Construir transactions - APENAS com amount e payment_method
        Map<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("amount", amount.toPlainString());

        // PaymentMethod para PIX - APENAS id e type
        Map<String, Object> paymentMethod = new LinkedHashMap<>();
        paymentMethod.put("id", "pix");
        paymentMethod.put("type", "bank_transfer");
        transaction.put("payment_method", paymentMethod);

        // NÃO adicionar: payer, date_of_expiration, notification_url, identification aqui
        // Esses campos não são suportados dentro de transactions.payments[0]

        // Wrapping transactions in the order
        Map<String, Object> transactions = new LinkedHashMap<>();
        transactions.put("payments", new Object[]{transaction});
        payload.put("transactions", transactions);

        return payload;
    }

    private String sanitizeLogValue(String value) {
        return value == null ? null : value.replace('\n', ' ').replace('\r', ' ');
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private String maskDocument(String document) {
        if (document == null || document.isBlank()) {
            return document;
        }
        int visibleCharacters = Math.min(4, document.length());
        return "*".repeat(document.length() - visibleCharacters)
                + document.substring(document.length() - visibleCharacters);
    }

    private void requireAccessToken() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IntegracaoMercadoPagoException(
                    "Configure MERCADO_PAGO_ACCESS_TOKEN antes de solicitar pagamentos.");
        }
    }
}
