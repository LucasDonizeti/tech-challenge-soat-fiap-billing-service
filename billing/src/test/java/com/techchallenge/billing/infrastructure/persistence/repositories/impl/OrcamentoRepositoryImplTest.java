package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.infrastructure.persistence.mappers.OrcamentoMongoMapper;
import com.techchallenge.billing.infrastructure.persistence.repositories.OrcamentoMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - OrcamentoRepositoryImpl")
class OrcamentoRepositoryImplTest {

    @Mock
    private OrcamentoMongoRepository repository;

    @Mock
    private OrcamentoMongoMapper mapper;

    @Mock
    private Orcamento domainOrcamento;

    @Mock
    private com.techchallenge.billing.infrastructure.persistence.documents.OrcamentoDocument document;

    private OrcamentoRepositoryImpl repositoryImpl;

    @BeforeEach
    void setUp() {
        repositoryImpl = new OrcamentoRepositoryImpl(repository, mapper);
    }

    @Test
    @DisplayName("Deve salvar orçamento")
    void deveSalvarOrcamento() {
        when(mapper.toDocument(domainOrcamento)).thenReturn(document);
        when(repository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(domainOrcamento);

        Orcamento result = repositoryImpl.save(domainOrcamento);

        assertEquals(domainOrcamento, result);
        verify(mapper).toDocument(domainOrcamento);
        verify(repository).save(document);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar orçamento por ID")
    void deveEncontrarOrcamentoPorId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(domainOrcamento);

        Optional<Orcamento> result = repositoryImpl.findById(id);

        assertTrue(result.isPresent());
        assertEquals(domainOrcamento, result.get());
        verify(repository).findById(id);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve retornar vazio quando orçamento não encontrado por ID")
    void deveRetornarVazioQuandoOrcamentoNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Orcamento> result = repositoryImpl.findById(id);

        assertFalse(result.isPresent());
        verify(repository).findById(id);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Deve encontrar orçamento por OsId")
    void deveEncontrarOrcamentoPorOsId() {
        UUID osId = UUID.randomUUID();
        when(repository.findByOsId(osId)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(domainOrcamento);

        Optional<Orcamento> result = repositoryImpl.findByOsId(osId);

        assertTrue(result.isPresent());
        assertEquals(domainOrcamento, result.get());
        verify(repository).findByOsId(osId);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar orçamentos por clienteId")
    void deveEncontrarOrcamentosPorClienteId() {
        UUID clienteId = UUID.randomUUID();
        when(repository.findAllByClienteId(clienteId)).thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(domainOrcamento);

        List<Orcamento> result = repositoryImpl.findByClienteId(clienteId);

        assertEquals(1, result.size());
        assertEquals(domainOrcamento, result.get(0));
        verify(repository).findAllByClienteId(clienteId);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar orçamentos por clienteId e status")
    void deveEncontrarOrcamentosPorClienteIdEStatus() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.PAGO;
        when(repository.findAllByClienteIdAndStatus(clienteId, status)).thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(domainOrcamento);

        List<Orcamento> result = repositoryImpl.findByClienteIdAndStatus(clienteId, status);

        assertEquals(1, result.size());
        assertEquals(domainOrcamento, result.get(0));
        verify(repository).findAllByClienteIdAndStatus(clienteId, status);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar orçamentos paginados por clienteId e status null")
    void deveEncontrarOrcamentosPaginadosPorClienteIdEStatusNull() {
        UUID clienteId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<com.techchallenge.billing.infrastructure.persistence.documents.OrcamentoDocument> page = new PageImpl<>(List.of(document));

        when(repository.findAllByClienteId(clienteId, pageable)).thenReturn(page);
        when(mapper.toDomain(document)).thenReturn(domainOrcamento);

        Page<Orcamento> result = repositoryImpl.findPageByClienteIdAndStatus(clienteId, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(domainOrcamento, result.getContent().get(0));
        verify(repository).findAllByClienteId(clienteId, pageable);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar orçamentos paginados por clienteId e status específico")
    void deveEncontrarOrcamentosPaginadosPorClienteIdEStatusEspecifico() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.PAGO;
        Pageable pageable = PageRequest.of(0, 10);
        Page<com.techchallenge.billing.infrastructure.persistence.documents.OrcamentoDocument> page = new PageImpl<>(List.of(document));

        when(repository.findAllByClienteIdAndStatus(clienteId, status, pageable)).thenReturn(page);
        when(mapper.toDomain(document)).thenReturn(domainOrcamento);

        Page<Orcamento> result = repositoryImpl.findPageByClienteIdAndStatus(clienteId, status, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(domainOrcamento, result.getContent().get(0));
        verify(repository).findAllByClienteIdAndStatus(clienteId, status, pageable);
        verify(mapper).toDomain(document);
    }
}
