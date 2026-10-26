package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.infrastructure.persistence.mappers.PagamentoMongoMapper;
import com.techchallenge.billing.infrastructure.persistence.repositories.PagamentoMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PagamentoRepositoryImpl")
class PagamentoRepositoryImplTest {

    @Mock
    private PagamentoMongoRepository repository;

    @Mock
    private PagamentoMongoMapper mapper;

    @Mock
    private Pagamento domainPagamento;

    @Mock
    private com.techchallenge.billing.infrastructure.persistence.documents.PagamentoDocument document;

    private PagamentoRepositoryImpl repositoryImpl;

    @BeforeEach
    void setUp() {
        repositoryImpl = new PagamentoRepositoryImpl(repository, mapper);
    }

    @Test
    @DisplayName("Deve salvar pagamento")
    void deveSalvarPagamento() {
        when(mapper.toDocument(domainPagamento)).thenReturn(document);
        when(repository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(domainPagamento);

        Pagamento result = repositoryImpl.save(domainPagamento);

        assertEquals(domainPagamento, result);
        verify(mapper).toDocument(domainPagamento);
        verify(repository).save(document);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar pagamento por ID")
    void deveEncontrarPagamentoPorId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(domainPagamento);

        Optional<Pagamento> result = repositoryImpl.findById(id);

        assertTrue(result.isPresent());
        assertEquals(domainPagamento, result.get());
        verify(repository).findById(id);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve retornar vazio quando pagamento não encontrado por ID")
    void deveRetornarVazioQuandoPagamentoNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Pagamento> result = repositoryImpl.findById(id);

        assertFalse(result.isPresent());
        verify(repository).findById(id);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Deve encontrar pagamentos por orcamentoId")
    void deveEncontrarPagamentosPorOrcamentoId() {
        UUID orcamentoId = UUID.randomUUID();
        when(repository.findAllByOrcamentoId(orcamentoId)).thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(domainPagamento);

        List<Pagamento> result = repositoryImpl.findByOrcamentoId(orcamentoId);

        assertEquals(1, result.size());
        assertEquals(domainPagamento, result.get(0));
        verify(repository).findAllByOrcamentoId(orcamentoId);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar pagamentos por osId")
    void deveEncontrarPagamentosPorOsId() {
        UUID osId = UUID.randomUUID();
        when(repository.findAllByOsId(osId)).thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(domainPagamento);

        List<Pagamento> result = repositoryImpl.findByOsId(osId);

        assertEquals(1, result.size());
        assertEquals(domainPagamento, result.get(0));
        verify(repository).findAllByOsId(osId);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve encontrar pagamento por mercadoPagoPaymentId")
    void deveEncontrarPagamentoPorMercadoPagoPaymentId() {
        String mercadoPagoPaymentId = "mp-payment-123";
        when(repository.findByMercadoPagoPaymentId(mercadoPagoPaymentId)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(domainPagamento);

        Optional<Pagamento> result = repositoryImpl.findByMercadoPagoPaymentId(mercadoPagoPaymentId);

        assertTrue(result.isPresent());
        assertEquals(domainPagamento, result.get());
        verify(repository).findByMercadoPagoPaymentId(mercadoPagoPaymentId);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Deve retornar vazio quando pagamento não encontrado por mercadoPagoPaymentId")
    void deveRetornarVazioQuandoPagamentoNaoEncontradoPorMercadoPagoPaymentId() {
        String mercadoPagoPaymentId = "mp-payment-123";
        when(repository.findByMercadoPagoPaymentId(mercadoPagoPaymentId)).thenReturn(Optional.empty());

        Optional<Pagamento> result = repositoryImpl.findByMercadoPagoPaymentId(mercadoPagoPaymentId);

        assertFalse(result.isPresent());
        verify(repository).findByMercadoPagoPaymentId(mercadoPagoPaymentId);
        verify(mapper, never()).toDomain(any());
    }
}
