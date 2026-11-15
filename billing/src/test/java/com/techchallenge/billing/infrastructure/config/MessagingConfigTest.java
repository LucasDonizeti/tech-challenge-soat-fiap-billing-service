package com.techchallenge.billing.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.infrastructure.messaging.AwsSnsEventPublisher;
import com.techchallenge.billing.infrastructure.messaging.LocalEventPublisher;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - MessagingConfig")
class MessagingConfigTest {

    @Mock
    private SnsTemplate snsTemplate;

    private MessagingConfig config;

    @BeforeEach
    void setUp() {
        config = new MessagingConfig();
    }

    @Test
    @DisplayName("Deve criar bean ObjectMapper quando não existe")
    void deveCriarBeanObjectMapperQuandoNaoExiste() {
        ObjectMapper mapper = config.eventObjectMapper();

        assertNotNull(mapper);
    }

    @Test
    @DisplayName("Deve criar bean LocalEventPublisher")
    void deveCriarBeanLocalEventPublisher() {
        BillingEventPublisher publisher = config.localEventPublisher();

        assertNotNull(publisher);
        assertTrue(publisher instanceof LocalEventPublisher);
    }

    @Test
    @DisplayName("Deve criar bean AwsSnsEventPublisher")
    void deveCriarBeanAwsSnsEventPublisher() {
        ObjectMapper objectMapper = new ObjectMapper();
        BillingEventPublisher publisher = config.awsEventPublisher(snsTemplate, objectMapper);

        assertNotNull(publisher);
        assertTrue(publisher instanceof AwsSnsEventPublisher);
    }

    @Test
    @DisplayName("Deve lançar exceção quando SnsTemplate é nulo para AWS publisher")
    void deveLancarExcecaoQuandoSnsTemplateENuloParaAWSPublisher() {
        ObjectMapper objectMapper = new ObjectMapper();

        assertThrows(NullPointerException.class, () -> config.awsEventPublisher(null, objectMapper));
    }
}
