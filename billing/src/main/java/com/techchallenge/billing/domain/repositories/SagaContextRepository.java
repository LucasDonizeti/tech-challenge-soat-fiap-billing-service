package com.techchallenge.billing.domain.repositories;

import com.techchallenge.billing.domain.models.SagaContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository MongoDB para SagaContext
 * Gerencia persistência de contextos de sagas distribuídas
 * 
 * Nota: Não usar @Repository aqui, deixar @EnableMongoRepositories criar o bean
 * para evitar ciclo de dependência com SagaContextRepositoryImpl
 */
public interface SagaContextRepository extends MongoRepository<SagaContext, UUID> {

    /**
     * Busca saga por sagaId
     */
    Optional<SagaContext> findBySagaId(UUID sagaId);

    /**
     * Busca todas as sagas associadas ao pagamento para tolerar dados duplicados legados.
     */
    List<SagaContext> findAllByPagamentoId(UUID pagamentoId);

    /**
     * Busca saga por ordemServicoId com status PENDENTE
     * (valida PIX único pendente por ordem)
     */
    @Query("{ 'ordemServicoId': ?0, 'status': { $in: ['INICIADA', 'PENDENTE'] } }")
    Optional<SagaContext> findByOrdemServicoIdAndStatusPendente(UUID ordemServicoId);

    /**
     * Busca sagas expiradas que ainda não foram processadas
     */
    @Query("{ 'status': 'INICIADA', 'dataExpiracao': { $lt: ?0 } }")
    List<SagaContext> findExpiredSagas(LocalDateTime now);
}
