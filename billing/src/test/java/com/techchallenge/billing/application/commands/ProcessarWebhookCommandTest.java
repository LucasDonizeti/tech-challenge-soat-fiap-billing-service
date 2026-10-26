package com.techchallenge.billing.application.commands;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - ProcessarWebhookCommand")
class ProcessarWebhookCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar comando com todos os campos válidos")
    void deveCriarComandoComTodosOsCamposValidos() {
        LocalDateTime dataRecebimento = LocalDateTime.now();
        Map<String, Object> payload = Map.of("type", "payment", "data_id", "mp-123");

        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                dataRecebimento,
                payload
        );

        assertEquals("mp-payment-123", command.mercadoPagoPaymentId());
        assertEquals("payment.updated", command.action());
        assertEquals(dataRecebimento, command.dataRecebimento());
        assertEquals(payload, command.payloadOriginal());

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar comando com mercadoPagoPaymentId nulo")
    void deveRejeitarComandoComMercadoPagoPaymentIdNulo() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                null,
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mercadoPagoPaymentId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com mercadoPagoPaymentId vazio")
    void deveRejeitarComandoComMercadoPagoPaymentIdVazio() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mercadoPagoPaymentId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com mercadoPagoPaymentId apenas espaços")
    void deveRejeitarComandoComMercadoPagoPaymentIdApenasEspacos() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "   ",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mercadoPagoPaymentId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com action nulo")
    void deveRejeitarComandoComActionNulo() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "mp-payment-123",
                null,
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("action")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com action vazio")
    void deveRejeitarComandoComActionVazio() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "mp-payment-123",
                "",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("action")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com dataRecebimento nulo")
    void deveRejeitarComandoComDataRecebimentoNulo() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                null,
                Map.of("type", "payment")
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dataRecebimento")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payloadOriginal nulo")
    void deveRejeitarComandoComPayloadOriginalNulo() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                null
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payloadOriginal")));
    }

    @Test
    @DisplayName("Deve permitir payloadOriginal vazio")
    void devePermitirPayloadOriginalVazio() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                Map.of()
        );

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        String paymentId = "mp-payment-123";
        String action = "payment.updated";
        LocalDateTime data = LocalDateTime.now();
        Map<String, Object> payload = Map.of("key", "value");

        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                paymentId,
                action,
                data,
                payload
        );

        assertEquals(paymentId, command.mercadoPagoPaymentId());
        assertEquals(action, command.action());
        assertEquals(data, command.dataRecebimento());
        assertEquals(payload, command.payloadOriginal());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para comandos iguais")
    void devePermitirEqualsEHashCodeParaComandosIguais() {
        LocalDateTime data = LocalDateTime.now();
        Map<String, Object> payload = Map.of("key", "value");

        ProcessarWebhookCommand command1 = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                data,
                payload
        );

        ProcessarWebhookCommand command2 = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                data,
                payload
        );

        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }
}
