package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.infrastructure.persistence.mappers.OrcamentoMongoMapper;
import com.techchallenge.billing.infrastructure.persistence.repositories.OrcamentoMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrcamentoRepositoryImpl implements OrcamentoGateway {

    private final OrcamentoMongoRepository repository;
    private final OrcamentoMongoMapper mapper;

    @Override
    public Orcamento save(Orcamento orcamento) {
        return mapper.toDomain(repository.save(mapper.toDocument(orcamento)));
    }

    @Override
    public Optional<Orcamento> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Orcamento> findByOsId(UUID osId) {
        return repository.findByOsId(osId).map(mapper::toDomain);
    }

    @Override
    public List<Orcamento> findByClienteId(UUID clienteId) {
        return repository.findAllByClienteId(clienteId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Orcamento> findByClienteIdAndStatus(UUID clienteId, StatusOrcamento status) {
        return repository.findAllByClienteIdAndStatus(clienteId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<Orcamento> findPageByClienteIdAndStatus(
            UUID clienteId,
            StatusOrcamento status,
            Pageable pageable
    ) {
        var page = status == null
                ? repository.findAllByClienteId(clienteId, pageable)
                : repository.findAllByClienteIdAndStatus(clienteId, status, pageable);
        return page.map(mapper::toDomain);
    }
}
