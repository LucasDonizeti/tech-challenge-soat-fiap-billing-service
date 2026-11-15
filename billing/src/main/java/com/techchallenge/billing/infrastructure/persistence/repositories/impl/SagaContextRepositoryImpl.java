package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.domain.models.SagaContext;
import com.techchallenge.billing.domain.repositories.SagaContextRepository;
import com.techchallenge.billing.infrastructure.persistence.DynamoDbPersistence;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SagaContextRepositoryImpl implements SagaContextRepository {

    private static final String TABLE = "billing-saga-contexts";
    private static final DateTimeFormatter INDEX_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
    private final DynamoDbPersistence dynamo;

    public SagaContextRepositoryImpl(DynamoDbPersistence dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public SagaContext save(SagaContext sagaContext) {
        var indexAttributes = new LinkedHashMap<String, String>();
        add(indexAttributes, "pagamentoId", sagaContext.getPagamentoId());
        add(indexAttributes, "ordemServicoId", sagaContext.getOrdemServicoId());
        add(indexAttributes, "status", sagaContext.getStatus());
        add(indexAttributes, "dataExpiracao", sagaContext.getDataExpiracao());
        return dynamo.save(TABLE, "sagaId", sagaContext.getSagaId().toString(),
                sagaContext, indexAttributes, Map.of());
    }

    @Override
    public Optional<SagaContext> findBySagaId(UUID sagaId) {
        return dynamo.get(TABLE, "sagaId", sagaId.toString(), SagaContext.class);
    }

    @Override
    public List<SagaContext> findAllByPagamentoId(UUID pagamentoId) {
        return dynamo.query(TABLE, "pagamentoId-index", "pagamentoId", pagamentoId.toString(),
                null, null, null, SagaContext.class);
    }

    @Override
    public Optional<SagaContext> findByOrdemServicoIdAndStatusPendente(UUID ordemServicoId) {
        return dynamo.query(TABLE, "ordemServicoId-status-index", "ordemServicoId",
                        ordemServicoId.toString(), null, null, null, SagaContext.class)
                .stream()
                .filter(saga -> "INICIADA".equals(saga.getStatus()) || "PENDENTE".equals(saga.getStatus()))
                .findFirst();
    }

    @Override
    public List<SagaContext> findExpiredSagas(LocalDateTime now) {
        return dynamo.query(TABLE, "status-dataExpiracao-index", "status", "INICIADA",
                "dataExpiracao", "<", indexDate(now), SagaContext.class);
    }

    private static void add(Map<String, String> attributes, String name, Object value) {
        if (value != null) {
            attributes.put(name, value instanceof LocalDateTime date ? indexDate(date) : value.toString());
        }
    }

    private static String indexDate(LocalDateTime date) {
        return date.format(INDEX_DATE_FORMAT);
    }
}
