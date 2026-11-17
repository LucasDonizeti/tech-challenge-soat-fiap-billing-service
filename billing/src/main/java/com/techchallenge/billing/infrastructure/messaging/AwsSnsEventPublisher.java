package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.domain.events.CobrancaPixGeradaEvent;
import com.techchallenge.billing.domain.events.PagamentoAprovadoEvent;
import com.techchallenge.billing.domain.events.PagamentoRecusadoEvent;
import io.awspring.cloud.sns.core.SnsTemplate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * AWS SNS Event Publisher Implementation - Fase A
 * 
 * Used for production environments with AWS infrastructure
 * Events are published to AWS SNS topics for cross-service communication
 * 
 * Environment: AWS
 */
@Slf4j
@RequiredArgsConstructor
public class AwsSnsEventPublisher implements BillingEventPublisher {

    private final SnsTemplate snsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.topics.cobranca-pix-gerada:}")
    private String cobrancaPixGeradaTopic;

    @Value("${aws.sns.topics.pagamento-aprovado:}")
    private String pagamentoAprovadoTopic;

    @Value("${aws.sns.topics.pagamento-recusado:}")
    private String pagamentoRecusadoTopic;

    @PostConstruct
    void validateTopics() {
        requireTopic(cobrancaPixGeradaTopic, "AWS_SNS_TOPIC_COBRANCA_PIX");
        requireTopic(pagamentoAprovadoTopic, "AWS_SNS_TOPIC_PAGAMENTO_APROVADO");
        requireTopic(pagamentoRecusadoTopic, "AWS_SNS_TOPIC_PAGAMENTO_RECUSADO");
    }

    private void requireTopic(String topicArn, String environmentVariable) {
        if (topicArn.isBlank()) {
            throw new IllegalStateException(
                "Missing required SNS topic ARN; configure " + environmentVariable
            );
        }
    }

    @Override
    public void publishCobrancaPixGerada(CobrancaPixGeradaEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("📤 [AWS - SNS] Publicando CobrancaPixGeradaEvent no tópico: {}", cobrancaPixGeradaTopic);
            log.debug("   Payload: {}", eventJson);

            snsTemplate.convertAndSend(cobrancaPixGeradaTopic, eventJson, getMessageAttributes(event));

            log.info("   ✅ Evento publicado com sucesso");
        } catch (Exception e) {
            log.error("❌ Erro ao publicar CobrancaPixGeradaEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar evento de cobrança PIX", e);
        }
    }

    @Override
    public void publishPagamentoAprovado(PagamentoAprovadoEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("📤 [AWS - SNS] Publicando PagamentoAprovadoEvent no tópico: {}", pagamentoAprovadoTopic);
            log.debug("   Payload: {}", eventJson);

            snsTemplate.convertAndSend(pagamentoAprovadoTopic, eventJson, getMessageAttributes(event));

            log.info("   ✅ Evento publicado com sucesso");
        } catch (Exception e) {
            log.error("❌ Erro ao publicar PagamentoAprovadoEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar evento de pagamento aprovado", e);
        }
    }

    @Override
    public void publishPagamentoRecusado(PagamentoRecusadoEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            log.info("📤 [AWS - SNS] Publicando PagamentoRecusadoEvent no tópico: {}", pagamentoRecusadoTopic);
            log.debug("   Payload: {}", eventJson);

            snsTemplate.convertAndSend(pagamentoRecusadoTopic, eventJson, getMessageAttributes(event));

            log.info("   ✅ Evento publicado com sucesso");
        } catch (Exception e) {
            log.error("❌ Erro ao publicar PagamentoRecusadoEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar evento de pagamento recusado", e);
        }
    }

    /**
     * Helper method to add message attributes (metadata) to SNS message
     */
    private java.util.Map<String, Object> getMessageAttributes(Object event) {
        var attributes = new java.util.HashMap<String, Object>();
        attributes.put("EventType", event.getClass().getSimpleName());
        attributes.put("Timestamp", System.currentTimeMillis());
        return attributes;
    }
}
