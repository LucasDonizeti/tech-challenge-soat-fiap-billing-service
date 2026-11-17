package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.CriarOrcamentoInput;
import com.techchallenge.billing.domain.events.OrcamentoProntoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "messaging.type", havingValue = "rabbit")
public class RabbitOrcamentoProntoListener {

    private final ObjectMapper objectMapper;
    private final CriarOrcamentoInput criarOrcamentoInput;

    @RabbitListener(queues = "${messaging.rabbit.queues.orcamento-pronto}")
    public void receive(String body) {
        try {
            JsonNode payload = objectMapper.readTree(body);
            if (payload == null || !payload.isObject()) {
                throw new IllegalArgumentException("Mensagem RabbitMQ de orçamento deve ser um objeto JSON.");
            }
            var event = objectMapper.treeToValue(payload, OrcamentoProntoEvent.class);
            criarOrcamentoInput.execute(new CriarOrcamentoCommand(
                    event.ordemServicoId(),
                    event.clienteId(),
                    event.veiculoId(),
                    event.valorTotal(),
                    event.descricaoServicos(),
                    event.dataOrcamento(),
                    event.payerEmail(),
                    event.payerIdentificationType(),
                    event.payerIdentificationNumber()
            ));
            log.info("OrcamentoProntoEvent processado via RabbitMQ: ordemServicoId={}", event.ordemServicoId());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Mensagem inválida na fila RabbitMQ de orçamentos.", exception);
        }
    }
}
