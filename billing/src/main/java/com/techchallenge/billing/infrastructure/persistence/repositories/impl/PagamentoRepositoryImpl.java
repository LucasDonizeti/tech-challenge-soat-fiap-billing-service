package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.infrastructure.persistence.DynamoDbPersistence;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PagamentoRepositoryImpl implements PagamentoGateway {

    private static final String TABLE = "billing-pagamentos";
    private final DynamoDbPersistence dynamo;

    public PagamentoRepositoryImpl(DynamoDbPersistence dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public Pagamento save(Pagamento pagamento) {
        var indexAttributes = new LinkedHashMap<String, String>();
        add(indexAttributes, "orcamentoId", pagamento.getOrcamentoId());
        add(indexAttributes, "osId", pagamento.getOsId());
        add(indexAttributes, "mercadoPagoPaymentId", pagamento.getMercadoPagoPaymentId());
        var uniqueAttributes = new LinkedHashMap<String, String>();
        add(uniqueAttributes, "mercadoPagoPaymentId", pagamento.getMercadoPagoPaymentId());
        return dynamo.save(TABLE, "id", pagamento.getId().toString(), pagamento,
                indexAttributes, uniqueAttributes);
    }

    @Override
    public Optional<Pagamento> findById(UUID id) {
        return dynamo.get(TABLE, "id", id.toString(), Pagamento.class);
    }

    @Override
    public List<Pagamento> findByOrcamentoId(UUID orcamentoId) {
        return dynamo.query(TABLE, "orcamentoId-index", "orcamentoId", orcamentoId.toString(),
                null, null, null, Pagamento.class);
    }

    @Override
    public List<Pagamento> findByOsId(UUID osId) {
        return dynamo.query(TABLE, "osId-index", "osId", osId.toString(),
                null, null, null, Pagamento.class);
    }

    @Override
    public Optional<Pagamento> findByMercadoPagoPaymentId(String mercadoPagoPaymentId) {
        return dynamo.findUniqueOwner(TABLE, "id", "mercadoPagoPaymentId", mercadoPagoPaymentId)
                .flatMap(id -> dynamo.get(TABLE, "id", id, Pagamento.class))
                .or(() -> dynamo.query(TABLE, "mercadoPagoPaymentId-index", "mercadoPagoPaymentId",
                        mercadoPagoPaymentId, null, null, null, Pagamento.class).stream().findFirst());
    }

    private static void add(Map<String, String> attributes, String name, Object value) {
        if (value != null) {
            attributes.put(name, value.toString());
        }
    }
}
