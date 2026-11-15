package com.techchallenge.billing.application.gateway.ports.output.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - MercadoPagoPaymentResult")
class MercadoPagoPaymentResultTest {

    @Test
    @DisplayName("Deve criar registro com todos os campos")
    void deveCriarRegistroComTodosOsCampos() {
        LocalDateTime dateApproved = LocalDateTime.now();
        MercadoPagoPaymentResult result = new MercadoPagoPaymentResult(
                "payment-123",
                "approved",
                new BigDecimal("100.50"),
                "ref-456",
                dateApproved,
                null,
                null,
                "qr-code",
                "base64-code"
        );

        assertEquals("payment-123", result.paymentId());
        assertEquals("approved", result.status());
        assertEquals(new BigDecimal("100.50"), result.amount());
        assertEquals("ref-456", result.externalReference());
        assertEquals(dateApproved, result.dateApproved());
        assertNull(result.failureCode());
        assertNull(result.failureMessage());
        assertEquals("qr-code", result.qrCode());
        assertEquals("base64-code", result.qrCodeBase64());
    }

    @Test
    @DisplayName("Deve criar registro com campos nulos opcionais")
    void deveCriarRegistroComCamposNulosOpcionais() {
        MercadoPagoPaymentResult result = new MercadoPagoPaymentResult(
                "payment-123",
                "pending",
                new BigDecimal("50.00"),
                "ref-789",
                null,
                null,
                null,
                null,
                null
        );

        assertEquals("payment-123", result.paymentId());
        assertEquals("pending", result.status());
        assertEquals(new BigDecimal("50.00"), result.amount());
        assertEquals("ref-789", result.externalReference());
        assertNull(result.dateApproved());
        assertNull(result.failureCode());
        assertNull(result.failureMessage());
        assertNull(result.qrCode());
        assertNull(result.qrCodeBase64());
    }

    @Test
    @DisplayName("Deve criar registro com falha")
    void deveCriarRegistroComFalha() {
        MercadoPagoPaymentResult result = new MercadoPagoPaymentResult(
                "payment-999",
                "rejected",
                new BigDecimal("75.00"),
                "ref-000",
                null,
                "ERR-001",
                "Cartão rejeitado",
                null,
                null
        );

        assertEquals("payment-999", result.paymentId());
        assertEquals("rejected", result.status());
        assertEquals("ERR-001", result.failureCode());
        assertEquals("Cartão rejeitado", result.failureMessage());
    }

    @Test
    @DisplayName("Deve permitir valores iguais")
    void devePermitirValoresIguais() {
        LocalDateTime dateApproved = LocalDateTime.now();
        MercadoPagoPaymentResult result1 = new MercadoPagoPaymentResult(
                "payment-123",
                "approved",
                new BigDecimal("100.50"),
                "ref-456",
                dateApproved,
                null,
                null,
                "qr-code",
                "base64-code"
        );

        MercadoPagoPaymentResult result2 = new MercadoPagoPaymentResult(
                "payment-123",
                "approved",
                new BigDecimal("100.50"),
                "ref-456",
                dateApproved,
                null,
                null,
                "qr-code",
                "base64-code"
        );

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Deve ter métodos de acesso para todos os campos")
    void deveTerMetodosDeAcessoParaTodosOsCampos() {
        LocalDateTime dateApproved = LocalDateTime.now();
        MercadoPagoPaymentResult result = new MercadoPagoPaymentResult(
                "payment-123",
                "approved",
                new BigDecimal("100.50"),
                "ref-456",
                dateApproved,
                "ERR-001",
                "Error",
                "qr-code",
                "base64-code"
        );

        assertNotNull(result.paymentId());
        assertNotNull(result.status());
        assertNotNull(result.amount());
        assertNotNull(result.externalReference());
        assertNotNull(result.dateApproved());
        assertNotNull(result.failureCode());
        assertNotNull(result.failureMessage());
        assertNotNull(result.qrCode());
        assertNotNull(result.qrCodeBase64());
    }
}
