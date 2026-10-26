package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.infrastructure.persistence.mappers.PagamentoMongoMapper;
import com.techchallenge.billing.infrastructure.persistence.repositories.PagamentoMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PagamentoRepositoryImpl implements PagamentoGateway {

    private final PagamentoMongoRepository repository;
    private final PagamentoMongoMapper mapper;

    @Override
    public Pagamento save(Pagamento pagamento) {
        return mapper.toDomain(repository.save(mapper.toDocument(pagamento)));
    }

    @Override
    public Optional<Pagamento> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Pagamento> findByOrcamentoId(UUID orcamentoId) {
        return repository.findAllByOrcamentoId(orcamentoId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Pagamento> findByOsId(UUID osId) {
        return repository.findAllByOsId(osId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Pagamento> findByMercadoPagoPaymentId(String mercadoPagoPaymentId) {
        return repository.findByMercadoPagoPaymentId(mercadoPagoPaymentId).map(mapper::toDomain);
    }
}
