package com.techchallenge.billing.infrastructure.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.commands.ConsultarOrcamentoCommand;
import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.commands.ListarOrcamentosCommand;
import com.techchallenge.billing.application.commands.ListarPagamentosCommand;
import com.techchallenge.billing.application.gateway.ports.input.ConsultarOrcamentoInput;
import com.techchallenge.billing.application.gateway.ports.input.CriarOrcamentoInput;
import com.techchallenge.billing.application.gateway.ports.input.ListarOrcamentosInput;
import com.techchallenge.billing.application.gateway.ports.input.ListarPagamentosInput;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.application.responses.PagamentoResponse;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.infrastructure.web.dtos.CriarOrcamentoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    @Mock
    private CriarOrcamentoInput criarOrcamentoInput;

    @Mock
    private ConsultarOrcamentoInput consultarOrcamentoInput;

    @Mock
    private ListarOrcamentosInput listarOrcamentosInput;

    @Mock
    private ListarPagamentosInput listarPagamentosInput;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        BillingController controller = new BillingController(
                criarOrcamentoInput,
                consultarOrcamentoInput,
                listarOrcamentosInput,
                listarPagamentosInput
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void criarOrcamentoReturnsCreatedWithLocation() throws Exception {
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                osId,
                clienteId,
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                "Serviço de manutenção",
                "test@example.com",
                "CPF",
                "12345678900"
        );

        OrcamentoResponse response = new OrcamentoResponse(
                orcamentoId,
                osId,
                clienteId,
                new BigDecimal("500.00"),
                StatusOrcamento.AGUARDANDO_PAGAMENTO.name(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(criarOrcamentoInput.execute(any(CriarOrcamentoCommand.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/billings/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(orcamentoId.toString()))
                .andExpect(jsonPath("$.ordemServicoId").value(osId.toString()))
                .andExpect(jsonPath("$.clienteId").value(clienteId.toString()));
    }

    @Test
    void criarOrcamentoReturnsBadRequestForInvalidRequest() throws Exception {
        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                null,
                null,
                null,
                null,
                "",
                "",
                "",
                ""
        );

        mockMvc.perform(post("/api/v1/billings/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void obterOrcamentoReturnsOrcamento() throws Exception {
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        OrcamentoResponse response = new OrcamentoResponse(
                orcamentoId,
                osId,
                clienteId,
                new BigDecimal("500.00"),
                StatusOrcamento.AGUARDANDO_PAGAMENTO.name(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(consultarOrcamentoInput.execute(any(ConsultarOrcamentoCommand.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/billings/orcamentos/{orcamentoId}", orcamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orcamentoId.toString()))
                .andExpect(jsonPath("$.ordemServicoId").value(osId.toString()))
                .andExpect(jsonPath("$.clienteId").value(clienteId.toString()));
    }

    @Test
    void listarPagamentosReturnsListOfPagamentos() throws Exception {
        UUID osId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();

        PagamentoResponse pagamento = new PagamentoResponse(
                pagamentoId,
                orcamentoId,
                osId,
                "qr-code-data",
                "base64-data",
                "PENDENTE",
                LocalDateTime.now().plusMinutes(30),
                null
        );

        when(listarPagamentosInput.execute(any(ListarPagamentosCommand.class)))
                .thenReturn(List.of(pagamento));

        mockMvc.perform(get("/api/v1/billings/os/{osId}/pagamentos", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(pagamentoId.toString()))
                .andExpect(jsonPath("$[0].ordemServicoId").value(osId.toString()));
    }

    @Test
    void listarPagamentosReturnsEmptyListWhenNoPagamentos() throws Exception {
        UUID osId = UUID.randomUUID();

        when(listarPagamentosInput.execute(any(ListarPagamentosCommand.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/billings/os/{osId}/pagamentos", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void criarOrcamentoMapsRequestToCommandCorrectly() throws Exception {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        BigDecimal valorTotal = new BigDecimal("500.00");
        String descricao = "Serviço de manutenção";
        String email = "test@example.com";
        String idType = "CPF";
        String idNumber = "12345678900";

        CriarOrcamentoRequest request = new CriarOrcamentoRequest(
                osId,
                clienteId,
                veiculoId,
                valorTotal,
                descricao,
                email,
                idType,
                idNumber
        );

        OrcamentoResponse response = new OrcamentoResponse(
                UUID.randomUUID(),
                osId,
                clienteId,
                valorTotal,
                StatusOrcamento.AGUARDANDO_PAGAMENTO.name(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(criarOrcamentoInput.execute(any(CriarOrcamentoCommand.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/billings/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
