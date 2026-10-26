package com.techchallenge.billing.infrastructure.persistence.repositories;

import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.infrastructure.persistence.documents.OrcamentoDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoMongoRepository extends MongoRepository<OrcamentoDocument, UUID> {

    Optional<OrcamentoDocument> findByOsId(UUID osId);

    List<OrcamentoDocument> findAllByClienteId(UUID clienteId);

    List<OrcamentoDocument> findAllByClienteIdAndStatus(
            UUID clienteId,
            StatusOrcamento status
    );

    Page<OrcamentoDocument> findAllByClienteId(UUID clienteId, Pageable pageable);

    Page<OrcamentoDocument> findAllByClienteIdAndStatus(
            UUID clienteId,
            StatusOrcamento status,
            Pageable pageable
    );
}
