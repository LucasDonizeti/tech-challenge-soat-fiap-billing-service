package com.techchallenge.billing.application.gateway.ports.output;

import com.techchallenge.billing.domain.models.Pagamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoGateway {

    Pagamento save(Pagamento pagamento);

    Optional<Pagamento> findById(UUID id);

    List<Pagamento> findByOrcamentoId(UUID orcamentoId);

    List<Pagamento> findByOsId(UUID osId);

    Optional<Pagamento> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);
}
