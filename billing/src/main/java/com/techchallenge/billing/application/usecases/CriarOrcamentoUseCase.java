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
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriarOrcamentoUseCase implements CriarOrcamentoInput {

    private final OrcamentoGateway orcamentoGateway;
    private final PagamentoGateway pagamentoGateway;
    private final MercadoPagoGateway mercadoPagoGateway;
    private final BillingEventPublisher eventPublisher;
    private final Validator validator;
    private final OrcamentoResponseAssembler responseAssembler;

    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    @Value("${billing.pix.expires-in-minutes:30}")
    private long pixExpiresInMinutes;

    @Override
    public OrcamentoResponse execute(CriarOrcamentoCommand command) {
        validate(command);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dataOrcamento = command.dataOrcamento() == null ? now : command.dataOrcamento();
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
            orcamento.setDataCriacao(dataOrcamento);
            orcamento.setDataAtualizacao(now);
            orcamento.setDescricaoServicos(command.descricaoServicos());
            orcamento.setHistoricoStatusChanges(new ArrayList<>(
                    java.util.List.of(new StatusOrcamentoChange(StatusOrcamento.GERADO, dataOrcamento))
            ));
            orcamento = orcamentoGateway.save(orcamento);
        }

        var paymentResult = mercadoPagoGateway.createPixPayment(new CreatePixPaymentCommand(
                command.valorTotal(),
                command.descricaoServicos(),
                command.payerEmail(),
                command.payerIdentificationType(),
                command.payerIdentificationNumber(),
                command.ordemServicoId().toString(),
                notificationUrl
        ));
        if (paymentResult.paymentId() == null || paymentResult.qrCode() == null
                || paymentResult.qrCodeBase64() == null) {
            throw new IntegracaoMercadoPagoException(
                    "A resposta de criação de pagamento não contém ID e QR Code PIX.");
        }

        var pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrcamentoId(orcamento.getId());
        pagamento.setOsId(command.ordemServicoId());
        pagamento.setMercadoPagoPaymentId(paymentResult.paymentId());
        pagamento.setQrCodePix(paymentResult.qrCode());
        pagamento.setQrCodeBase64(paymentResult.qrCodeBase64());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValorCobrado(command.valorTotal());
        pagamento.setDataCriacao(now);
        pagamento.setDataExpiracao(now.plusMinutes(pixExpiresInMinutes));
        pagamento.setDataAtualizacao(now);
        pagamentoGateway.save(pagamento);

        orcamento = ensureAwaitingPaymentStatus(orcamento);
        publishPixEvent(pagamento);
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
