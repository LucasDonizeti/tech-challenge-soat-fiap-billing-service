package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ConfirmarPagamentoCommand;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.application.gateway.ports.output.MercadoPagoGateway;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.models.MercadoPagoPaymentResult;
import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import com.techchallenge.billing.domain.services.SagaPaymentService;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfirmarPagamentoUseCaseTest {

    private final PagamentoGateway pagamentoGateway = mock(PagamentoGateway.class);
    private final OrcamentoGateway orcamentoGateway = mock(OrcamentoGateway.class);
    private final MercadoPagoGateway mercadoPagoGateway = mock(MercadoPagoGateway.class);
    private final BillingEventPublisher eventPublisher = mock(BillingEventPublisher.class);
    private final SagaPaymentService sagaPaymentService = mock(SagaPaymentService.class);
    private ValidatorFactory validatorFactory;
    private ConfirmarPagamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        useCase = new ConfirmarPagamentoUseCase(
                pagamentoGateway,
                orcamentoGateway,
                mercadoPagoGateway,
                eventPublisher,
                sagaPaymentService,
                validatorFactory.getValidator()
        );
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void republishesApprovedEventWhenWebhookRetriesAnAlreadyApprovedPayment() {
        String mercadoPagoPaymentId = "ORD123";
        UUID osId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setOrcamentoId(orcamentoId);
        pagamento.setOsId(osId);
        pagamento.setMercadoPagoPaymentId(mercadoPagoPaymentId);
        pagamento.setStatus(StatusPagamento.APROVADO);

        Orcamento orcamento = new Orcamento();
        orcamento.setId(orcamentoId);
        orcamento.setOsId(osId);
        orcamento.setClienteId(clienteId);
        orcamento.setStatus(StatusOrcamento.PAGO);
        orcamento.setHistoricoStatusChanges(new ArrayList<>());

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "approved",
                        new BigDecimal("380.00"),
                        osId.toString(),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
        when(pagamentoGateway.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orcamentoGateway.findById(orcamentoId)).thenReturn(Optional.of(orcamento));

        useCase.execute(new ConfirmarPagamentoCommand(mercadoPagoPaymentId));

        verify(eventPublisher).publishPagamentoAprovado(any());
        verify(eventPublisher, never()).publishPagamentoRecusado(any());
    }
}
