package com.techchallenge.billing.domain.repositories;

import com.techchallenge.billing.domain.models.SagaContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaContextRepository {

    SagaContext save(SagaContext sagaContext);
    Optional<SagaContext> findBySagaId(UUID sagaId);
    List<SagaContext> findAllByPagamentoId(UUID pagamentoId);
    Optional<SagaContext> findByOrdemServicoIdAndStatusPendente(UUID ordemServicoId);
    List<SagaContext> findExpiredSagas(java.time.LocalDateTime now);
}
