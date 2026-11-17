package com.techchallenge.billing.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.CriarOrcamentoInput;
import com.techchallenge.billing.domain.events.OrcamentoProntoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - SqsOrcamentoProntoListener")
class SqsOrcamentoProntoListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CriarOrcamentoInput criarOrcamentoInput;

    private SqsOrcamentoProntoListener listener;

    @BeforeEach
    void setUp() {
        listener = new SqsOrcamentoProntoListener(objectMapper, criarOrcamentoInput);
    }

    @Test
    @DisplayName("Deve processar mensagem JSON válida")
    void deveProcessarMensagemJSONValida() throws JsonProcessingException {
        String jsonBody = "{\"ordemServicoId\":\"uuid-1\",\"clienteId\":\"uuid-2\",\"veiculoId\":\"uuid-3\",\"valorTotal\":100.00,\"descricaoServicos\":\"Serviço\",\"dataOrcamento\":\"2024-01-15T10:00:00\",\"payerEmail\":\"email@test.com\",\"payerIdentificationType\":\"CPF\",\"payerIdentificationNumber\":\"12345678909\"}";
        JsonNode node = mock(JsonNode.class);
        when(node.isObject()).thenReturn(true);

        when(objectMapper.readTree(jsonBody)).thenReturn(node);
        when(objectMapper.treeToValue(eq(node), eq(OrcamentoProntoEvent.class)))
                .thenReturn(createOrcamentoProntoEvent());

        listener.receive(jsonBody);

        verify(criarOrcamentoInput).execute(any(CriarOrcamentoCommand.class));
    }

    @Test
    @DisplayName("Deve processar mensagem com envelope SNS")
    void deveProcessarMensagemComEnvelopeSNS() throws JsonProcessingException {
        String snsBody = "{\"Message\":\"{\\\"ordemServicoId\\\":\\\"uuid-1\\\"}\"}";
        JsonNode outerNode = mock(JsonNode.class);
        JsonNode messageNode = mock(JsonNode.class);
        JsonNode innerNode = mock(JsonNode.class);
        when(outerNode.isObject()).thenReturn(true);
        when(innerNode.isObject()).thenReturn(true);

        when(objectMapper.readTree(snsBody)).thenReturn(outerNode);
        when(outerNode.hasNonNull("Message")).thenReturn(true);
        when(outerNode.get("Message")).thenReturn(messageNode);
        when(messageNode.asText()).thenReturn("{\"ordemServicoId\":\"uuid-1\"}");
        when(objectMapper.readTree("{\"ordemServicoId\":\"uuid-1\"}")).thenReturn(innerNode);
        when(objectMapper.treeToValue(innerNode, OrcamentoProntoEvent.class)).thenReturn(createOrcamentoProntoEvent());

        listener.receive(snsBody);

        verify(criarOrcamentoInput).execute(any(CriarOrcamentoCommand.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando mensagem não é objeto JSON")
    void deveLancarExcecaoQuandoMensagemNaoEObjetoJSON() throws JsonProcessingException {
        String invalidBody = "not-a-json";
        JsonNode node = mock(JsonNode.class);
        when(node.isObject()).thenReturn(false);

        when(objectMapper.readTree(invalidBody)).thenReturn(node);

        assertThrows(IllegalArgumentException.class, () -> listener.receive(invalidBody));
        verify(criarOrcamentoInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando payload SNS não é objeto JSON")
    void deveLancarExcecaoQuandoPayloadSNSNaoEObjetoJSON() throws JsonProcessingException {
        String snsBody = "{\"Message\":\"invalid\"}";
        JsonNode outerNode = mock(JsonNode.class);
        JsonNode messageNode = mock(JsonNode.class);
        JsonNode innerNode = mock(JsonNode.class);
        when(outerNode.isObject()).thenReturn(true);
        when(innerNode.isObject()).thenReturn(false);

        when(objectMapper.readTree(snsBody)).thenReturn(outerNode);
        when(outerNode.hasNonNull("Message")).thenReturn(true);
        when(outerNode.get("Message")).thenReturn(messageNode);
        when(messageNode.asText()).thenReturn("invalid");
        when(objectMapper.readTree("invalid")).thenReturn(innerNode);

        assertThrows(IllegalArgumentException.class, () -> listener.receive(snsBody));
        verify(criarOrcamentoInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando event é nulo")
    void deveLancarExcecaoQuandoEventENulo() throws JsonProcessingException {
        String jsonBody = "{}";
        JsonNode node = mock(JsonNode.class);
        when(node.isObject()).thenReturn(true);

        when(objectMapper.readTree(jsonBody)).thenReturn(node);
        when(objectMapper.treeToValue(node, OrcamentoProntoEvent.class)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> listener.receive(jsonBody));
        verify(criarOrcamentoInput, never()).execute(any());
    }

    @Test
    @DisplayName("Deve passar campos corretos para CriarOrcamentoCommand")
    void devePassarCamposCorretosParaCriarOrcamentoCommand() throws JsonProcessingException {
        OrcamentoProntoEvent event = createOrcamentoProntoEvent();
        String jsonBody = "{}";
        JsonNode node = mock(JsonNode.class);
        when(node.isObject()).thenReturn(true);

        when(objectMapper.readTree(jsonBody)).thenReturn(node);
        when(objectMapper.treeToValue(node, OrcamentoProntoEvent.class)).thenReturn(event);

        listener.receive(jsonBody);

        ArgumentCaptor<CriarOrcamentoCommand> commandCaptor = ArgumentCaptor.forClass(CriarOrcamentoCommand.class);
        verify(criarOrcamentoInput).execute(commandCaptor.capture());

        CriarOrcamentoCommand command = commandCaptor.getValue();
        assertEquals(event.ordemServicoId(), command.ordemServicoId());
        assertEquals(event.clienteId(), command.clienteId());
        assertEquals(event.veiculoId(), command.veiculoId());
        assertEquals(event.valorTotal(), command.valorTotal());
        assertEquals(event.descricaoServicos(), command.descricaoServicos());
        assertEquals(event.dataOrcamento(), command.dataOrcamento());
        assertEquals(event.payerEmail(), command.payerEmail());
        assertEquals(event.payerIdentificationType(), command.payerIdentificationType());
        assertEquals(event.payerIdentificationNumber(), command.payerIdentificationNumber());
    }

    private OrcamentoProntoEvent createOrcamentoProntoEvent() {
        return new OrcamentoProntoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Serviço",
                LocalDateTime.now(),
                "email@test.com",
                "CPF",
                "12345678909"
        );
    }
}
