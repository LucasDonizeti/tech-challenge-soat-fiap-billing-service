package com.techchallenge.billing.infrastructure.external;

import com.mercadopago.client.MercadoPagoClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentPointOfInteraction;
import com.mercadopago.resources.payment.PaymentTransactionData;
import com.techchallenge.billing.application.gateway.ports.output.models.CreatePixPaymentCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoIntegrationAdapterTest {
    /*

    @Mock
    private MercadoPagoClient mercadoPagoClient;

    @Mock
    private Payment payment;

    @Mock
    private PaymentPointOfInteraction pointOfInteraction;

    @Mock
    private PaymentTransactionData transactionData;

    @Test
    void buildsSdkRequestAndMapsNestedQrCodeResponse() {
        var adapter = new MercadoPagoIntegrationAdapter(mercadoPagoClient, 30);
        var command = new CreatePixPaymentCommand(
                new BigDecimal("10.25"),
                "Serviço",
                "cliente@example.com",
                "CPF",
                "12345678901",
                "os-123",
                "https://example.com/webhook"
        );
        when(mercadoPagoClient.createPixPayment(any())).thenReturn(payment);
        when(payment.getId()).thenReturn(1234L);
        when(payment.getStatus()).thenReturn("pending");
        when(payment.getTransactionAmount()).thenReturn(new BigDecimal("10.25"));
        when(payment.getExternalReference()).thenReturn("os-123");
        when(payment.getDateApproved()).thenReturn(OffsetDateTime.parse("2026-10-18T12:00:00-03:00"));
        when(payment.getPointOfInteraction()).thenReturn(pointOfInteraction);
        when(pointOfInteraction.getTransactionData()).thenReturn(transactionData);
        when(transactionData.getQrCode()).thenReturn("pix-code");
        when(transactionData.getQrCodeBase64()).thenReturn("pix-image");

        var result = adapter.createPixPayment(command);

        var request = ArgumentCaptor.forClass(PaymentCreateRequest.class);
        verify(mercadoPagoClient).createPixPayment(request.capture());
        assertEquals(new BigDecimal("10.25"), request.getValue().getTransactionAmount());
        assertEquals("pix", request.getValue().getPaymentMethodId());
        assertEquals("cliente@example.com", request.getValue().getPayer().getEmail());
        assertEquals("CPF", request.getValue().getPayer().getIdentification().getType());
        assertEquals("12345678901", request.getValue().getPayer().getIdentification().getNumber());
        assertEquals("os-123", request.getValue().getExternalReference());
        assertEquals("https://example.com/webhook", request.getValue().getNotificationUrl());
        assertNotNull(request.getValue().getDateOfExpiration());
        assertEquals("1234", result.paymentId());
        assertEquals("pix-code", result.qrCode());
        assertEquals("pix-image", result.qrCodeBase64());
        assertEquals("os-123", result.externalReference());
        assertEquals(LocalDateTime.of(2026, 10, 18, 15, 0), result.dateApproved());
    }

     */
}
