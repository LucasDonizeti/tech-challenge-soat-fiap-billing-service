package com.techchallenge.billing.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class BillingAsyncConfig {

    @Bean(name = "webhookTaskExecutor")
    ThreadPoolTaskExecutor webhookTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mercadopago-webhook-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
