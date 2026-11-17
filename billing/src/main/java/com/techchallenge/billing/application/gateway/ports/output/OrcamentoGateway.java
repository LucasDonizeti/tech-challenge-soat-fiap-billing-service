package com.techchallenge.billing.application.gateway.ports.output;

import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoGateway {

    Orcamento save(Orcamento orcamento);

    Optional<Orcamento> findById(UUID id);

    Optional<Orcamento> findByOsId(UUID osId);

    List<Orcamento> findByClienteId(UUID clienteId);

    List<Orcamento> findByClienteIdAndStatus(UUID clienteId, StatusOrcamento status);

    Page<Orcamento> findPageByClienteIdAndStatus(UUID clienteId, StatusOrcamento status, Pageable pageable);
}
