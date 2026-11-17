package com.techchallenge.billing.application.gateway.ports.output;

import com.techchallenge.billing.domain.models.SagaContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gateway para persistência de contexto de Saga
 * Permite rastreamento e recuperação de estado de sagas distribuídas
 */
public interface SagaContextGateway {
    /**
     * Salva o contexto de uma saga
     */
    SagaContext save(SagaContext sagaContext);

    /**
     * Busca saga por ID
     */
    Optional<SagaContext> findBySagaId(UUID sagaId);

    /**
     * Busca saga por ID de pagamento (validação de PIX duplicado)
     */
    List<SagaContext> findAllByPagamentoId(UUID pagamentoId);

    /**
     * Busca saga por ID de ordem de serviço e status pendente
     * (para verificar se já existe PIX pendente)
     */
    Optional<SagaContext> findByOrdemServicoIdAndStatusPendente(UUID ordemServicoId);

    /**
     * Busca todas as sagas com status "INICIADA" e data de expiração vencida
     * (para processar PIXs expirados)
     */
    java.util.List<SagaContext> findExpiredSagas();
}
