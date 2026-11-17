package com.techchallenge.billing.infrastructure.config;

import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.application.gateway.ports.output.SagaEventPublisher;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AwsMessagingConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MessagingConfig.class)
            .withBean(SnsTemplate.class, () -> mock(SnsTemplate.class))
            .withPropertyValues(
                    "messaging.type=aws",
                    "aws.sns.topics.cobranca-pix-gerada=arn:aws:sns:us-east-1:123456789012:cobranca",
                    "aws.sns.topics.pagamento-aprovado=arn:aws:sns:us-east-1:123456789012:aprovado",
                    "aws.sns.topics.pagamento-recusado=arn:aws:sns:us-east-1:123456789012:recusado",
                    "aws.sns.topics.saga-initiated=arn:aws:sns:us-east-1:123456789012:saga-iniciada",
                    "aws.sns.topics.saga-expired=arn:aws:sns:us-east-1:123456789012:saga-expirada");

    @Test
    void shouldProvideBillingAndSagaPublishersInAwsMode() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(BillingEventPublisher.class);
            assertThat(context).hasSingleBean(SagaEventPublisher.class);
        });
    }
}
