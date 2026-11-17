package com.techchallenge.billing.application.commands;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - CriarOrcamentoCommand")
class CriarOrcamentoCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar comando com todos os campos válidos")
    void deveCriarComandoComTodosOsCamposValidos() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("150.00"),
                "Troca de óleo",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        assertNotNull(command.ordemServicoId());
        assertNotNull(command.clienteId());
        assertNotNull(command.veiculoId());
        assertEquals(new BigDecimal("150.00"), command.valorTotal());
        assertEquals("Troca de óleo", command.descricaoServicos());
        assertNotNull(command.dataOrcamento());
        assertEquals("cliente@email.com", command.payerEmail());
        assertEquals("CPF", command.payerIdentificationType());
        assertEquals("12345678909", command.payerIdentificationNumber());

        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve criar comando com dataOrcamento nulo")
    void deveCriarComandoComDataOrcamentoNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                null,
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        assertNull(command.dataOrcamento());
        var violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar comando com ordemServicoId nulo")
    void deveRejeitarComandoComOrdemServicoIdNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("ordemServicoId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com clienteId nulo")
    void deveRejeitarComandoComClienteIdNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clienteId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com veiculoId nulo")
    void deveRejeitarComandoComVeiculoIdNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("veiculoId")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com valorTotal nulo")
    void deveRejeitarComandoComValorTotalNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("valorTotal")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com valorTotal negativo")
    void deveRejeitarComandoComValorTotalNegativo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("-50.00"),
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("valorTotal")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com valorTotal zero")
    void deveRejeitarComandoComValorTotalZero() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ZERO,
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("valorTotal")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com descricaoServicos nulo")
    void deveRejeitarComandoComDescricaoServicosNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                null,
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricaoServicos")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com descricaoServicos vazio")
    void deveRejeitarComandoComDescricaoServicosVazio() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricaoServicos")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerEmail inválido")
    void deveRejeitarComandoComPayerEmailInvalido() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "email-invalido",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerEmail")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerEmail nulo")
    void deveRejeitarComandoComPayerEmailNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                null,
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerEmail")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerIdentificationType nulo")
    void deveRejeitarComandoComPayerIdentificationTypeNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                null,
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationType")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerIdentificationType vazio")
    void deveRejeitarComandoComPayerIdentificationTypeVazio() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "",
                "12345678909"
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationType")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerIdentificationNumber nulo")
    void deveRejeitarComandoComPayerIdentificationNumberNulo() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                null
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationNumber")));
    }

    @Test
    @DisplayName("Deve rejeitar comando com payerIdentificationNumber vazio")
    void deveRejeitarComandoComPayerIdentificationNumberVazio() {
        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "cliente@email.com",
                "CPF",
                ""
        );

        var violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationNumber")));
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("100.00");
        String descricao = "Serviço";
        LocalDateTime data = LocalDateTime.now();
        String email = "cliente@email.com";
        String idType = "CPF";
        String idNumber = "12345678909";

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                osId,
                clienteId,
                veiculoId,
                valor,
                descricao,
                data,
                email,
                idType,
                idNumber
        );

        assertEquals(osId, command.ordemServicoId());
        assertEquals(clienteId, command.clienteId());
        assertEquals(veiculoId, command.veiculoId());
        assertEquals(valor, command.valorTotal());
        assertEquals(descricao, command.descricaoServicos());
        assertEquals(data, command.dataOrcamento());
        assertEquals(email, command.payerEmail());
        assertEquals(idType, command.payerIdentificationType());
        assertEquals(idNumber, command.payerIdentificationNumber());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para comandos iguais")
    void devePermitirEqualsEHashCodeParaComandosIguais() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now();

        CriarOrcamentoCommand command1 = new CriarOrcamentoCommand(
                osId,
                clienteId,
                veiculoId,
                new BigDecimal("100.00"),
                "Serviço",
                data,
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        CriarOrcamentoCommand command2 = new CriarOrcamentoCommand(
                osId,
                clienteId,
                veiculoId,
                new BigDecimal("100.00"),
                "Serviço",
                data,
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        assertEquals(command1, command2);
        assertEquals(command1.hashCode(), command2.hashCode());
    }
}
