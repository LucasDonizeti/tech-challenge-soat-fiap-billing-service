package com.techchallenge.billing.domain.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - OrcamentoProntoEvent")
class OrcamentoProntoEventTest {

    @Test
    @DisplayName("Deve criar evento com todos os campos")
    void deveCriarEventoComTodosOsCampos() {
        LocalDateTime dataOrcamento = LocalDateTime.now();
        OrcamentoProntoEvent event = new OrcamentoProntoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("150.00"),
                "Troca de óleo",
                dataOrcamento,
                "cliente@email.com",
                "CPF",
                "12345678909"
        );

        assertNotNull(event.ordemServicoId());
        assertNotNull(event.clienteId());
        assertNotNull(event.veiculoId());
        assertEquals(new BigDecimal("150.00"), event.valorTotal());
        assertEquals("Troca de óleo", event.descricaoServicos());
        assertEquals(dataOrcamento, event.dataOrcamento());
        assertEquals("cliente@email.com", event.payerEmail());
        assertEquals("CPF", event.payerIdentificationType());
        assertEquals("12345678909", event.payerIdentificationNumber());
    }

    @Test
    @DisplayName("Deve implementar DomainEvent corretamente")
    void deveImplementarDomainEventCorretamente() {
        LocalDateTime dataOrcamento = LocalDateTime.now();
        OrcamentoProntoEvent event = new OrcamentoProntoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("200.00"),
                "Revisão completa",
                dataOrcamento,
                "user@test.com",
                "CNPJ",
                "11444777000161"
        );

        assertTrue(event instanceof DomainEvent);
        assertEquals(dataOrcamento, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve retornar timestamp correto")
    void deveRetornarTimestampCorreto() {
        LocalDateTime dataOrcamento = LocalDateTime.of(2024, 1, 15, 10, 30);
        OrcamentoProntoEvent event = new OrcamentoProntoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                dataOrcamento,
                "email@test.com",
                "CPF",
                "12345678909"
        );

        assertEquals(dataOrcamento, event.getTimestamp());
    }

    @Test
    @DisplayName("Deve permitir valores nulos para campos opcionais")
    void devePermitirValoresNulosParaCamposOpcionais() {
        OrcamentoProntoEvent event = new OrcamentoProntoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("75.50"),
                "Reparo",
                LocalDateTime.now(),
                null,
                null,
                null
        );

        assertNull(event.payerEmail());
        assertNull(event.payerIdentificationType());
        assertNull(event.payerIdentificationNumber());
    }

    @Test
    @DisplayName("Deve ter registro imutável")
    void deveTerRegistroImutavel() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("300.00");
        String descricao = "Serviço completo";
        LocalDateTime data = LocalDateTime.now();

        OrcamentoProntoEvent event = new OrcamentoProntoEvent(
                osId,
                clienteId,
                veiculoId,
                valor,
                descricao,
                data,
                "email@test.com",
                "CPF",
                "12345678909"
        );

        assertEquals(osId, event.ordemServicoId());
        assertEquals(clienteId, event.clienteId());
        assertEquals(veiculoId, event.veiculoId());
        assertEquals(valor, event.valorTotal());
        assertEquals(descricao, event.descricaoServicos());
    }

    @Test
    @DisplayName("Deve permitir equals e hashCode para registros iguais")
    void devePermitirEqualsEHashCodeParaRegistrosIguais() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.now();

        OrcamentoProntoEvent event1 = new OrcamentoProntoEvent(
                osId,
                clienteId,
                veiculoId,
                new BigDecimal("100.00"),
                "Serviço",
                data,
                "email@test.com",
                "CPF",
                "12345678909"
        );

        OrcamentoProntoEvent event2 = new OrcamentoProntoEvent(
                osId,
                clienteId,
                veiculoId,
                new BigDecimal("100.00"),
                "Serviço",
                data,
                "email@test.com",
                "CPF",
                "12345678909"
        );

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
