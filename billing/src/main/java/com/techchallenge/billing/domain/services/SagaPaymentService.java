package com.techchallenge.billing.domain.services;

import com.techchallenge.billing.domain.models.SagaContext;
import com.techchallenge.billing.domain.events.SagaPaymentInitiatedEvent;
import com.techchallenge.billing.domain.events.SagaPaymentExpiredEvent;
import com.techchallenge.billing.domain.repositories.SagaContextRepository;
import com.techchallenge.billing.application.gateway.ports.output.SagaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de domínio para gerenciar Sagas de Pagamento PIX
 * Implementa a orquestração da saga coreografada com eventos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaPaymentService {

    private final SagaContextRepository sagaContextRepository;
    private final SagaEventPublisher sagaEventPublisher;

    /**
     * Inicia uma nova saga de pagamento PIX
     * @return SagaContext com sagaId único para rastreamento
     */
    @Transactional
    public SagaContext initiateSaga(
            UUID pagamentoId,
            UUID orcamentoId,
            UUID ordemServicoId,
            UUID clienteId,
            String mercadoPagoPaymentId,
            String qrCodePix,
            String qrCodeBase64,
            BigDecimal valorCobrado,
            LocalDateTime dataExpiracao) {

        List<SagaContext> existingPaymentSagas = sagaContextRepository.findAllByPagamentoId(pagamentoId);
        if (!existingPaymentSagas.isEmpty()) {
            if (existingPaymentSagas.size() > 1) {
                log.error("Encontrados {} SagaContext para pagamentoId={}; reutilizando saga existente",
                        existingPaymentSagas.size(), pagamentoId);
            }
            return existingPaymentSagas.stream()
                    .min(Comparator.comparing(
                            SagaContext::getCriadoEm,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElseThrow();
        }

        // Validar se já existe PIX pendente para esta ordem
        Optional<SagaContext> existingSaga = sagaContextRepository
                .findByOrdemServicoIdAndStatusPendente(ordemServicoId);

        if (existingSaga.isPresent()) {
            log.warn("Tentativa de criar PIX duplicado para osId={}. Saga existente: sagaId={}",
                    ordemServicoId, existingSaga.get().getSagaId());
            return existingSaga.get();
        }

        // Criar novo contexto de saga
        SagaContext sagaContext = SagaContext.criar(
                ordemServicoId,
                pagamentoId,
                orcamentoId,
                mercadoPagoPaymentId,
                dataExpiracao
        );
        sagaContext.adicionarDado("clienteId", clienteId);

        // Persistir saga
        sagaContext = sagaContextRepository.save(sagaContext);

        // Publicar evento de iniciação
        SagaPaymentInitiatedEvent initiatedEvent = new SagaPaymentInitiatedEvent(
                sagaContext.getSagaId(),
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                mercadoPagoPaymentId,
                qrCodePix,
                qrCodeBase64,
                valorCobrado,
                dataExpiracao
        );

        sagaEventPublisher.publishSagaPaymentInitiated(initiatedEvent);
        sagaContext.marcarPendente();
        sagaContext = sagaContextRepository.save(sagaContext);

        log.info("Saga iniciada com sucesso: sagaId={}, osId={}", sagaContext.getSagaId(), ordemServicoId);
        return sagaContext;
    }

    /**
     * Confirma o resultado final de uma saga (aprovada, recusada ou expirada)
     */
    @Transactional
    public void confirmSagaPayment(
            UUID pagamentoId,
            String statusFinal, // APROVADO, RECUSADO, EXPIRADO
            BigDecimal valorPago,
            LocalDateTime dataPagamento,
            String motivo) {

        String sagaStatus = switch (statusFinal) {
                case "APROVADO" -> "APROVADA";
                case "RECUSADO" -> "RECUSADA";
                case "EXPIRADO" -> "EXPIRADA";
                case "CANCELADO" -> "CANCELADA";
                default -> {
                    log.warn("Status desconhecido para saga: {}", statusFinal);
                    yield null;
                }
        };
        if (sagaStatus == null) {
                return;
        }

        List<SagaContext> sagas = sagaContextRepository.findAllByPagamentoId(pagamentoId);
        if (sagas.isEmpty()) {
                log.warn("SagaContext não encontrado para pagamentoId={}", pagamentoId);
                return;
        }
        if (sagas.size() > 1) {
                log.error("Encontrados {} SagaContext para pagamentoId={}; atualizando todos",
                        sagas.size(), pagamentoId);
        }
        if (sagas.stream().allMatch(saga -> sagaStatus.equals(saga.getStatus()))) {
                log.info("Saga já confirmada: pagamentoId={}, statusFinal={}", pagamentoId, statusFinal);
                return;
        }

        SagaContext sagaContext = sagas.stream()
                    .min(Comparator.comparing(
                            SagaContext::getCriadoEm,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElseThrow();
        sagas.forEach(saga -> setStatus(saga, statusFinal));

        sagas.forEach(sagaContextRepository::save);

        log.info("Saga confirmada: sagaId={}, statusFinal={}", sagaContext.getSagaId(), statusFinal);
    }

    private void setStatus(SagaContext sagaContext, String statusFinal) {
        switch (statusFinal) {
            case "APROVADO" -> sagaContext.marcarAprovada();
            case "RECUSADO" -> sagaContext.marcarRecusada();
            case "EXPIRADO" -> sagaContext.marcarExpirada();
            case "CANCELADO" -> sagaContext.marcarCancelada();
            default -> throw new IllegalArgumentException("Status de saga não suportado: " + statusFinal);
        }
    }

    /**
     * Processa sagas expiradas (PIXs que não foram pagos no prazo)
     * Deve ser chamado periodicamente por um scheduler
     */
    @Transactional
    public void processExpiredSagas() {
        List<SagaContext> expiredSagas = sagaContextRepository.findExpiredSagas(LocalDateTime.now());

        if (expiredSagas.isEmpty()) {
            log.debug("Nenhuma saga expirada para processar");
            return;
        }

        log.info("Processando {} sagas expiradas", expiredSagas.size());

        for (SagaContext saga : expiredSagas) {
            try {
                saga.marcarExpirada();
                sagaContextRepository.save(saga);

                // Publicar evento de expiração
                SagaPaymentExpiredEvent expiredEvent = new SagaPaymentExpiredEvent(
                        saga.getSagaId(),
                        saga.getPagamentoId(),
                        saga.getOrdemServicoId(),
                        saga.getMercadoPagoPaymentId(),
                        saga.getDataExpiracao()
                );

                sagaEventPublisher.publishSagaPaymentExpired(expiredEvent);

                log.info("Saga marcada como expirada: sagaId={}", saga.getSagaId());
            } catch (Exception e) {
                log.error("Erro ao processar saga expirada: sagaId={}", saga.getSagaId(), e);
            }
        }
    }

    /**
     * Recupera contexto de saga por ID para rastreamento
     */
    public Optional<SagaContext> getSagaContext(UUID sagaId) {
        return sagaContextRepository.findBySagaId(sagaId);
    }

    /**
     * Verifica se existe PIX pendente para uma ordem de serviço
     * Evita criação de múltiplos PIXs
     */
    public boolean hasPendingPixForOrdemServico(UUID ordemServicoId) {
        return sagaContextRepository.findByOrdemServicoIdAndStatusPendente(ordemServicoId).isPresent();
    }
}
