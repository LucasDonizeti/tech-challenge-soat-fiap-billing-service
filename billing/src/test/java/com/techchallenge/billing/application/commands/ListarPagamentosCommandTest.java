package com.techchallenge.billing.application.commands;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - ListarPagamentosCommand")
class ListarPagamentosCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar comando com ordemServicoId válido")
    void deveCriarComandoComOrdemServicoIdValido() {
        UUID ordemServicoId = UUID.randomUUID();
        ListarPagamentosCommand command = new ListarPagamentosCommand(ordemServicoId);

        assertEquals(ordemServicoId, command.ordemServicoId());

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar comando com ordemServicoId nulo")
    void deveRejeitarComandoComOrdemServicoIdNulo() {
        ListarPagamentosCommand command = new ListarPagamentosCommand(null);

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("ordemServicoId")));
    }

    @Test
    @DisplayName("Deve ter record imutável")
    void deveTerRecordImutavel() {
        UUID ordemServicoId = UUID.randomUUID();

        ListarPagamentosCommand command = new ListarPagamentosCommand(ordemServicoId);

        assertEquals(ordemServicoId, command.ordemServicoId());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para comandos iguais")
    void devePermitirEqualsEHashCodeParaComandosIguais() {
        UUID ordemServicoId = UUID.randomUUID();

        ListarPagamentosCommand command1 = new ListarPagamentosCommand(ordemServicoId);
        ListarPagamentosCommand command2 = new ListarPagamentosCommand(ordemServicoId);

        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }
}
