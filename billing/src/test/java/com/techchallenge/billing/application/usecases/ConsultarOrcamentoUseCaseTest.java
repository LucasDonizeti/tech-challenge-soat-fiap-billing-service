package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ConsultarOrcamentoCommand;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.application.usecases.assemblers.OrcamentoResponseAssembler;
import com.techchallenge.billing.domain.exceptions.OrcamentoNaoEncontradoException;
import com.techchallenge.billing.domain.models.Orcamento;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ConsultarOrcamentoUseCase")
class ConsultarOrcamentoUseCaseTest {

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

    private ConsultarOrcamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarOrcamentoUseCase(orcamentoGateway, responseAssembler, validator);
    }

    @Test
    @DisplayName("Deve consultar orçamento com sucesso")
    void deveConsultarOrcamentoComSucesso() {
        UUID orcamentoId = UUID.randomUUID();
        ConsultarOrcamentoCommand command = new ConsultarOrcamentoCommand(orcamentoId);

        when(orcamentoGateway.findById(orcamentoId)).thenReturn(Optional.of(orcamento));
        when(responseAssembler.toResponse(orcamento)).thenReturn(response);

        OrcamentoResponse result = useCase.execute(command);

        assertEquals(response, result);
        verify(orcamentoGateway).findById(orcamentoId);
        verify(responseAssembler).toResponse(orcamento);
    }

    @Test
    @DisplayName("Deve lançar exceção quando orçamento não encontrado")
    void deveLancarExcecaoQuandoOrcamentoNaoEncontrado() {
        UUID orcamentoId = UUID.randomUUID();
        ConsultarOrcamentoCommand command = new ConsultarOrcamentoCommand(orcamentoId);

        when(orcamentoGateway.findById(orcamentoId)).thenReturn(Optional.empty());

        assertThrows(OrcamentoNaoEncontradoException.class, () -> useCase.execute(command));
        verify(orcamentoGateway).findById(orcamentoId);
        verify(responseAssembler, never()).toResponse(any());
    }
}
