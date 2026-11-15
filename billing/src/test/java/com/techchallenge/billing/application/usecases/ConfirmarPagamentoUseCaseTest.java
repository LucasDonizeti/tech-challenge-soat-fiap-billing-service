package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ConfirmarPagamentoCommand;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.application.gateway.ports.output.MercadoPagoGateway;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.models.MercadoPagoPaymentResult;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import com.techchallenge.billing.domain.services.SagaPaymentService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmarPagamentoUseCaseTest {

    @Mock
    private PagamentoGateway pagamentoGateway;

    @Mock
    private OrcamentoGateway orcamentoGateway;

    @Mock
    private MercadoPagoGateway mercadoPagoGateway;

    @Mock
    private BillingEventPublisher eventPublisher;

    @Mock
    private SagaPaymentService sagaPaymentService;

    @Mock
    private Validator validator;

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
                validator
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

    @Test
    void confirmsApprovedPaymentSuccessfully() {
        String mercadoPagoPaymentId = "ORD123";
        UUID osId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setOrcamentoId(orcamentoId);
        pagamento.setOsId(osId);
        pagamento.setMercadoPagoPaymentId(mercadoPagoPaymentId);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        Orcamento orcamento = new Orcamento();
        orcamento.setId(orcamentoId);
        orcamento.setOsId(osId);
        orcamento.setClienteId(clienteId);
        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
        orcamento.setHistoricoStatusChanges(new ArrayList<>());

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "approved",
                        new BigDecimal("380.00"),
                        osId.toString(),
                        now,
                        null,
                        null,
                        null,
                        null
                )
        );
        when(pagamentoGateway.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orcamentoGateway.findById(orcamentoId)).thenReturn(Optional.of(orcamento));

        useCase.execute(new ConfirmarPagamentoCommand(mercadoPagoPaymentId));

        assertEquals(StatusPagamento.APROVADO, pagamento.getStatus());
        assertEquals(new BigDecimal("380.00"), pagamento.getValorPago());
        assertEquals(StatusOrcamento.PAGO, orcamento.getStatus());
        verify(sagaPaymentService).confirmSagaPayment(
                eq(pagamentoId),
                eq("APROVADO"),
                any(BigDecimal.class),
                any(LocalDateTime.class),
                anyString()
        );
        verify(eventPublisher).publishPagamentoAprovado(any());
    }

    @Test
    void confirmsRejectedPaymentSuccessfully() {
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
        pagamento.setStatus(StatusPagamento.PENDENTE);

        Orcamento orcamento = new Orcamento();
        orcamento.setId(orcamentoId);
        orcamento.setOsId(osId);
        orcamento.setClienteId(clienteId);
        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
        orcamento.setHistoricoStatusChanges(new ArrayList<>());

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "rejected",
                        new BigDecimal("380.00"),
                        osId.toString(),
                        null,
                        "rejected_by_bank",
                        "Payment rejected",
                        null,
                        null
                )
        );
        when(pagamentoGateway.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orcamentoGateway.findById(orcamentoId)).thenReturn(Optional.of(orcamento));

        useCase.execute(new ConfirmarPagamentoCommand(mercadoPagoPaymentId));

        assertEquals(StatusPagamento.RECUSADO, pagamento.getStatus());
        assertEquals(StatusOrcamento.RECUSADO, orcamento.getStatus());
        verify(sagaPaymentService).confirmSagaPayment(
                eq(pagamentoId),
                eq("RECUSADO"),
                any(BigDecimal.class),
                any(LocalDateTime.class),
                anyString()
        );
        verify(eventPublisher).publishPagamentoRecusado(any());
    }

    @Test
    void throwsExceptionWhenLocalPaymentNotFound() {
        String mercadoPagoPaymentId = "ORD123";

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.empty());

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                useCase.execute(new ConfirmarPagamentoCommand(mercadoPagoPaymentId))
        );
    }

    @Test
    void throwsExceptionWhenExternalReferenceDoesNotMatch() {
        String mercadoPagoPaymentId = "ORD123";
        UUID osId = UUID.randomUUID();
        UUID differentOsId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();

        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setOrcamentoId(orcamentoId);
        pagamento.setOsId(osId);
        pagamento.setMercadoPagoPaymentId(mercadoPagoPaymentId);

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "approved",
                        new BigDecimal("380.00"),
                        differentOsId.toString(),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                useCase.execute(new ConfirmarPagamentoCommand(mercadoPagoPaymentId))
        );
    }

    @Test
    void throwsExceptionWhenOrcamentoNotFound() {
        String mercadoPagoPaymentId = "ORD123";
        UUID osId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();

        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setOrcamentoId(orcamentoId);
        pagamento.setOsId(osId);
        pagamento.setMercadoPagoPaymentId(mercadoPagoPaymentId);

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
        when(orcamentoGateway.findById(orcamentoId)).thenReturn(Optional.empty());

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                useCase.execute(new ConfirmarPagamentoCommand(mercadoPagoPaymentId))
        );
    }

    @Test
    void throwsExceptionWhenMercadoPagoStatusIsBlank() {
        String mercadoPagoPaymentId = "ORD123";
        UUID osId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();

        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setOrcamentoId(orcamentoId);
        pagamento.setOsId(osId);
        pagamento.setMercadoPagoPaymentId(mercadoPagoPaymentId);

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "",
                        new BigDecimal("380.00"),
                        osId.toString(),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                useCase.execute(new ConfirmarPagamentoCommand(mercadoPagoPaymentId))
        );
    }

    @Test
    void throwsExceptionWhenMercadoPagoStatusIsUnknown() {
        String mercadoPagoPaymentId = "ORD123";
        UUID osId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();

        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setOrcamentoId(orcamentoId);
        pagamento.setOsId(osId);
        pagamento.setMercadoPagoPaymentId(mercadoPagoPaymentId);

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "unknown_status",
                        new BigDecimal("380.00"),
                        osId.toString(),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                useCase.execute(new ConfirmarPagamentoCommand(mercadoPagoPaymentId))
        );
    }

    @Test
    void confirmsCanceledPaymentSuccessfully() {
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
        pagamento.setStatus(StatusPagamento.PENDENTE);

        Orcamento orcamento = new Orcamento();
        orcamento.setId(orcamentoId);
        orcamento.setOsId(osId);
        orcamento.setClienteId(clienteId);
        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
        orcamento.setHistoricoStatusChanges(new ArrayList<>());

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "cancelled",
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

        assertEquals(StatusPagamento.CANCELADO, pagamento.getStatus());
        assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
        verify(sagaPaymentService).confirmSagaPayment(
                eq(pagamentoId),
                eq("CANCELADO"),
                any(BigDecimal.class),
                any(LocalDateTime.class),
                anyString()
        );
    }

    @Test
    void confirmsExpiredPaymentSuccessfully() {
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
        pagamento.setStatus(StatusPagamento.PENDENTE);

        Orcamento orcamento = new Orcamento();
        orcamento.setId(orcamentoId);
        orcamento.setOsId(osId);
        orcamento.setClienteId(clienteId);
        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
        orcamento.setHistoricoStatusChanges(new ArrayList<>());

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "expired",
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

        assertEquals(StatusPagamento.EXPIRADO, pagamento.getStatus());
        assertEquals(StatusOrcamento.RECUSADO, orcamento.getStatus());
        verify(sagaPaymentService).confirmSagaPayment(
                eq(pagamentoId),
                eq("EXPIRADO"),
                any(BigDecimal.class),
                any(LocalDateTime.class),
                anyString()
        );
    }

    @Test
    void doesNotUpdateOrcamentoStatusWhenPaymentIsPending() {
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
        pagamento.setStatus(StatusPagamento.PENDENTE);

        Orcamento orcamento = new Orcamento();
        orcamento.setId(orcamentoId);
        orcamento.setOsId(osId);
        orcamento.setClienteId(clienteId);
        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
        orcamento.setHistoricoStatusChanges(new ArrayList<>());

        when(pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId))
                .thenReturn(Optional.of(pagamento));
        when(mercadoPagoGateway.getPayment(mercadoPagoPaymentId)).thenReturn(
                new MercadoPagoPaymentResult(
                        mercadoPagoPaymentId,
                        "pending",
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

        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatus());
        assertEquals(StatusOrcamento.AGUARDANDO_PAGAMENTO, orcamento.getStatus());
        verify(eventPublisher, never()).publishPagamentoAprovado(any());
        verify(eventPublisher, never()).publishPagamentoRecusado(any());
    }

    @Test
    void incrementsSyncAttemptsCounter() {
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
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setTentativasSincronizacao(2);

        Orcamento orcamento = new Orcamento();
        orcamento.setId(orcamentoId);
        orcamento.setOsId(osId);
        orcamento.setClienteId(clienteId);
        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
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

        assertEquals(3, pagamento.getTentativasSincronizacao());
        assertNotNull(pagamento.getUltimaSincronizacaoMercadoPago());
    }
}
