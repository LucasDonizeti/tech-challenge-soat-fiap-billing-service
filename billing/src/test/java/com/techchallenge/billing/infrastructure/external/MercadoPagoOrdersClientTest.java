package com.techchallenge.billing.infrastructure.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoOrdersClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private MercadoPagoOrdersClient client;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        client = new MercadoPagoOrdersClient(
                restTemplate,
                "test-access-token",
                30,
                objectMapper
        );
    }

    @Test
    void createsPixOrderSuccessfully() {
        String externalOrderId = UUID.randomUUID().toString();
        String description = "Test payment";
        BigDecimal amount = new BigDecimal("100.00");
        String payerEmail = "test@example.com";
        String payerIdentificationType = "CPF";
        String payerIdentificationNumber = "12345678900";
        String notificationUrl = "http://localhost/webhook";

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "ORD123");
        responseBody.put("status", "created");

        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.CREATED);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Map<String, Object> result = client.createPixOrder(
                externalOrderId,
                description,
                amount,
                payerEmail,
                payerIdentificationType,
                payerIdentificationNumber,
                notificationUrl
        );

        assertNotNull(result);
        assertEquals("ORD123", result.get("id"));
        verify(restTemplate).postForEntity(
                eq("https://api.mercadopago.com/v1/orders"),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    void throwsExceptionWhenAccessTokenNotConfigured() {
        MercadoPagoOrdersClient clientWithoutToken = new MercadoPagoOrdersClient(
                restTemplate,
                "",
                30,
                objectMapper
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                clientWithoutToken.createPixOrder(
                        UUID.randomUUID().toString(),
                        "Test",
                        new BigDecimal("100.00"),
                        "test@example.com",
                        "CPF",
                        "12345678900",
                        "http://localhost/webhook"
                )
        );
    }

    @Test
    void throwsExceptionWhenExternalOrderIdIsNull() {
        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.createPixOrder(
                        null,
                        "Test",
                        new BigDecimal("100.00"),
                        "test@example.com",
                        "CPF",
                        "12345678900",
                        "http://localhost/webhook"
                )
        );
    }

    @Test
    void throwsExceptionWhenExternalOrderIdIsBlank() {
        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.createPixOrder(
                        "   ",
                        "Test",
                        new BigDecimal("100.00"),
                        "test@example.com",
                        "CPF",
                        "12345678900",
                        "http://localhost/webhook"
                )
        );
    }

    @Test
    void throwsExceptionWhenResponseIsNotCreated() {
        String externalOrderId = UUID.randomUUID().toString();
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.createPixOrder(
                        externalOrderId,
                        "Test",
                        new BigDecimal("100.00"),
                        "test@example.com",
                        "CPF",
                        "12345678900",
                        "http://localhost/webhook"
                )
        );
    }

    @Test
    void throwsExceptionWhenResponseBodyIsNull() {
        ResponseEntity<Map> response = ResponseEntity.status(HttpStatus.CREATED).body(null);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.createPixOrder(
                        UUID.randomUUID().toString(),
                        "Test",
                        new BigDecimal("100.00"),
                        "test@example.com",
                        "CPF",
                        "12345678900",
                        "http://localhost/webhook"
                )
        );
    }

    @Test
    void throwsExceptionOnRestClientException() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection failed"));

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.createPixOrder(
                        UUID.randomUUID().toString(),
                        "Test",
                        new BigDecimal("100.00"),
                        "test@example.com",
                        "CPF",
                        "12345678900",
                        "http://localhost/webhook"
                )
        );
    }

    @Test
    void getOrderSuccessfully() {
        String orderId = "ORD123";
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", orderId);
        responseBody.put("status", "created");

        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Map<String, Object> result = client.getOrder(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.get("id"));
    }

    @Test
    void throwsExceptionWhenGetOrderAndAccessTokenNotConfigured() {
        MercadoPagoOrdersClient clientWithoutToken = new MercadoPagoOrdersClient(
                restTemplate,
                "",
                30,
                objectMapper
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                clientWithoutToken.getOrder("ORD123")
        );
    }

    @Test
    void throwsExceptionWhenGetOrderAndOrderIdIsNull() {
        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrder(null)
        );
    }

    @Test
    void throwsExceptionWhenGetOrderAndOrderIdIsBlank() {
        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrder("   ")
        );
    }

    @Test
    void throwsExceptionWhenGetOrderResponseIsNull() {
        ResponseEntity<Map> response = ResponseEntity.status(HttpStatus.OK).body(null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrder("ORD123")
        );
    }

    @Test
    void throwsExceptionWhenGetOrderReturns404() {
        RestClientResponseException exception = new RestClientResponseException(
                "Not Found",
                404,
                "Not Found",
                null,
                null,
                null
        );

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(exception);

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrder("ORD123")
        );
    }

    @Test
    void throwsExceptionWhenGetOrderFailsWithRestClientException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection failed"));

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrder("ORD123")
        );
    }

}
