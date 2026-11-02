package com.techchallenge.billing.infrastructure.scheduling;

import com.techchallenge.billing.domain.services.SagaPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para processar sagas de pagamento PIX
 * Executa periodicamente para:
 * 1. Detectar PIXs expirados
 * 2. Publicar eventos de expiração
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "billing.saga.scheduler.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class SagaScheduler {

    private final SagaPaymentService sagaPaymentService;

    /**
     * Processa sagas expiradas a cada 5 minutos
     * Verifica PIXs que ultrapassaram o tempo de expiração (padrão: 30 minutos)
     */
    @Scheduled(fixedDelayString = "${billing.saga.scheduler.expired-check-interval:300000}")
    public void processExpiredPixs() {
        try {
            log.info("Iniciando verificação de PIXs expirados");
            sagaPaymentService.processExpiredSagas();
            log.info("Verificação de PIXs expirados concluída");
        } catch (Exception e) {
            log.error("Erro ao processar PIXs expirados", e);
        }
    }
}
