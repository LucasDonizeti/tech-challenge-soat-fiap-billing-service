package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.application.gateway.ports.output.MercadoPagoGateway;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.models.CreatePixPaymentCommand;
import com.techchallenge.billing.application.gateway.ports.output.models.MercadoPagoPaymentResult;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.application.usecases.assemblers.OrcamentoResponseAssembler;
import com.techchallenge.billing.domain.events.CobrancaPixGeradaEvent;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import com.techchallenge.billing.domain.services.SagaPaymentService;
import com.techchallenge.billing.infrastructure.external.OrdemServicoClient;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarOrcamentoUseCaseTest {

    @Mock
    private OrcamentoGateway orcamentoGateway;

    @Mock
    private PagamentoGateway pagamentoGateway;

    @Mock
    private MercadoPagoGateway mercadoPagoGateway;

    @Mock
    private BillingEventPublisher eventPublisher;

    @Mock
    private SagaPaymentService sagaPaymentService;

    @Mock
    private Validator validator;

    @Mock
    private OrcamentoResponseAssembler responseAssembler;

    @Mock
    private OrdemServicoClient ordemServicoClient;

    private CriarOrcamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CriarOrcamentoUseCase(
                orcamentoGateway,
                pagamentoGateway,
                mercadoPagoGateway,
                eventPublisher,
                sagaPaymentService,
                validator,
                responseAssembler,
                ordemServicoClient
        );
        ReflectionTestUtils.setField(useCase, "notificationUrl", "http://localhost:8080/webhook");
        ReflectionTestUtils.setField(useCase, "pixExpiresInMinutes", 30L);
    }

    @Test
    void createsNewOrcamentoAndPixPaymentSuccessfully() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String mpPaymentId = "ORD123";
        String qrCode = "qr-code-data";
        String qrCodeBase64 = "base64-data";
        BigDecimal valorTotal = new BigDecimal("500.00");

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                osId,
                clienteId,
                UUID.randomUUID(),
                valorTotal,
                "Serviço de manutenção",
                null,
                "test@example.com",
                "CPF",
                "12345678900"
        );

        when(sagaPaymentService.hasPendingPixForOrdemServico(osId)).thenReturn(false);
        when(orcamentoGateway.findByOsId(osId)).thenReturn(Optional.empty());
        List<UUID> clienteIdsPersistidos = new ArrayList<>();
        when(orcamentoGateway.save(any(Orcamento.class))).thenAnswer(invocation -> {
            Orcamento orc = invocation.getArgument(0);
            clienteIdsPersistidos.add(orc.getClienteId());
            when(orcamentoGateway.findById(orc.getId())).thenReturn(Optional.of(orc));
            return orc;
        });

        OrdemServicoClient.OrdemServicoData osData = new OrdemServicoClient.OrdemServicoData(
                osId,
                clienteId,
                valorTotal,
                "Serviço de manutenção",
                "test@example.com",
                "CPF",
                "12345678900"
        );
        when(ordemServicoClient.getOrdemServico(osId)).thenReturn(osData);

        MercadoPagoPaymentResult paymentResult = new MercadoPagoPaymentResult(
                mpPaymentId,
                "pending",
                valorTotal,
                osId.toString(),
                null,
                null,
                null,
                qrCode,
                qrCodeBase64
        );
        when(mercadoPagoGateway.createPixPayment(any(CreatePixPaymentCommand.class))).thenReturn(paymentResult);
        when(pagamentoGateway.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrcamentoResponse response = new OrcamentoResponse(
                UUID.randomUUID(),
                osId,
                clienteId,
                valorTotal,
                StatusOrcamento.AGUARDANDO_PAGAMENTO.name(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(responseAssembler.toResponse(any(Orcamento.class))).thenReturn(response);

        var result = useCase.execute(command);

        assertNotNull(result);
        verify(orcamentoGateway, times(3)).save(any(Orcamento.class));
        assertEquals(clienteId, clienteIdsPersistidos.get(0));
        verify(mercadoPagoGateway).createPixPayment(any(CreatePixPaymentCommand.class));
        verify(pagamentoGateway).save(any(Pagamento.class));
        verify(sagaPaymentService).initiateSaga(
                any(UUID.class),
                any(UUID.class),
                eq(osId),
                eq(clienteId),
                eq(mpPaymentId),
                eq(qrCode),
                eq(qrCodeBase64),
                eq(valorTotal),
                any(LocalDateTime.class)
        );
        verify(eventPublisher).publishCobrancaPixGerada(any(CobrancaPixGeradaEvent.class));
    }

    @Test
    void returnsExistingOrcamentoWhenPendingPixExists() {
        UUID osId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        Orcamento existingOrcamento = new Orcamento();
        existingOrcamento.setId(orcamentoId);
        existingOrcamento.setOsId(osId);

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                osId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                "Serviço",
                null,
                "test@example.com",
                "CPF",
                "12345678900"
        );

        when(sagaPaymentService.hasPendingPixForOrdemServico(osId)).thenReturn(true);
        when(orcamentoGateway.findByOsId(osId)).thenReturn(Optional.of(existingOrcamento));

        OrcamentoResponse response = new OrcamentoResponse(
                orcamentoId,
                osId,
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                StatusOrcamento.AGUARDANDO_PAGAMENTO.name(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(responseAssembler.toResponse(existingOrcamento)).thenReturn(response);

        var result = useCase.execute(command);

        assertNotNull(result);
        verify(mercadoPagoGateway, never()).createPixPayment(any());
        verify(pagamentoGateway, never()).save(any());
    }

    @Test
    void throwsExceptionWhenExistingOrcamentoNotFoundForDuplicatePix() {
        UUID osId = UUID.randomUUID();

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                osId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                "Serviço",
                null,
                "test@example.com",
                "CPF",
                "12345678900"
        );

        when(sagaPaymentService.hasPendingPixForOrdemServico(osId)).thenReturn(true);
        when(orcamentoGateway.findByOsId(osId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
    }

    @Test
    void reusesExistingOrcamentoWithPayment() {
        UUID osId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        Orcamento existingOrcamento = new Orcamento();
        existingOrcamento.setId(orcamentoId);
        existingOrcamento.setOsId(osId);
        existingOrcamento.setStatus(StatusOrcamento.GERADO);
        existingOrcamento.setHistoricoStatusChanges(new ArrayList<>());

        Pagamento existingPayment = new Pagamento();
        existingPayment.setId(pagamentoId);
        existingPayment.setOrcamentoId(orcamentoId);
        existingPayment.setOsId(osId);
        existingPayment.setQrCodePix("qr-code");
        existingPayment.setQrCodeBase64("base64");
        existingPayment.setValorCobrado(new BigDecimal("500.00"));
        existingPayment.setDataExpiracao(LocalDateTime.now().plusMinutes(30));

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                osId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                "Serviço",
                null,
                "test@example.com",
                "CPF",
                "12345678900"
        );

        when(sagaPaymentService.hasPendingPixForOrdemServico(osId)).thenReturn(false);
        when(orcamentoGateway.findByOsId(osId)).thenReturn(Optional.of(existingOrcamento));
        when(orcamentoGateway.findById(orcamentoId)).thenReturn(Optional.of(existingOrcamento));
        when(pagamentoGateway.findByOsId(osId)).thenReturn(List.of(existingPayment));
        when(orcamentoGateway.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrcamentoResponse response = new OrcamentoResponse(
                orcamentoId,
                osId,
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                StatusOrcamento.AGUARDANDO_PAGAMENTO.name(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(responseAssembler.toResponse(any(Orcamento.class))).thenReturn(response);

        var result = useCase.execute(command);

        assertNotNull(result);
        verify(mercadoPagoGateway, never()).createPixPayment(any());
        verify(eventPublisher).publishCobrancaPixGerada(any(CobrancaPixGeradaEvent.class));
    }

    @Test
    void throwsExceptionWhenNotificationUrlNotConfigured() {
        ReflectionTestUtils.setField(useCase, "notificationUrl", "");

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                "Serviço",
                null,
                "test@example.com",
                "CPF",
                "12345678900"
        );

        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
    }

    @Test
    void throwsExceptionWhenPixExpiresInMinutesIsInvalid() {
        ReflectionTestUtils.setField(useCase, "pixExpiresInMinutes", 0L);

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                "Serviço",
                null,
                "test@example.com",
                "CPF",
                "12345678900"
        );

        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
    }

    @Test
    void throwsExceptionWhenMercadoPagoReturnsNullPaymentId() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        BigDecimal valorTotal = new BigDecimal("500.00");

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                osId,
                clienteId,
                UUID.randomUUID(),
                valorTotal,
                "Serviço",
                null,
                "test@example.com",
                "CPF",
                "12345678900"
        );

        when(sagaPaymentService.hasPendingPixForOrdemServico(osId)).thenReturn(false);
        when(orcamentoGateway.findByOsId(osId)).thenReturn(Optional.empty());
        when(orcamentoGateway.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoClient.OrdemServicoData osData = new OrdemServicoClient.OrdemServicoData(
                osId,
                clienteId,
                valorTotal,
                "Serviço",
                "test@example.com",
                "CPF",
                "12345678900"
        );
        when(ordemServicoClient.getOrdemServico(osId)).thenReturn(osData);

        MercadoPagoPaymentResult paymentResult = new MercadoPagoPaymentResult(
                null,
                "pending",
                valorTotal,
                osId.toString(),
                null,
                null,
                null,
                null,
                null
        );
        when(mercadoPagoGateway.createPixPayment(any(CreatePixPaymentCommand.class))).thenReturn(paymentResult);

        assertThrows(IntegracaoMercadoPagoException.class, () -> useCase.execute(command));
    }

    @Test
    void updatesOrcamentoWithOsDataFromService() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        BigDecimal valorTotal = new BigDecimal("500.00");

        CriarOrcamentoCommand command = new CriarOrcamentoCommand(
                osId,
                clienteId,
                UUID.randomUUID(),
                valorTotal,
                "Serviço",
                null,
                "test@example.com",
                "CPF",
                "12345678900"
        );

        when(sagaPaymentService.hasPendingPixForOrdemServico(osId)).thenReturn(false);
        when(orcamentoGateway.findByOsId(osId)).thenReturn(Optional.empty());
        when(orcamentoGateway.save(any(Orcamento.class))).thenAnswer(invocation -> {
            Orcamento orc = invocation.getArgument(0);
            when(orcamentoGateway.findById(orc.getId())).thenReturn(Optional.of(orc));
            return orc;
        });

        OrdemServicoClient.OrdemServicoData osData = new OrdemServicoClient.OrdemServicoData(
                osId,
                clienteId,
                valorTotal,
                "Serviço de manutenção completa",
                "test@example.com",
                "CPF",
                "12345678900"
        );
        when(ordemServicoClient.getOrdemServico(osId)).thenReturn(osData);

        MercadoPagoPaymentResult paymentResult = new MercadoPagoPaymentResult(
                "ORD123",
                "pending",
                valorTotal,
                osId.toString(),
                null,
                null,
                null,
                "qr-code",
                "base64"
        );
        when(mercadoPagoGateway.createPixPayment(any(CreatePixPaymentCommand.class))).thenReturn(paymentResult);
        when(pagamentoGateway.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrcamentoResponse response = new OrcamentoResponse(
                UUID.randomUUID(),
                osId,
                clienteId,
                valorTotal,
                StatusOrcamento.AGUARDANDO_PAGAMENTO.name(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(responseAssembler.toResponse(any(Orcamento.class))).thenReturn(response);

        useCase.execute(command);

        verify(orcamentoGateway, times(3)).save(argThat(orcamento -> {
            return clienteId.equals(orcamento.getClienteId()) &&
                   valorTotal.equals(orcamento.getValorTotal()) &&
                   "Serviço de manutenção completa".equals(orcamento.getDescricaoServicos());
        }));
    }
}
