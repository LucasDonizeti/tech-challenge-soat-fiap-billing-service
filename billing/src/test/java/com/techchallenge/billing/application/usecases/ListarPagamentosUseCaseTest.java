package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ListarPagamentosCommand;
import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.application.responses.PagamentoResponse;
import com.techchallenge.billing.application.usecases.assemblers.PagamentoResponseAssembler;
import com.techchallenge.billing.domain.models.Pagamento;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ListarPagamentosUseCase")
class ListarPagamentosUseCaseTest {

    @Mock
    private PagamentoGateway pagamentoGateway;

    @Mock
    private PagamentoResponseAssembler responseAssembler;

    @Mock
    private Validator validator;

    @Mock
    private Pagamento pagamento;

    @Mock
    private PagamentoResponse response;

    private ListarPagamentosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListarPagamentosUseCase(pagamentoGateway, responseAssembler, validator);
    }

    @Test
    @DisplayName("Deve listar pagamentos com sucesso")
    void deveListarPagamentosComSucesso() {
        UUID osId = UUID.randomUUID();
        ListarPagamentosCommand command = new ListarPagamentosCommand(osId);

        when(pagamentoGateway.findByOsId(osId)).thenReturn(List.of(pagamento));
        when(responseAssembler.toResponse(pagamento)).thenReturn(response);

        List<PagamentoResponse> result = useCase.execute(command);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
        verify(pagamentoGateway).findByOsId(osId);
        verify(responseAssembler).toResponse(pagamento);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pagamentos")
    void deveRetornarListaVaziaQuandoNaoHaPagamentos() {
        UUID osId = UUID.randomUUID();
        ListarPagamentosCommand command = new ListarPagamentosCommand(osId);

        when(pagamentoGateway.findByOsId(osId)).thenReturn(List.of());

        List<PagamentoResponse> result = useCase.execute(command);

        assertTrue(result.isEmpty());
        verify(pagamentoGateway).findByOsId(osId);
        verify(responseAssembler, never()).toResponse(any());
    }
}
