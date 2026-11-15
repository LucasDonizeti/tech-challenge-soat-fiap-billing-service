package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.CriarOrcamentoInput;
import com.techchallenge.billing.domain.events.OrcamentoProntoEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitOrcamentoProntoListenerTest {

    @Mock
    private CriarOrcamentoInput criarOrcamentoInput;

    @Test
    void convertsRabbitMessageToCreateQuoteCommand() throws Exception {
        var mapper = new ObjectMapper().findAndRegisterModules();
        var listener = new RabbitOrcamentoProntoListener(mapper, criarOrcamentoInput);
        var event = new OrcamentoProntoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("25.50"),
                "Reparo",
                LocalDateTime.now(),
                "customer@example.com",
                "CPF",
                "12345678901"
        );

        listener.receive(mapper.writeValueAsString(event));

        var command = ArgumentCaptor.forClass(CriarOrcamentoCommand.class);
        verify(criarOrcamentoInput).execute(command.capture());
        assertEquals(event.ordemServicoId(), command.getValue().ordemServicoId());
        assertEquals(event.payerEmail(), command.getValue().payerEmail());
        assertEquals(0, event.valorTotal().compareTo(command.getValue().valorTotal()));
    }
}
