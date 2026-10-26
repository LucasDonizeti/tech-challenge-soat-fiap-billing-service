package com.techchallenge.billing.infrastructure.persistence.repositories;

import com.techchallenge.billing.infrastructure.persistence.documents.WebhookEventLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookEventLogMongoRepository extends MongoRepository<WebhookEventLogDocument, UUID> {

    List<WebhookEventLogDocument> findAllByMercadoPagoPaymentId(String mercadoPagoPaymentId);

    List<WebhookEventLogDocument> findAllByStatusProcessamentoAndProximaRetentativaLessThanEqual(
            String statusProcessamento,
            LocalDateTime timestamp
    );
}
