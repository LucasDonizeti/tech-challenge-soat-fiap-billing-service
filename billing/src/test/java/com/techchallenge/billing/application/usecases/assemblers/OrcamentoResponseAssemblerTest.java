package com.techchallenge.billing.application.usecases.assemblers;

import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - OrcamentoResponseAssembler")
class OrcamentoResponseAssemblerTest {

    private OrcamentoResponseAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new OrcamentoResponseAssembler();
    }

    @Test
    @DisplayName("Deve converter Orcamento para OrcamentoResponse")
    void deveConverterOrcamentoParaOrcamentoResponse() {
        Orcamento orcamento = createOrcamento();

        OrcamentoResponse response = assembler.toResponse(orcamento);

        assertNotNull(response);
        assertEquals(orcamento.getId(), response.id());
        assertEquals(orcamento.getOsId(), response.ordemServicoId());
        assertEquals(orcamento.getClienteId(), response.clienteId());
        assertEquals(orcamento.getValorTotal(), response.valorTotal());
        assertEquals(orcamento.getStatus().name(), response.status());
        assertEquals(orcamento.getDataCriacao(), response.dataCriacao());
        assertEquals(orcamento.getDataAtualizacao(), response.dataAtualizacao());
    }

    @Test
    @DisplayName("Deve converter status para string")
    void deveConverterStatusParaString() {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(UUID.randomUUID());
        orcamento.setOsId(UUID.randomUUID());
        orcamento.setClienteId(UUID.randomUUID());
        orcamento.setValorTotal(new BigDecimal("100.00"));
        orcamento.setStatus(StatusOrcamento.PAGO);
        orcamento.setDataCriacao(LocalDateTime.now());
        orcamento.setDataAtualizacao(LocalDateTime.now());

        OrcamentoResponse response = assembler.toResponse(orcamento);

        assertEquals("PAGO", response.status());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Orcamento é nulo")
    void deveLancarExcecaoQuandoOrcamentoENulo() {
        assertThrows(NullPointerException.class, () -> assembler.toResponse(null));
    }

    @Test
    @DisplayName("Deve converter Orcamento com diferentes status")
    void deveConverterOrcamentoComDiferentesStatus() {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(UUID.randomUUID());
        orcamento.setOsId(UUID.randomUUID());
        orcamento.setClienteId(UUID.randomUUID());
        orcamento.setValorTotal(new BigDecimal("100.00"));
        orcamento.setDataCriacao(LocalDateTime.now());
        orcamento.setDataAtualizacao(LocalDateTime.now());

        orcamento.setStatus(StatusOrcamento.GERADO);
        OrcamentoResponse response1 = assembler.toResponse(orcamento);
        assertEquals("GERADO", response1.status());

        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
        OrcamentoResponse response2 = assembler.toResponse(orcamento);
        assertEquals("AGUARDANDO_PAGAMENTO", response2.status());

        orcamento.setStatus(StatusOrcamento.PAGO);
        OrcamentoResponse response3 = assembler.toResponse(orcamento);
        assertEquals("PAGO", response3.status());

        orcamento.setStatus(StatusOrcamento.RECUSADO);
        OrcamentoResponse response4 = assembler.toResponse(orcamento);
        assertEquals("RECUSADO", response4.status());

        orcamento.setStatus(StatusOrcamento.CANCELADO);
        OrcamentoResponse response5 = assembler.toResponse(orcamento);
        assertEquals("CANCELADO", response5.status());
    }

    @Test
    @DisplayName("Deve converter Orcamento com todos os campos preenchidos")
    void deveConverterOrcamentoComTodosOsCamposPreenchidos() {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(UUID.randomUUID());
        orcamento.setOsId(UUID.randomUUID());
        orcamento.setClienteId(UUID.randomUUID());
        orcamento.setValorTotal(new BigDecimal("250.50"));
        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
        orcamento.setDataCriacao(LocalDateTime.of(2024, 1, 15, 10, 0));
        orcamento.setDataAtualizacao(LocalDateTime.of(2024, 1, 15, 10, 30));

        OrcamentoResponse response = assembler.toResponse(orcamento);

        assertNotNull(response);
        assertEquals(new BigDecimal("250.50"), response.valorTotal());
        assertEquals("AGUARDANDO_PAGAMENTO", response.status());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), response.dataCriacao());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30), response.dataAtualizacao());
    }

    private Orcamento createOrcamento() {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(UUID.randomUUID());
        orcamento.setOsId(UUID.randomUUID());
        orcamento.setClienteId(UUID.randomUUID());
        orcamento.setValorTotal(new BigDecimal("150.00"));
        orcamento.setStatus(StatusOrcamento.GERADO);
        orcamento.setDataCriacao(LocalDateTime.now());
        orcamento.setDataAtualizacao(LocalDateTime.now());
        return orcamento;
    }
}
