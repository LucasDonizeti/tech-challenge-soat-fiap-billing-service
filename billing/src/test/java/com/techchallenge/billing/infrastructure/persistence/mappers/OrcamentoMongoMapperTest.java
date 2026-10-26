package com.techchallenge.billing.infrastructure.persistence.mappers;

import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamentoChange;
import com.techchallenge.billing.infrastructure.persistence.documents.OrcamentoDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - OrcamentoMongoMapper")
class OrcamentoMongoMapperTest {

    private OrcamentoMongoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrcamentoMongoMapper();
    }

    @Test
    @DisplayName("Deve converter Orcamento para OrcamentoDocument")
    void deveConverterOrcamentoParaOrcamentoDocument() {
        Orcamento orcamento = createOrcamento();

        OrcamentoDocument document = mapper.toDocument(orcamento);

        assertNotNull(document);
        assertEquals(orcamento.getId(), document.getId());
        assertEquals(orcamento.getOsId(), document.getOsId());
        assertEquals(orcamento.getClienteId(), document.getClienteId());
        assertEquals(orcamento.getVeiculoId(), document.getVeiculoId());
        assertEquals(orcamento.getValorTotal(), document.getValorTotal());
        assertEquals(orcamento.getStatus(), document.getStatus());
        assertEquals(orcamento.getDataCriacao(), document.getDataCriacao());
        assertEquals(orcamento.getDataAtualizacao(), document.getDataAtualizacao());
        assertEquals(orcamento.getDescricaoServicos(), document.getDescricaoServicos());
        assertEquals(orcamento.getHistoricoStatusChanges(), document.getHistoricoStatusChanges());
    }

    @Test
    @DisplayName("Deve converter OrcamentoDocument para Orcamento")
    void deveConverterOrcamentoDocumentParaOrcamento() {
        OrcamentoDocument document = createOrcamentoDocument();

        Orcamento orcamento = mapper.toDomain(document);

        assertNotNull(orcamento);
        assertEquals(document.getId(), orcamento.getId());
        assertEquals(document.getOsId(), orcamento.getOsId());
        assertEquals(document.getClienteId(), orcamento.getClienteId());
        assertEquals(document.getVeiculoId(), orcamento.getVeiculoId());
        assertEquals(document.getValorTotal(), orcamento.getValorTotal());
        assertEquals(document.getStatus(), orcamento.getStatus());
        assertEquals(document.getDataCriacao(), orcamento.getDataCriacao());
        assertEquals(document.getDataAtualizacao(), orcamento.getDataAtualizacao());
        assertEquals(document.getDescricaoServicos(), orcamento.getDescricaoServicos());
        assertEquals(document.getHistoricoStatusChanges(), orcamento.getHistoricoStatusChanges());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Orcamento é nulo ao converter para document")
    void deveLancarExcecaoQuandoOrcamentoENuloAoConverterParaDocument() {
        assertThrows(NullPointerException.class, () -> mapper.toDocument(null));
    }

    @Test
    @DisplayName("Deve lançar exceção quando OrcamentoDocument é nulo ao converter para domain")
    void deveLancarExcecaoQuandoOrcamentoDocumentENuloAoConverterParaDomain() {
        assertThrows(NullPointerException.class, () -> mapper.toDomain(null));
    }

    @Test
    @DisplayName("Deve manter consistência em conversão bidirecional")
    void deveManterConsistenciaEmConversaoBidirecional() {
        Orcamento original = createOrcamento();

        OrcamentoDocument document = mapper.toDocument(original);
        Orcamento restored = mapper.toDomain(document);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getOsId(), restored.getOsId());
        assertEquals(original.getClienteId(), restored.getClienteId());
        assertEquals(original.getVeiculoId(), restored.getVeiculoId());
        assertEquals(original.getValorTotal(), restored.getValorTotal());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getDataCriacao(), restored.getDataCriacao());
        assertEquals(original.getDataAtualizacao(), restored.getDataAtualizacao());
        assertEquals(original.getDescricaoServicos(), restored.getDescricaoServicos());
        assertEquals(original.getHistoricoStatusChanges(), restored.getHistoricoStatusChanges());
    }

    @Test
    @DisplayName("Deve criar nova lista para historicoStatusChanges ao converter para document")
    void deveCriarNovaListaParaHistoricoStatusChangesAoConverterParaDocument() {
        Orcamento orcamento = createOrcamento();
        List<StatusOrcamentoChange> originalHistory = orcamento.getHistoricoStatusChanges();

        OrcamentoDocument document = mapper.toDocument(orcamento);

        assertNotNull(document.getHistoricoStatusChanges());
        assertNotSame(originalHistory, document.getHistoricoStatusChanges());
        assertEquals(originalHistory, document.getHistoricoStatusChanges());
    }

    @Test
    @DisplayName("Deve criar nova lista para historicoStatusChanges ao converter para domain")
    void deveCriarNovaListaParaHistoricoStatusChangesAoConverterParaDomain() {
        OrcamentoDocument document = createOrcamentoDocument();
        List<StatusOrcamentoChange> originalHistory = document.getHistoricoStatusChanges();

        Orcamento orcamento = mapper.toDomain(document);

        assertNotNull(orcamento.getHistoricoStatusChanges());
        assertNotSame(originalHistory, orcamento.getHistoricoStatusChanges());
        assertEquals(originalHistory, orcamento.getHistoricoStatusChanges());
    }

    @Test
    @DisplayName("Deve converter Orcamento com historicoStatusChanges nulo")
    void deveConverterOrcamentoComHistoricoStatusChangesNulo() {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(UUID.randomUUID());
        orcamento.setOsId(UUID.randomUUID());
        orcamento.setClienteId(UUID.randomUUID());
        orcamento.setVeiculoId(UUID.randomUUID());
        orcamento.setValorTotal(new BigDecimal("100.00"));
        orcamento.setStatus(StatusOrcamento.GERADO);
        orcamento.setDataCriacao(LocalDateTime.now());
        orcamento.setDataAtualizacao(LocalDateTime.now());
        orcamento.setDescricaoServicos("Serviço");
        orcamento.setHistoricoStatusChanges(null);

        OrcamentoDocument document = mapper.toDocument(orcamento);

        assertNotNull(document.getHistoricoStatusChanges());
        assertTrue(document.getHistoricoStatusChanges().isEmpty());
    }

    @Test
    @DisplayName("Deve converter OrcamentoDocument com historicoStatusChanges nulo")
    void deveConverterOrcamentoDocumentComHistoricoStatusChangesNulo() {
        OrcamentoDocument document = new OrcamentoDocument();
        document.setId(UUID.randomUUID());
        document.setOsId(UUID.randomUUID());
        document.setClienteId(UUID.randomUUID());
        document.setVeiculoId(UUID.randomUUID());
        document.setValorTotal(new BigDecimal("100.00"));
        document.setStatus(StatusOrcamento.GERADO);
        document.setDataCriacao(LocalDateTime.now());
        document.setDataAtualizacao(LocalDateTime.now());
        document.setDescricaoServicos("Serviço");
        document.setHistoricoStatusChanges(null);

        Orcamento orcamento = mapper.toDomain(document);

        assertNotNull(orcamento.getHistoricoStatusChanges());
        assertTrue(orcamento.getHistoricoStatusChanges().isEmpty());
    }

    private Orcamento createOrcamento() {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(UUID.randomUUID());
        orcamento.setOsId(UUID.randomUUID());
        orcamento.setClienteId(UUID.randomUUID());
        orcamento.setVeiculoId(UUID.randomUUID());
        orcamento.setValorTotal(new BigDecimal("150.00"));
        orcamento.setStatus(StatusOrcamento.GERADO);
        orcamento.setDataCriacao(LocalDateTime.now());
        orcamento.setDataAtualizacao(LocalDateTime.now());
        orcamento.setDescricaoServicos("Troca de óleo");
        orcamento.setHistoricoStatusChanges(List.of(
                new StatusOrcamentoChange(StatusOrcamento.GERADO, LocalDateTime.now())
        ));
        return orcamento;
    }

    private OrcamentoDocument createOrcamentoDocument() {
        OrcamentoDocument document = new OrcamentoDocument();
        document.setId(UUID.randomUUID());
        document.setOsId(UUID.randomUUID());
        document.setClienteId(UUID.randomUUID());
        document.setVeiculoId(UUID.randomUUID());
        document.setValorTotal(new BigDecimal("200.00"));
        document.setStatus(StatusOrcamento.PAGO);
        document.setDataCriacao(LocalDateTime.now());
        document.setDataAtualizacao(LocalDateTime.now());
        document.setDescricaoServicos("Revisão completa");
        document.setHistoricoStatusChanges(List.of(
                new StatusOrcamentoChange(StatusOrcamento.GERADO, LocalDateTime.now()),
                new StatusOrcamentoChange(StatusOrcamento.PAGO, LocalDateTime.now())
        ));
        return document;
    }
}
