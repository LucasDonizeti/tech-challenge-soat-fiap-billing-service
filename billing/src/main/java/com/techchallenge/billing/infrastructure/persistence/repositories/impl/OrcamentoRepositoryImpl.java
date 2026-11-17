package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.infrastructure.persistence.DynamoDbPersistence;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class OrcamentoRepositoryImpl implements OrcamentoGateway {

    private static final String TABLE = "billing-orcamentos";
    private static final DateTimeFormatter INDEX_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
    private static final String CLIENT_INDEX = "clienteId-dataCriacao-index";
    private static final String CLIENT_STATUS_INDEX = "clienteStatus-dataCriacao-index";
    private static final String OS_INDEX = "osId-index";

    private final DynamoDbPersistence dynamo;

    public OrcamentoRepositoryImpl(DynamoDbPersistence dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Orcamento save(Orcamento orcamento) {
        var indexAttributes = Map.of(
                "osId", orcamento.getOsId().toString(),
                "clienteId", orcamento.getClienteId().toString(),
                "clienteStatus", clientStatus(orcamento.getClienteId(), orcamento.getStatus()),
                "dataCriacao", indexDate(orcamento.getDataCriacao())
        );
        return dynamo.save(TABLE, "id", orcamento.getId().toString(), orcamento, indexAttributes,
                Map.of("osId", orcamento.getOsId().toString()));
    }

    @Override
    public Optional<Orcamento> findById(UUID id) {
        return dynamo.get(TABLE, "id", id.toString(), Orcamento.class);
    }

    @Override
    public Optional<Orcamento> findByOsId(UUID osId) {
        return dynamo.findUniqueOwner(TABLE, "id", "osId", osId.toString())
                .flatMap(id -> dynamo.get(TABLE, "id", id, Orcamento.class))
                .or(() -> dynamo.query(TABLE, OS_INDEX, "osId", osId.toString(),
                        null, null, null, Orcamento.class).stream().findFirst());
    }

    @Override
    public List<Orcamento> findByClienteId(UUID clienteId) {
        return queryByCliente(clienteId);
    }

    @Override
    public List<Orcamento> findByClienteIdAndStatus(UUID clienteId, StatusOrcamento status) {
        return queryByClienteAndStatus(clienteId, status);
    }

    @Override
    public Page<Orcamento> findPageByClienteIdAndStatus(
            UUID clienteId,
            StatusOrcamento status,
            Pageable pageable
    ) {
        var matches = status == null
                ? queryByCliente(clienteId)
                : queryByClienteAndStatus(clienteId, status);
        Comparator<Orcamento> comparator = comparatorFor(pageable);
        if (comparator != null) {
            matches.sort(comparator);
        }
        int start = (int) Math.min(pageable.getOffset(), matches.size());
        int end = Math.min(start + pageable.getPageSize(), matches.size());
        return new PageImpl<>(matches.subList(start, end), pageable, matches.size());
    }

    private List<Orcamento> queryByCliente(UUID clienteId) {
        return dynamo.query(TABLE, CLIENT_INDEX, "clienteId", clienteId.toString(),
                null, null, null, Orcamento.class);
    }

    private List<Orcamento> queryByClienteAndStatus(UUID clienteId, StatusOrcamento status) {
        return dynamo.query(TABLE, CLIENT_STATUS_INDEX, "clienteStatus", clientStatus(clienteId, status),
                null, null, null, Orcamento.class);
    }

    private Comparator<Orcamento> comparatorFor(Pageable pageable) {
        Comparator<Orcamento> comparator = null;
        for (var order : pageable.getSort()) {
            Comparator<Orcamento> propertyComparator =
                    (left, right) -> dynamo.compareJsonProperty(
                            left, right, order.getProperty(), order.isIgnoreCase());
            if (order.isDescending()) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        return comparator;
    }

    private static String clientStatus(UUID clienteId, StatusOrcamento status) {
        return clienteId + "#" + status.name();
    }

    private static String indexDate(LocalDateTime date) {
        return date.format(INDEX_DATE_FORMAT);
    }
}
