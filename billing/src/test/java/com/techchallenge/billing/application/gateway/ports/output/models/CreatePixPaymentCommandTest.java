package com.techchallenge.billing.application.gateway.ports.output.models;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - CreatePixPaymentCommand")
class CreatePixPaymentCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar comando com todos os campos válidos")
    void deveCriarComandoComTodosOsCamposValidos() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.50"),
                "Pagamento de serviço",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        assertEquals(new BigDecimal("100.50"), command.amount());
        assertEquals("Pagamento de serviço", command.description());
        assertEquals("cliente@email.com", command.payerEmail());
        assertEquals("CPF", command.payerIdentificationType());
        assertEquals("12345678909", command.payerIdentificationNumber());
        assertEquals("ref-123", command.externalReference());
        assertEquals("https://example.com/webhook", command.notificationUrl());

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar comando com amount nulo")
    void deveRejeitarComandoComAmountNulo() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                null,
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com amount negativo")
    void deveRejeitarComandoComAmountNegativo() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("-50.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com amount zero")
    void deveRejeitarComandoComAmountZero() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                BigDecimal.ZERO,
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com description nulo")
    void deveRejeitarComandoComDescriptionNulo() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                null,
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com description vazio")
    void deveRejeitarComandoComDescriptionVazio() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerEmail inválido")
    void deveRejeitarComandoComPayerEmailInvalido() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "email-invalido",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerEmail")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerEmail nulo")
    void deveRejeitarComandoComPayerEmailNulo() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                null,
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerEmail")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerIdentificationType nulo")
    void deveRejeitarComandoComPayerIdentificationTypeNulo() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                null,
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationType")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerIdentificationType vazio")
    void deveRejeitarComandoComPayerIdentificationTypeVazio() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationType")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerIdentificationNumber nulo")
    void deveRejeitarComandoComPayerIdentificationNumberNulo() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                null,
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationNumber")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerIdentificationNumber vazio")
    void deveRejeitarComandoComPayerIdentificationNumberVazio() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                "",
                "ref-123",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationNumber")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com externalReference nulo")
    void deveRejeitarComandoComExternalReferenceNulo() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                null,
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("externalReference")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com externalReference vazio")
    void deveRejeitarComandoComExternalReferenceVazio() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "",
                "https://example.com/webhook"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("externalReference")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com notificationUrl nulo")
    void deveRejeitarComandoComNotificationUrlNulo() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                null
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("notificationUrl")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com notificationUrl vazio")
    void deveRejeitarComandoComNotificationUrlVazio() {
        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                ""
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("notificationUrl")));
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Descrição";
        String email = "cliente@email.com";
        String idType = "CPF";
        String idNumber = "12345678909";
        String ref = "ref-123";
        String url = "https://example.com/webhook";

        CreatePixPaymentCommand command = new CreatePixPaymentCommand(
                amount,
                description,
                email,
                idType,
                idNumber,
                ref,
                url
        );

        assertEquals(amount, command.amount());
        assertEquals(description, command.description());
        assertEquals(email, command.payerEmail());
        assertEquals(idType, command.payerIdentificationType());
        assertEquals(idNumber, command.payerIdentificationNumber());
        assertEquals(ref, command.externalReference());
        assertEquals(url, command.notificationUrl());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para comandos iguais")
    void devePermitirEqualsEHashCodeParaComandosIguais() {
        CreatePixPaymentCommand command1 = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        CreatePixPaymentCommand command2 = new CreatePixPaymentCommand(
                new BigDecimal("100.00"),
                "Descrição",
                "cliente@email.com",
                "CPF",
                "12345678909",
                "ref-123",
                "https://example.com/webhook"
        );

        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }
}
