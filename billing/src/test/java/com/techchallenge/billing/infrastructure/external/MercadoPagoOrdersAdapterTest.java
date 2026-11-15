package com.techchallenge.billing.infrastructure.external;

import com.techchallenge.billing.application.gateway.ports.output.models.CreatePixPaymentCommand;
import com.techchallenge.billing.application.gateway.ports.output.models.MercadoPagoPaymentResult;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoOrdersAdapterTest {

    @Mock
    private MercadoPagoOrdersClient ordersClient;

    private MercadoPagoOrdersAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MercadoPagoOrdersAdapter(ordersClient);
    }

    @Test
    void createsPixPaymentSuccessfully() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Test payment",
                "test@example.com",
                "CPF",
                "12345678900",
                "OS-123",
                "http://localhost/webhook"
        );

        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("id", "ORD123");
        orderResponse.put("status", "created");
        orderResponse.put("total_amount", "100.00");
        orderResponse.put("external_reference", "OS-123");

        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("qr_code", "qr-code-data");
        paymentMethod.put("qr_code_base64", "base64-data");

        Map<String, Object> payment = new HashMap<>();
        payment.put("payment_method", paymentMethod);

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", java.util.List.of(payment));

        orderResponse.put("transactions", transactions);

        when(ordersClient.createPixOrder(
                eq("OS-123"),
                eq("Test payment"),
                eq(new BigDecimal("100.00")),
                eq("test@example.com"),
                eq("CPF"),
                eq("12345678900"),
                eq("http://localhost/webhook")
        )).thenReturn(orderResponse);

        MercadoPagoPaymentResult result = adapter.createPixPayment(command);

        assertNotNull(result);
        assertEquals("ORD123", result.paymentId());
        assertEquals("100.00", result.amount().toString());
        assertEquals("qr-code-data", result.qrCode());
        assertEquals("base64-data", result.qrCodeBase64());
        verify(ordersClient).createPixOrder(
                anyString(), anyString(), any(BigDecimal.class),
                anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void throwsExceptionWhenOrderResponseIsNull() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Test payment",
                "test@example.com",
                "CPF",
                "12345678900",
                "OS-123",
                "http://localhost/webhook"
        );

        when(ordersClient.createPixOrder(
                anyString(), anyString(), any(BigDecimal.class),
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(null);

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                adapter.createPixPayment(command)
        );
    }

    @Test
    void throwsExceptionWhenOrderResponseMissingId() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Test payment",
                "test@example.com",
                "CPF",
                "12345678900",
                "OS-123",
                "http://localhost/webhook"
        );

        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("status", "created");

        when(ordersClient.createPixOrder(
                anyString(), anyString(), any(BigDecimal.class),
                anyString(), anyString(), anyString(), anyString()
        )).thenReturn(orderResponse);

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                adapter.createPixPayment(command)
        );
    }

    @Test
    void getPaymentSuccessfully() {
        String paymentId = "ORD123";

        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("id", paymentId);
        orderResponse.put("status", "processed");
        orderResponse.put("external_reference", "OS-123");
        orderResponse.put("total_amount", "100.00");

        Map<String, Object> payment = new HashMap<>();
        payment.put("status", "approved");
        payment.put("status_detail", "accredited");
        payment.put("amount", "100.00");

        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("qr_code", "qr-code-data");
        paymentMethod.put("qr_code_base64", "base64-data");
        payment.put("payment_method", paymentMethod);

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", java.util.List.of(payment));
        orderResponse.put("transactions", transactions);

        when(ordersClient.getOrder(paymentId)).thenReturn(orderResponse);

        MercadoPagoPaymentResult result = adapter.getPayment(paymentId);

        assertNotNull(result);
        assertEquals(paymentId, result.paymentId());
        assertEquals("approved", result.status());
        assertEquals("OS-123", result.externalReference());
        verify(ordersClient).getOrder(paymentId);
    }

    @Test
    void throwsExceptionWhenGetPaymentWithNullPaymentId() {
        assertThrows(IntegracaoMercadoPagoException.class, () ->
                adapter.getPayment(null)
        );
    }

    @Test
    void throwsExceptionWhenGetPaymentWithBlankPaymentId() {
        assertThrows(IntegracaoMercadoPagoException.class, () ->
                adapter.getPayment("   ")
        );
    }

    @Test
    void throwsExceptionWhenOrderResponseMissingIdInGetPayment() {
        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("status", "processed");

        when(ordersClient.getOrder("ORD123")).thenReturn(orderResponse);

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                adapter.getPayment("ORD123")
        );
    }

    @Test
    void throwsExceptionWhenOrderStatusIsUnrecognized() {
        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("id", "ORD123");
        orderResponse.put("status", "unknown_status");

        when(ordersClient.getOrder("ORD123")).thenReturn(orderResponse);

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                adapter.getPayment("ORD123")
        );
    }

    @Test
    void mapsOrderStatusRejectedCorrectly() {
        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("id", "ORD123");
        orderResponse.put("status", "processed");

        Map<String, Object> payment = new HashMap<>();
        payment.put("status", "rejected");
        payment.put("status_detail", "rejected_by_bank");
        payment.put("amount", "100.00");

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", java.util.List.of(payment));
        orderResponse.put("transactions", transactions);

        when(ordersClient.getOrder("ORD123")).thenReturn(orderResponse);

        MercadoPagoPaymentResult result = adapter.getPayment("ORD123");

        assertEquals("rejected", result.status());
    }

    @Test
    void mapsOrderStatusCanceledCorrectly() {
        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("id", "ORD123");
        orderResponse.put("status", "canceled");

        Map<String, Object> payment = new HashMap<>();
        payment.put("status", "processed");
        payment.put("amount", "100.00");

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", java.util.List.of(payment));
        orderResponse.put("transactions", transactions);

        when(ordersClient.getOrder("ORD123")).thenReturn(orderResponse);

        MercadoPagoPaymentResult result = adapter.getPayment("ORD123");

        assertEquals("cancelled", result.status());
    }

    @Test
    void mapsOrderStatusExpiredCorrectly() {
        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("id", "ORD123");
        orderResponse.put("status", "expired");

        Map<String, Object> payment = new HashMap<>();
        payment.put("status", "processed");
        payment.put("amount", "100.00");

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", java.util.List.of(payment));
        orderResponse.put("transactions", transactions);

        when(ordersClient.getOrder("ORD123")).thenReturn(orderResponse);

        MercadoPagoPaymentResult result = adapter.getPayment("ORD123");

        assertEquals("expired", result.status());
    }

    @Test
    void mapsOrderStatusApprovedCorrectly() {
        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("id", "ORD123");
        orderResponse.put("status", "processed");

        Map<String, Object> payment = new HashMap<>();
        payment.put("status", "processed");
        payment.put("status_detail", "accredited");
        payment.put("amount", "100.00");

        Map<String, Object> transactions = new HashMap<>();
        transactions.put("payments", java.util.List.of(payment));
        orderResponse.put("transactions", transactions);

        when(ordersClient.getOrder("ORD123")).thenReturn(orderResponse);

        MercadoPagoPaymentResult result = adapter.getPayment("ORD123");

        assertEquals("approved", result.status());
    }
}
