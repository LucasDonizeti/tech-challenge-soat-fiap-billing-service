package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.domain.events.CobrancaPixGeradaEvent;
import com.techchallenge.billing.domain.events.PagamentoAprovadoEvent;
import com.techchallenge.billing.domain.events.PagamentoRecusadoEvent;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - AwsSnsEventPublisher")
class AwsSnsEventPublisherTest {

    @Mock
    private SnsTemplate snsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private AwsSnsEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new AwsSnsEventPublisher(snsTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "cobrancaPixGeradaTopic", "arn:aws:sns:us-east-1:123:cobranca-pix-gerada");
        ReflectionTestUtils.setField(publisher, "pagamentoAprovadoTopic", "arn:aws:sns:us-east-1:123:pagamento-aprovado");
        ReflectionTestUtils.setField(publisher, "pagamentoRecusadoTopic", "arn:aws:sns:us-east-1:123:pagamento-recusado");
    }



    @Test
    @DisplayName("Deve publicar CobrancaPixGeradaEvent")
    void devePublicarCobrancaPixGeradaEvent() throws Exception {
        CobrancaPixGeradaEvent event = new CobrancaPixGeradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pix-code",
                "base64",
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "ref-123"
        );

        when(objectMapper.writeValueAsString(event)).thenReturn("{}");

        assertDoesNotThrow(() -> publisher.publishCobrancaPixGerada(event));

        verify(snsTemplate).convertAndSend(eq("arn:aws:sns:us-east-1:123:cobranca-pix-gerada"), any(String.class), any(java.util.Map.class));
    }

    @Test
    @DisplayName("Deve publicar PagamentoAprovadoEvent")
    void devePublicarPagamentoAprovadoEvent() throws Exception {
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "mp-payment-123",
                new BigDecimal("100.00"),
                LocalDateTime.now()
        );

        when(objectMapper.writeValueAsString(event)).thenReturn("{}");

        assertDoesNotThrow(() -> publisher.publishPagamentoAprovado(event));

        verify(snsTemplate).convertAndSend(eq("arn:aws:sns:us-east-1:123:pagamento-aprovado"), any(String.class), any(java.util.Map.class));
    }

    @Test
    @DisplayName("Deve publicar PagamentoRecusadoEvent")
    void devePublicarPagamentoRecusadoEvent() throws Exception {
        PagamentoRecusadoEvent event = new PagamentoRecusadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Cartão inválido",
                "rejected"
        );

        when(objectMapper.writeValueAsString(event)).thenReturn("{}");

        assertDoesNotThrow(() -> publisher.publishPagamentoRecusado(event));

        verify(snsTemplate).convertAndSend(eq("arn:aws:sns:us-east-1:123:pagamento-recusado"), any(String.class), any(java.util.Map.class));
    }

    @Test
    @DisplayName("Deve implementar BillingEventPublisher")
    void deveImplementarBillingEventPublisher() {
        assertTrue(publisher instanceof BillingEventPublisher);
    }
}
