package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.CriarOrcamentoInput;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.application.gateway.ports.output.MercadoPagoGateway;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.models.CreatePixPaymentCommand;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.application.usecases.assemblers.OrcamentoResponseAssembler;
import com.techchallenge.billing.domain.events.CobrancaPixGeradaEvent;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamentoChange;
import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import com.techchallenge.billing.domain.services.SagaPaymentService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriarOrcamentoUseCase implements CriarOrcamentoInput {

    private final OrcamentoGateway orcamentoGateway;
    private final PagamentoGateway pagamentoGateway;
    private final MercadoPagoGateway mercadoPagoGateway;
    private final BillingEventPublisher eventPublisher;
    private final SagaPaymentService sagaPaymentService;
    private final Validator validator;
    private final OrcamentoResponseAssembler responseAssembler;
    private final com.techchallenge.billing.infrastructure.external.OrdemServicoClient ordemServicoClient;

    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    @Value("${billing.pix.expires-in-minutes:30}")
    private long pixExpiresInMinutes;

    @Override
    @Transactional
    public OrcamentoResponse execute(CriarOrcamentoCommand command) {
        validate(command);
        LocalDateTime now = LocalDateTime.now();

        // VALIDAÇÃO: Verificar se já existe PIX pendente para esta ordem
        // (Saga Pattern: evitar criação de múltiplos PIXs)
        if (sagaPaymentService.hasPendingPixForOrdemServico(command.ordemServicoId())) {
            log.warn("Tentativa de criar PIX duplicado para osId={}. Retornando PIX existente.",
                    command.ordemServicoId());
            var existingOrcamento = orcamentoGateway.findByOsId(command.ordemServicoId())
                    .orElseThrow(() -> new IllegalStateException("Orçamento não encontrado para OS duplicada"));
            return responseAssembler.toResponse(existingOrcamento);
        }

        var existingOrcamento = orcamentoGateway.findByOsId(command.ordemServicoId());
        Orcamento orcamento;
        if (existingOrcamento.isPresent()) {
            orcamento = existingOrcamento.get();
            UUID existingOrcamentoId = orcamento.getId();
            var existingPayment = pagamentoGateway.findByOsId(command.ordemServicoId()).stream()
                    .filter(pagamento -> existingOrcamentoId.equals(pagamento.getOrcamentoId()))
                    .findFirst();
            if (existingPayment.isPresent()) {
                orcamento = ensureAwaitingPaymentStatus(orcamento);
                publishPixEvent(existingPayment.get());
                return responseAssembler.toResponse(orcamento);
            }
        } else {
            orcamento = new Orcamento();
            orcamento.setId(UUID.randomUUID());
            orcamento.setOsId(command.ordemServicoId());
            orcamento.setClienteId(command.clienteId());
            orcamento.setVeiculoId(command.veiculoId());
            orcamento.setValorTotal(command.valorTotal());
            orcamento.setStatus(StatusOrcamento.GERADO);
            orcamento.setDataCriacao(now);
            orcamento.setDataAtualizacao(now);
            orcamento.setDescricaoServicos(command.descricaoServicos());
            orcamento.setHistoricoStatusChanges(new ArrayList<>(
                    java.util.List.of(new StatusOrcamentoChange(StatusOrcamento.GERADO, now))
            ));
            orcamento = orcamentoGateway.save(orcamento);
        }

        // Consultar dados da Ordem de Serviço via HTTP
        log.info("Consultando dados da OS no OS Service: osId={}", command.ordemServicoId());
        var osData = ordemServicoClient.getOrdemServico(command.ordemServicoId());
        
        // Atualizar orçamento com dados da OS
        orcamento.setClienteId(osData.clienteId());
        orcamento.setValorTotal(osData.valorTotal());
        orcamento.setDescricaoServicos(osData.descricaoServicos());
        orcamento = orcamentoGateway.save(orcamento);

        // Criar pagamento PIX via Orders API com dados da OS
        var paymentResult = mercadoPagoGateway.createPixPayment(new CreatePixPaymentCommand(
                osData.valorTotal(),
                osData.descricaoServicos(),
                osData.payerEmail(),
                osData.payerIdentificationType(),
                osData.payerIdentificationNumber(),
                command.ordemServicoId().toString(),
                notificationUrl
        ));

        if (paymentResult.paymentId() == null) {
            throw new IntegracaoMercadoPagoException(
                    "A resposta de criação de pagamento não contém Order ID.");
        }

        var pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrcamentoId(orcamento.getId());
        pagamento.setOsId(command.ordemServicoId());
        pagamento.setMercadoPagoPaymentId(paymentResult.paymentId());
        pagamento.setQrCodePix(paymentResult.qrCode());
        pagamento.setQrCodeBase64(paymentResult.qrCodeBase64());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValorCobrado(paymentResult.amount());
        pagamento.setDataCriacao(now);
        LocalDateTime dataExpiracao = now.plusMinutes(pixExpiresInMinutes);
        pagamento.setDataExpiracao(dataExpiracao);
        pagamento.setDataAtualizacao(now);
        pagamentoGateway.save(pagamento);

        orcamento = ensureAwaitingPaymentStatus(orcamento);

        // SAGA PATTERN: Iniciar saga de pagamento com Orders API
        sagaPaymentService.initiateSaga(
                pagamento.getId(),
                orcamento.getId(),
                command.ordemServicoId(),
                osData.clienteId(),
                paymentResult.paymentId(),
                paymentResult.qrCode(),
                paymentResult.qrCodeBase64(),
                paymentResult.amount(),
                dataExpiracao
        );

        publishPixEvent(pagamento);

        log.info("Orçamento criado e saga iniciada via Orders API: osId={}", command.ordemServicoId());
        return responseAssembler.toResponse(orcamento);
    }

    private void validate(CriarOrcamentoCommand command) {
        UseCaseValidation.validate(command, validator);
        if (notificationUrl == null || notificationUrl.isBlank()) {
            throw new IllegalStateException("Configure MERCADO_PAGO_WEBHOOK_URL para criar cobranças PIX.");
        }
        if (pixExpiresInMinutes <= 0) {
            throw new IllegalStateException("BILLING_PIX_EXPIRES_IN_MINUTES deve ser maior que zero.");
        }
    }

    private void publishPixEvent(Pagamento pagamento) {
        var orcamento = orcamentoGateway.findById(pagamento.getOrcamentoId())
                .orElseThrow(() -> new IllegalStateException(
                        "Orçamento associado ao pagamento não foi encontrado: " + pagamento.getOrcamentoId()));
        eventPublisher.publishCobrancaPixGerada(new CobrancaPixGeradaEvent(
                pagamento.getId(),
                orcamento.getId(),
                pagamento.getOsId(),
                orcamento.getClienteId(),
                pagamento.getQrCodePix(),
                pagamento.getQrCodeBase64(),
                pagamento.getValorCobrado(),
                pagamento.getDataExpiracao(),
                pagamento.getOsId().toString()
        ));
    }

    private Orcamento ensureAwaitingPaymentStatus(Orcamento orcamento) {
        if (orcamento.getStatus() == StatusOrcamento.AGUARDANDO_PAGAMENTO) {
            return orcamento;
        }
        LocalDateTime now = LocalDateTime.now();
        orcamento.setStatus(StatusOrcamento.AGUARDANDO_PAGAMENTO);
        orcamento.setDataAtualizacao(now);
        orcamento.getHistoricoStatusChanges().add(
                new StatusOrcamentoChange(StatusOrcamento.AGUARDANDO_PAGAMENTO, now));
        return orcamentoGateway.save(orcamento);
    }

}
