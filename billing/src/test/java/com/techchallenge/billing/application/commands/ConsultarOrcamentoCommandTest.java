package com.techchallenge.billing.application.commands;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - ConsultarOrcamentoCommand")
class ConsultarOrcamentoCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar comando com orcamentoId válido")
    void deveCriarComandoComOrcamentoIdValido() {
        UUID orcamentoId = UUID.randomUUID();
        ConsultarOrcamentoCommand command = new ConsultarOrcamentoCommand(orcamentoId);

        assertEquals(orcamentoId, command.orcamentoId());

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar comando com orcamentoId nulo")
    void deveRejeitarComandoComOrcamentoIdNulo() {
        ConsultarOrcamentoCommand command = new ConsultarOrcamentoCommand(null);

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orcamentoId")));
    }

    @Test
    @DisplayName("Deve ter record imutável")
    void deveTerRecordImutavel() {
        UUID orcamentoId = UUID.randomUUID();

        ConsultarOrcamentoCommand command = new ConsultarOrcamentoCommand(orcamentoId);

        assertEquals(orcamentoId, command.orcamentoId());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para comandos iguais")
    void devePermitirEqualsEHashCodeParaComandosIguais() {
        UUID orcamentoId = UUID.randomUUID();

        ConsultarOrcamentoCommand command1 = new ConsultarOrcamentoCommand(orcamentoId);
        ConsultarOrcamentoCommand command2 = new ConsultarOrcamentoCommand(orcamentoId);

        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }
}
