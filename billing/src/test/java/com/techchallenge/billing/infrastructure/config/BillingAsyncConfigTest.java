package com.techchallenge.billing.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - BillingAsyncConfig")
class BillingAsyncConfigTest {

    @Test
    @DisplayName("Deve criar bean webhookTaskExecutor")
    void deveCriarBeanWebhookTaskExecutor() {
        BillingAsyncConfig config = new BillingAsyncConfig();

        var executor = config.webhookTaskExecutor();

        assertNotNull(executor);
    }

    @Test
    @DisplayName("Deve configurar corePoolSize corretamente")
    void deveConfigurarCorePoolSizeCorretamente() {
        BillingAsyncConfig config = new BillingAsyncConfig();

        var executor = config.webhookTaskExecutor();

        assertEquals(2, executor.getCorePoolSize());
    }

    @Test
    @DisplayName("Deve configurar maxPoolSize corretamente")
    void deveConfigurarMaxPoolSizeCorretamente() {
        BillingAsyncConfig config = new BillingAsyncConfig();

        var executor = config.webhookTaskExecutor();

        assertEquals(4, executor.getMaxPoolSize());
    }

    @Test
    @DisplayName("Deve configurar queueCapacity corretamente")
    void deveConfigurarQueueCapacityCorretamente() {
        BillingAsyncConfig config = new BillingAsyncConfig();

        var executor = config.webhookTaskExecutor();

        assertEquals(100, executor.getQueueCapacity());
    }

    @Test
    @DisplayName("Deve configurar threadNamePrefix corretamente")
    void deveConfigurarThreadNamePrefixCorretamente() {
        BillingAsyncConfig config = new BillingAsyncConfig();

        var executor = config.webhookTaskExecutor();

        assertEquals("mercadopago-webhook-", executor.getThreadNamePrefix());
    }

}
