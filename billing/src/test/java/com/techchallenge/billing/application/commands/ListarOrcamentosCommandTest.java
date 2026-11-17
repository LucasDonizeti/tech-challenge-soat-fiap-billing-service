package com.techchallenge.billing.application.commands;

import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - ListarOrcamentosCommand")
class ListarOrcamentosCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar comando com todos os campos válidos")
    void deveCriarComandoComTodosOsCamposValidos() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.PAGO;
        Pageable pageable = PageRequest.of(0, 10);

        ListarOrcamentosCommand command = new ListarOrcamentosCommand(
                clienteId,
                status,
                pageable
        );

        assertEquals(clienteId, command.clienteId());
        assertEquals(status, command.status());
        assertEquals(pageable, command.pageable());

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve criar comando com status nulo")
    void deveCriarComandoComStatusNulo() {
        UUID clienteId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        ListarOrcamentosCommand command = new ListarOrcamentosCommand(
                clienteId,
                null,
                pageable
        );

        assertEquals(clienteId, command.clienteId());
        assertNull(command.status());
        assertEquals(pageable, command.pageable());

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar comando com clienteId nulo")
    void deveRejeitarComandoComClienteIdNulo() {
        Pageable pageable = PageRequest.of(0, 10);

        ListarOrcamentosCommand command = new ListarOrcamentosCommand(
                null,
                StatusOrcamento.PAGO,
                pageable
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clienteId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com pageable nulo")
    void deveRejeitarComandoComPageableNulo() {
        ListarOrcamentosCommand command = new ListarOrcamentosCommand(
                UUID.randomUUID(),
                StatusOrcamento.PAGO,
                null
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("pageable")));
    }

    @Test
    @DisplayName("Deve criar comando com diferentes status")
    void deveCriarComandoComDiferentesStatus() {
        UUID clienteId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        ListarOrcamentosCommand command1 = new ListarOrcamentosCommand(
                clienteId,
                StatusOrcamento.GERADO,
                pageable
        );

        ListarOrcamentosCommand command2 = new ListarOrcamentosCommand(
                clienteId,
                StatusOrcamento.AGUARDANDO_PAGAMENTO,
                pageable
        );

        ListarOrcamentosCommand command3 = new ListarOrcamentosCommand(
                clienteId,
                StatusOrcamento.PAGO,
                pageable
        );

        ListarOrcamentosCommand command4 = new ListarOrcamentosCommand(
                clienteId,
                StatusOrcamento.RECUSADO,
                pageable
        );

        ListarOrcamentosCommand command5 = new ListarOrcamentosCommand(
                clienteId,
                StatusOrcamento.CANCELADO,
                pageable
        );

        assertEquals(StatusOrcamento.GERADO, command1.status());
        assertEquals(StatusOrcamento.AGUARDANDO_PAGAMENTO, command2.status());
        assertEquals(StatusOrcamento.PAGO, command3.status());
        assertEquals(StatusOrcamento.RECUSADO, command4.status());
        assertEquals(StatusOrcamento.CANCELADO, command5.status());
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.PAGO;
        Pageable pageable = PageRequest.of(0, 10);

        ListarOrcamentosCommand command = new ListarOrcamentosCommand(
                clienteId,
                status,
                pageable
        );

        assertEquals(clienteId, command.clienteId());
        assertEquals(status, command.status());
        assertEquals(pageable, command.pageable());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para comandos iguais")
    void devePermitirEqualsEHashCodeParaComandosIguais() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.PAGO;
        Pageable pageable = PageRequest.of(0, 10);

        ListarOrcamentosCommand command1 = new ListarOrcamentosCommand(
                clienteId,
                status,
                pageable
        );

        ListarOrcamentosCommand command2 = new ListarOrcamentosCommand(
                clienteId,
                status,
                pageable
        );

        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }

    @Test
    @DisplayName("Deve diferenciar comandos com clienteId diferente")
    void deveDiferenciarComandosComClienteIdDiferente() {
        StatusOrcamento status = StatusOrcamento.PAGO;
        Pageable pageable = PageRequest.of(0, 10);

        ListarOrcamentosCommand command1 = new ListarOrcamentosCommand(
                UUID.randomUUID(),
                status,
                pageable
        );

        ListarOrcamentosCommand command2 = new ListarOrcamentosCommand(
                UUID.randomUUID(),
                status,
                pageable
        );

        assertNotEquals(command1, command2);
    }

    @Test
    @DisplayName("Deve diferenciar comandos com status diferente")
    void deveDiferenciarComandosComStatusDiferente() {
        UUID clienteId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        ListarOrcamentosCommand command1 = new ListarOrcamentosCommand(
                clienteId,
                StatusOrcamento.PAGO,
                pageable
        );

        ListarOrcamentosCommand command2 = new ListarOrcamentosCommand(
                clienteId,
                StatusOrcamento.GERADO,
                pageable
        );

        assertNotEquals(command1, command2);
    }

    @Test
    @DisplayName("Deve diferenciar comandos com pageable diferente")
    void deveDiferenciarComandosComPageableDiferente() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.PAGO;

        ListarOrcamentosCommand command1 = new ListarOrcamentosCommand(
                clienteId,
                status,
                PageRequest.of(0, 10)
        );

        ListarOrcamentosCommand command2 = new ListarOrcamentosCommand(
                clienteId,
                status,
                PageRequest.of(1, 20)
        );

        assertNotEquals(command1, command2);
    }

    @Test
    @DisplayName("Deve aceitar pageable com diferentes configurações")
    void deveAceitarPageableComDiferentesConfiguracoes() {
        UUID clienteId = UUID.randomUUID();

        ListarOrcamentosCommand command1 = new ListarOrcamentosCommand(
                clienteId,
                null,
                PageRequest.of(0, 10)
        );

        ListarOrcamentosCommand command2 = new ListarOrcamentosCommand(
                clienteId,
                null,
                PageRequest.of(1, 20)
        );

        ListarOrcamentosCommand command3 = new ListarOrcamentosCommand(
                clienteId,
                null,
                PageRequest.of(2, 50)
        );

        assertEquals(0, command1.pageable().getPageNumber());
        assertEquals(10, command1.pageable().getPageSize());
        assertEquals(1, command2.pageable().getPageNumber());
        assertEquals(20, command2.pageable().getPageSize());
        assertEquals(2, command3.pageable().getPageNumber());
        assertEquals(50, command3.pageable().getPageSize());
    }
}
