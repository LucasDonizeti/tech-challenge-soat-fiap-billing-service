package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.infrastructure.persistence.DynamoDbPersistence;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class OrcamentoRepositoryImplTest {

    @Mock
    private DynamoDbPersistence dynamo;
    @Mock
    private Orcamento orcamento;
    private OrcamentoRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new OrcamentoRepositoryImpl(dynamo);
    }

    @Test
    void savesWithIndexKeysAndUniqueOrderServiceKey() {
        var id = UUID.randomUUID();
        var osId = UUID.randomUUID();
        var clienteId = UUID.randomUUID();
        var created = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
        when(orcamento.getId()).thenReturn(id);
        when(orcamento.getOsId()).thenReturn(osId);
        when(orcamento.getClienteId()).thenReturn(clienteId);
        when(orcamento.getStatus()).thenReturn(StatusOrcamento.GERADO);
        when(orcamento.getDataCriacao()).thenReturn(created);
        when(dynamo.save("billing-orcamentos", "id", id.toString(), orcamento,
                Map.of(
                        "osId", osId.toString(),
                        "clienteId", clienteId.toString(),
                        "clienteStatus", clienteId + "#GERADO",
                        "dataCriacao", created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"))
                ),
                Map.of("osId", osId.toString()))).thenReturn(orcamento);

        assertEquals(orcamento, repository.save(orcamento));
    }

    @Test
    void findByIdReturnsOrcamentoWhenExists() {
        UUID id = UUID.randomUUID();
        when(dynamo.get("billing-orcamentos", "id", id.toString(), Orcamento.class))
                .thenReturn(Optional.of(orcamento));

        Optional<Orcamento> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(orcamento, result.get());
    }

    @Test
    void findByIdReturnsEmptyWhenNotExists() {
        UUID id = UUID.randomUUID();
        when(dynamo.get("billing-orcamentos", "id", id.toString(), Orcamento.class))
                .thenReturn(Optional.empty());

        Optional<Orcamento> result = repository.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByOsIdReturnsOrcamentoWhenExists() {
        UUID osId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();

        when(dynamo.findUniqueOwner("billing-orcamentos", "id", "osId", osId.toString()))
                .thenReturn(Optional.of(orcamentoId.toString()));
        when(dynamo.get("billing-orcamentos", "id", orcamentoId.toString(), Orcamento.class))
                .thenReturn(Optional.of(orcamento));

        Optional<Orcamento> result = repository.findByOsId(osId);

        assertTrue(result.isPresent());
        assertEquals(orcamento, result.get());
    }

    @Test
    void findByOsIdFallsBackToQueryWhenUniqueOwnerNotFound() {
        UUID osId = UUID.randomUUID();

        when(dynamo.findUniqueOwner("billing-orcamentos", "id", "osId", osId.toString()))
                .thenReturn(Optional.empty());
        when(dynamo.query("billing-orcamentos", "osId-index", "osId", osId.toString(),
                null, null, null, Orcamento.class))
                .thenReturn(List.of(orcamento));

        Optional<Orcamento> result = repository.findByOsId(osId);

        assertTrue(result.isPresent());
        assertEquals(orcamento, result.get());
    }

    @Test
    void findByOsIdReturnsEmptyWhenNotExists() {
        UUID osId = UUID.randomUUID();

        when(dynamo.findUniqueOwner("billing-orcamentos", "id", "osId", osId.toString()))
                .thenReturn(Optional.empty());
        when(dynamo.query("billing-orcamentos", "osId-index", "osId", osId.toString(),
                null, null, null, Orcamento.class))
                .thenReturn(List.of());

        Optional<Orcamento> result = repository.findByOsId(osId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByClienteIdReturnsListOfOrcamentos() {
        UUID clienteId = UUID.randomUUID();

        when(dynamo.query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class))
                .thenReturn(new ArrayList<>(List.of(orcamento)));

        List<Orcamento> result = repository.findByClienteId(clienteId);

        assertEquals(1, result.size());
        assertEquals(orcamento, result.get(0));
    }

    @Test
    void findByClienteIdReturnsEmptyListWhenNoOrcamentos() {
        UUID clienteId = UUID.randomUUID();

        when(dynamo.query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class))
                .thenReturn(new ArrayList<>());

        List<Orcamento> result = repository.findByClienteId(clienteId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByClienteIdAndStatusReturnsListOfOrcamentos() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.GERADO;

        when(dynamo.query("billing-orcamentos", "clienteStatus-dataCriacao-index",
                "clienteStatus", clienteId + "#GERADO", null, null, null, Orcamento.class))
                .thenReturn(new ArrayList<>(List.of(orcamento)));

        List<Orcamento> result = repository.findByClienteIdAndStatus(clienteId, status);

        assertEquals(1, result.size());
        assertEquals(orcamento, result.get(0));
    }

    @Test
    void queriesPaginatedClientOrcamentosThroughCustomerIndex() {
        var clienteId = UUID.randomUUID();
        when(dynamo.query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class))
                .thenReturn(new ArrayList<>(List.of(orcamento)));

        var page = repository.findPageByClienteIdAndStatus(clienteId, null, PageRequest.of(0, 10));

        assertEquals(List.of(orcamento), page.getContent());
        assertEquals(1, page.getTotalElements());
        verify(dynamo).query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class);
    }

    @Test
    void queriesPaginatedClientOrcamentosThroughCustomerStatusIndex() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.GERADO;

        when(dynamo.query("billing-orcamentos", "clienteStatus-dataCriacao-index",
                "clienteStatus", clienteId + "#GERADO", null, null, null, Orcamento.class))
                .thenReturn(new ArrayList<>(List.of(orcamento)));

        Page<Orcamento> page = repository.findPageByClienteIdAndStatus(clienteId, status, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        verify(dynamo).query("billing-orcamentos", "clienteStatus-dataCriacao-index",
                "clienteStatus", clienteId + "#GERADO", null, null, null, Orcamento.class);
    }

    @Test
    void handlesPaginationWithPageRequest() {
        UUID clienteId = UUID.randomUUID();
        Orcamento orc1 = new Orcamento();
        Orcamento orc2 = new Orcamento();
        Orcamento orc3 = new Orcamento();
        List<Orcamento> orcamentos = new ArrayList<>(List.of(orc1, orc2, orc3));

        when(dynamo.query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class))
                .thenReturn(orcamentos);

        Page<Orcamento> page = repository.findPageByClienteIdAndStatus(clienteId, null, PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(0, page.getNumber());
        assertEquals(2, page.getSize());
    }

    @Test
    void handlesPaginationWithSort() {
        UUID clienteId = UUID.randomUUID();
        Orcamento orc1 = new Orcamento();
        Orcamento orc2 = new Orcamento();
        List<Orcamento> orcamentos = new ArrayList<>(List.of(orc1, orc2));

        when(dynamo.query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class))
                .thenReturn(orcamentos);

        Page<Orcamento> page = repository.findPageByClienteIdAndStatus(
                clienteId,
                null,
                PageRequest.of(0, 10, Sort.by("valorTotal").ascending())
        );

        assertEquals(2, page.getContent().size());
    }

    @Test
    void handlesPaginationWithDescendingSort() {
        UUID clienteId = UUID.randomUUID();
        Orcamento orc1 = new Orcamento();
        Orcamento orc2 = new Orcamento();
        List<Orcamento> orcamentos = new ArrayList<>(List.of(orc1, orc2));

        when(dynamo.query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class))
                .thenReturn(orcamentos);

        Page<Orcamento> page = repository.findPageByClienteIdAndStatus(
                clienteId,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataCriacao"))
        );

        assertEquals(2, page.getContent().size());
    }

    @Test
    void handlesEmptyResults() {
        UUID clienteId = UUID.randomUUID();

        when(dynamo.query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class))
                .thenReturn(List.of());

        Page<Orcamento> page = repository.findPageByClienteIdAndStatus(clienteId, null, PageRequest.of(0, 10));

        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void handlesPageBeyondResults() {
        UUID clienteId = UUID.randomUUID();
        Orcamento orc1 = new Orcamento();
        List<Orcamento> orcamentos = new ArrayList<>(List.of(orc1));

        when(dynamo.query("billing-orcamentos", "clienteId-dataCriacao-index",
                "clienteId", clienteId.toString(), null, null, null, Orcamento.class))
                .thenReturn(orcamentos);

        Page<Orcamento> page = repository.findPageByClienteIdAndStatus(clienteId, null, PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
        assertEquals(1, page.getTotalElements());
    }
}
