package com.techchallenge.billing.application.commands;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - ConfirmarPagamentoCommand")
class ConfirmarPagamentoCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar comando com mercadoPagoPaymentId válido")
    void deveCriarComandoComMercadoPagoPaymentIdValido() {
        ConfirmarPagamentoCommand command = new ConfirmarPagamentoCommand("mp-payment-123");

        assertEquals("mp-payment-123", command.mercadoPagoPaymentId());

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar comando com mercadoPagoPaymentId nulo")
    void deveRejeitarComandoComMercadoPagoPaymentIdNulo() {
        ConfirmarPagamentoCommand command = new ConfirmarPagamentoCommand(null);

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mercadoPagoPaymentId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com mercadoPagoPaymentId vazio")
    void deveRejeitarComandoComMercadoPagoPaymentIdVazio() {
        ConfirmarPagamentoCommand command = new ConfirmarPagamentoCommand("");

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mercadoPagoPaymentId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com mercadoPagoPaymentId apenas espaços")
    void deveRejeitarComandoComMercadoPagoPaymentIdApenasEspacos() {
        ConfirmarPagamentoCommand command = new ConfirmarPagamentoCommand("   ");

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("mercadoPagoPaymentId")));
    }

    @Test
    @DisplayName("Deve ter record imutável")
    void deveTerRecordImutavel() {
        String paymentId = "mp-payment-123";

        ConfirmarPagamentoCommand command = new ConfirmarPagamentoCommand(paymentId);

        assertEquals(paymentId, command.mercadoPagoPaymentId());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para comandos iguais")
    void devePermitirEqualsEHashCodeParaComandosIguais() {
        ConfirmarPagamentoCommand command1 = new ConfirmarPagamentoCommand("mp-payment-123");
        ConfirmarPagamentoCommand command2 = new ConfirmarPagamentoCommand("mp-payment-123");

        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }
}
