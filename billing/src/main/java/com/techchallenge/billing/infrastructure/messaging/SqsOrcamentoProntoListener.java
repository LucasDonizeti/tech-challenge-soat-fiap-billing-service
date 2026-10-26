package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.CriarOrcamentoInput;
import com.techchallenge.billing.domain.events.OrcamentoProntoEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "messaging.type", havingValue = "aws")
public class SqsOrcamentoProntoListener {

    private final ObjectMapper objectMapper;
    private final CriarOrcamentoInput criarOrcamentoInput;

    @SqsListener("${aws.sqs.queues.orcamento-pronto}")
    public void receive(String body) {
        try {
            JsonNode message = objectMapper.readTree(body);
            if (message == null || !message.isObject()) {
                throw new IllegalArgumentException("Mensagem SQS de orçamento deve ser um objeto JSON.");
            }
            if (message.hasNonNull("Message")) {
                message = objectMapper.readTree(message.get("Message").asText());
            }
            if (message == null || !message.isObject()) {
                throw new IllegalArgumentException("Payload SNS de orçamento deve ser um objeto JSON.");
            }
            var event = objectMapper.treeToValue(message, OrcamentoProntoEvent.class);
            if (event == null) {
                throw new IllegalArgumentException("Payload não contém OrcamentoProntoEvent.");
            }
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
            log.info("OrcamentoProntoEvent processado para ordemServicoId={}", event.ordemServicoId());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Mensagem inválida na fila de orçamentos.", exception);
        }
    }
}
