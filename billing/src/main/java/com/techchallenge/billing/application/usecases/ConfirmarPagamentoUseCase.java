package com.techchallenge.billing.application.usecases;

import com.techchallenge.billing.application.commands.ConfirmarPagamentoCommand;
import com.techchallenge.billing.application.gateway.ports.input.ConfirmarPagamentoInput;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import com.techchallenge.billing.application.gateway.ports.output.MercadoPagoGateway;
import com.techchallenge.billing.application.gateway.ports.output.OrcamentoGateway;
import com.techchallenge.billing.application.gateway.ports.output.PagamentoGateway;
import com.techchallenge.billing.domain.events.PagamentoAprovadoEvent;
import com.techchallenge.billing.domain.events.PagamentoRecusadoEvent;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamentoChange;
import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ConfirmarPagamentoUseCase implements ConfirmarPagamentoInput {

    private final PagamentoGateway pagamentoGateway;
    private final OrcamentoGateway orcamentoGateway;
    private final MercadoPagoGateway mercadoPagoGateway;
    private final BillingEventPublisher eventPublisher;
    private final Validator validator;

    @Override
    public void execute(ConfirmarPagamentoCommand command) {
        UseCaseValidation.validate(command, validator);
        String mercadoPagoPaymentId = command.mercadoPagoPaymentId();
        var localPayment = pagamentoGateway.findByMercadoPagoPaymentId(mercadoPagoPaymentId)
                .orElseThrow(() -> new IntegracaoMercadoPagoException(
                        "Não existe pagamento local para o ID Mercado Pago " + mercadoPagoPaymentId));
        var result = mercadoPagoGateway.getPayment(mercadoPagoPaymentId);
        if (!localPayment.getOsId().toString().equals(result.externalReference())) {
            throw new IntegracaoMercadoPagoException(
                    "A referência externa do pagamento não corresponde à ordem de serviço local.");
        }

        StatusPagamento newStatus = mapStatus(result.status());
        StatusPagamento previousStatus = localPayment.getStatus();
        LocalDateTime now = LocalDateTime.now();
        localPayment.setStatus(newStatus);
        localPayment.setValorPago(result.amount());
        localPayment.setDataPagamento(result.dateApproved());
        localPayment.setMotivoRejeicao(result.failureMessage());
        localPayment.setDataAtualizacao(now);
        localPayment.setTentativasSincronizacao(localPayment.getTentativasSincronizacao() + 1);
        localPayment.setUltimaSincronizacaoMercadoPago(now);
        Pagamento savedPayment = pagamentoGateway.save(localPayment);

        var quote = orcamentoGateway.findById(savedPayment.getOrcamentoId())
                .orElseThrow(() -> new IntegracaoMercadoPagoException(
                        "Não foi encontrado o orçamento do pagamento " + savedPayment.getId()));
        StatusOrcamento quoteStatus = switch (newStatus) {
            case APROVADO -> StatusOrcamento.PAGO;
            case RECUSADO, EXPIRADO -> StatusOrcamento.RECUSADO;
            case CANCELADO -> StatusOrcamento.CANCELADO;
            case PENDENTE -> quote.getStatus();
        };
        if (quoteStatus != quote.getStatus()) {
            quote.setStatus(quoteStatus);
            quote.setDataAtualizacao(now);
            quote.getHistoricoStatusChanges().add(new StatusOrcamentoChange(quoteStatus, now));
            orcamentoGateway.save(quote);
        }

        if (previousStatus == newStatus) {
            return;
        }
        if (newStatus == StatusPagamento.APROVADO) {
            eventPublisher.publishPagamentoAprovado(new PagamentoAprovadoEvent(
                    savedPayment.getId(),
                    savedPayment.getOrcamentoId(),
                    savedPayment.getOsId(),
                    quote.getClienteId(),
                    savedPayment.getMercadoPagoPaymentId(),
                    result.amount(),
                    result.dateApproved() == null ? now : result.dateApproved()
            ));
        } else if (newStatus != StatusPagamento.PENDENTE) {
            eventPublisher.publishPagamentoRecusado(new PagamentoRecusadoEvent(
                    savedPayment.getId(),
                    savedPayment.getOrcamentoId(),
                    savedPayment.getOsId(),
                    quote.getClienteId(),
                    result.failureMessage() == null ? result.status() : result.failureMessage(),
                    result.status()
            ));
        }
    }

    private StatusPagamento mapStatus(String mercadoPagoStatus) {
        if (mercadoPagoStatus == null || mercadoPagoStatus.isBlank()) {
            throw new IntegracaoMercadoPagoException("O Mercado Pago retornou o status vazio.");
        }
        return switch (mercadoPagoStatus.toLowerCase(Locale.ROOT)) {
            case "approved" -> StatusPagamento.APROVADO;
            case "rejected" -> StatusPagamento.RECUSADO;
            case "cancelled" -> StatusPagamento.CANCELADO;
            case "expired" -> StatusPagamento.EXPIRADO;
            case "pending", "in_process", "authorized" -> StatusPagamento.PENDENTE;
            default -> throw new IntegracaoMercadoPagoException(
                    "Status Mercado Pago não reconhecido: " + mercadoPagoStatus);
        };
    }
}
