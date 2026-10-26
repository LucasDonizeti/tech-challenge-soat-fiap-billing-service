package com.techchallenge.billing.infrastructure.web.dtos;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - CriarOrcamentoRequest")
class CriarOrcamentoRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar request com todos os campos válidos")
    void deveCriarRequestComTodosOsCamposValidos() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço de revisão",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        assertNotNull(request.ordemServicoId());
        assertNotNull(request.clienteId());
        assertNotNull(request.veiculoId());
        assertEquals(new BigDecimal("100.00"), request.valorTotal());
        assertEquals("Serviço de revisão", request.descricaoServicos());
        assertEquals("cliente@email.com", request.payerEmail());
        assertEquals("CPF", request.payerIdentificationType());
        assertEquals("12345678909", request.payerIdentificationNumber());

        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar request com ordemServicoId nulo")
    void deveRejeitarRequestComOrdemServicoIdNulo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("ordemServicoId")));
    }

    @Test
    @DisplayName("Deve rejeitar request com clienteId nulo")
    void deveRejeitarRequestComClienteIdNulo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("clienteId")));
    }

    @Test
    @DisplayName("Deve rejeitar request com veiculoId nulo")
    void deveRejeitarRequestComVeiculoIdNulo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new BigDecimal("100.00"),
                "Serviço",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("veiculoId")));
    }

    @Test
    @DisplayName("Deve rejeitar request com valorTotal nulo")
    void deveRejeitarRequestComValorTotalNulo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Serviço",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("valorTotal")));
    }

    @Test
    @DisplayName("Deve rejeitar request com valorTotal negativo")
    void deveRejeitarRequestComValorTotalNegativo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("-50.00"),
                "Serviço",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("valorTotal")));
    }

    @Test
    @DisplayName("Deve rejeitar request com valorTotal zero")
    void deveRejeitarRequestComValorTotalZero() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ZERO,
                "Serviço",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("valorTotal")));
    }

    @Test
    @DisplayName("Deve rejeitar request com descricaoServicos nulo")
    void deveRejeitarRequestComDescricaoServicosNulo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                null,
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricaoServicos")));
    }

    @Test
    @DisplayName("Deve rejeitar request com descricaoServicos vazio")
    void deveRejeitarRequestComDescricaoServicosVazio() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricaoServicos")));
    }

    @Test
    @DisplayName("Deve rejeitar request com payerEmail inválido")
    void deveRejeitarRequestComPayerEmailInvalido() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                "email-invalido",
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerEmail")));
    }

    @Test
    @DisplayName("Deve rejeitar request com payerEmail nulo")
    void deveRejeitarRequestComPayerEmailNulo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                null,
                "CPF",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerEmail")));
    }

    @Test
    @DisplayName("Deve rejeitar request com payerIdentificationType nulo")
    void deveRejeitarRequestComPayerIdentificationTypeNulo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                "cliente@email.com",
                null,
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationType")));
    }

    @Test
    @DisplayName("Deve rejeitar request com payerIdentificationType vazio")
    void deveRejeitarRequestComPayerIdentificationTypeVazio() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                "cliente@email.com",
                "",
                "12345678909"
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationType")));
    }

    @Test
    @DisplayName("Deve rejeitar request com payerIdentificationNumber nulo")
    void deveRejeitarRequestComPayerIdentificationNumberNulo() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                "cliente@email.com",
                "CPF",
                null
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationNumber")));
    }

    @Test
    @DisplayName("Deve rejeitar request com payerIdentificationNumber vazio")
    void deveRejeitarRequestComPayerIdentificationNumberVazio() {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                "cliente@email.com",
                "CPF",
                ""
        );

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("payerIdentificationNumber")));
    }

    @Test
    @DisplayName("Deve ter record imutável")
    void deveTerRecordImutavel() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("100.00");
        String descricao = "Serviço";
        String email = "cliente@email.com";
        String idType = "CPF";
        String idNumber = "12345678909";

        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                osId,
                clienteId,
                veiculoId,
                valor,
                descricao,
                email,
                idType,
                idNumber
        );

        assertEquals(osId, request.ordemServicoId());
        assertEquals(clienteId, request.clienteId());
        assertEquals(veiculoId, request.veiculoId());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para requests iguais")
    void devePermitirEqualsEHashCodeParaRequestsIguais() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("100.00");

        CriarOrcamentoRequest request1 = new CriarOrcamentoRequest(
                osId,
                clienteId,
                veiculoId,
                valor,
                "Serviço",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        CriarOrcamentoRequest request2 = new CriarOrcamentoRequest(
                osId,
                clienteId,
                veiculoId,
                valor,
                "Serviço",
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
}
