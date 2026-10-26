package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ListarOrcamentosCommand;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.application.usecases.assemblers.OrcamentoResponseAssembler;
import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ListarOrcamentosUseCase")
class ListarOrcamentosUseCaseTest {

    @Mock
    private OrcamentoGateway orcamentoGateway;

    @Mock
    private OrcamentoResponseAssembler responseAssembler;

    @Mock
    private Validator validator;

    @Mock
    private Orcamento orcamento;

    @Mock
    private OrcamentoResponse response;

    private ListarOrcamentosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListarOrcamentosUseCase(orcamentoGateway, responseAssembler, validator);
    }

    @Test
    @DisplayName("Deve listar orçamentos com sucesso")
    void deveListarOrcamentosComSucesso() {
        UUID clienteId = UUID.randomUUID();
        StatusOrcamento status = StatusOrcamento.PAGO;
        Pageable pageable = PageRequest.of(0, 10);
        ListarOrcamentosCommand command = new ListarOrcamentosCommand(clienteId, status, pageable);

        Page<Orcamento> orcamentoPage = new PageImpl<>(List.of(orcamento));
        Page<OrcamentoResponse> responsePage = new PageImpl<>(List.of(response));

        when(orcamentoGateway.findPageByClienteIdAndStatus(clienteId, status, pageable)).thenReturn(orcamentoPage);
        when(responseAssembler.toResponse(orcamento)).thenReturn(response);

        Page<OrcamentoResponse> result = useCase.execute(command);

        assertEquals(1, result.getTotalElements());
        assertEquals(response, result.getContent().get(0));
        verify(orcamentoGateway).findPageByClienteIdAndStatus(clienteId, status, pageable);
        verify(responseAssembler).toResponse(orcamento);
    }

    @Test
    @DisplayName("Deve listar orçamentos com status nulo")
    void deveListarOrcamentosComStatusNulo() {
        UUID clienteId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        ListarOrcamentosCommand command = new ListarOrcamentosCommand(clienteId, null, pageable);

        Page<Orcamento> orcamentoPage = new PageImpl<>(List.of(orcamento));
        Page<OrcamentoResponse> responsePage = new PageImpl<>(List.of(response));

        when(orcamentoGateway.findPageByClienteIdAndStatus(clienteId, null, pageable)).thenReturn(orcamentoPage);
        when(responseAssembler.toResponse(orcamento)).thenReturn(response);

        Page<OrcamentoResponse> result = useCase.execute(command);

        assertEquals(1, result.getTotalElements());
        verify(orcamentoGateway).findPageByClienteIdAndStatus(clienteId, null, pageable);
        verify(responseAssembler).toResponse(orcamento);
    }
}
