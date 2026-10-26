package com.techchallenge.billing.infrastructure.persistence.repositories;

import com.techchallenge.billing.infrastructure.persistence.documents.PagamentoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoMongoRepository extends MongoRepository<PagamentoDocument, UUID> {

    List<PagamentoDocument> findAllByOrcamentoId(UUID orcamentoId);

    List<PagamentoDocument> findAllByOsId(UUID osId);

    Optional<PagamentoDocument> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);
}
