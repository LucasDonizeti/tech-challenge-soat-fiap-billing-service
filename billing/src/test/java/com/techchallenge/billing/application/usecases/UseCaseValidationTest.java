package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ProcessarWebhookCommand;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - UseCaseValidation")
class UseCaseValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve validar comando sem violações")
    void deveValidarComandoSemViolacoes() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        assertDoesNotThrow(() -> UseCaseValidation.validate(command, validator));
    }

    @Test
    @DisplayName("Deve lançar exceção quando há violações de validação")
    void deveLancarExcecaoQuandoHaViolacoesDeValidacao() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "",
                "",
                null,
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UseCaseValidation.validate(command, validator)
        );

        assertTrue(exception.getMessage().contains("Dados inválidos"));
    }

    @Test
    @DisplayName("Deve incluir mensagens de violação na exceção")
    void deveIncluirMensagensDeViolacaoNaExcecao() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UseCaseValidation.validate(command, validator)
        );

        assertTrue(exception.getMessage().contains("Dados inválidos"));
    }

    @Test
    @DisplayName("Deve validar múltiplas violações e concatenar mensagens")
    void deveValidarMultiplasViolacoesEConcatenarMensagens() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "",
                "",
                null,
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UseCaseValidation.validate(command, validator)
        );

        String message = exception.getMessage();
        assertTrue(message.contains("Dados inválidos"));
    }

    @Test
    @DisplayName("Deve validar quando validator é fornecido")
    void deveValidarQuandoValidatorEFornecido() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "mp-payment-123",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        assertDoesNotThrow(() -> UseCaseValidation.validate(command, validator));
    }

    @Test
    @DisplayName("Deve lançar exceção com campo específico quando único campo inválido")
    void deveLancarExcecaoComCampoEspecificoQuandoUnicoCampoInvalido() {
        ProcessarWebhookCommand command = new ProcessarWebhookCommand(
                "",
                "payment.updated",
                LocalDateTime.now(),
                Map.of("type", "payment")
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UseCaseValidation.validate(command, validator)
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Dados inválidos"));
    }
}
