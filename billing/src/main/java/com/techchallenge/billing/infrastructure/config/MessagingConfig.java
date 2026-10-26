package com.techchallenge.billing.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.infrastructure.messaging.AwsSnsEventPublisher;
import com.techchallenge.billing.infrastructure.messaging.LocalEventPublisher;
import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Slf4j
@Configuration
public class MessagingConfig {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper eventObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.type", havingValue = "local")
    public BillingEventPublisher localEventPublisher() {
        log.info("Initializing LOCAL (Mock) Event Publisher; AWS is not required");
        return new LocalEventPublisher();
    }

    @Bean
    @ConditionalOnProperty(name = "messaging.type", havingValue = "aws")
    public BillingEventPublisher awsEventPublisher(
            SnsTemplate snsTemplate,
            ObjectMapper objectMapper) {
        log.info("Initializing AWS SNS Event Publisher");
        return new AwsSnsEventPublisher(
                Objects.requireNonNull(snsTemplate, "snsTemplate must not be null"),
                Objects.requireNonNull(objectMapper, "objectMapper must not be null")
        );
    }
}
