package com.techchallenge.billing.infrastructure.external;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.resources.payment.Payment;
import com.techchallenge.billing.application.gateway.ports.output.MercadoPagoGateway;
import com.techchallenge.billing.application.gateway.ports.output.models.CreatePixPaymentCommand;
import com.techchallenge.billing.application.gateway.ports.output.models.MercadoPagoPaymentResult;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class MercadoPagoIntegrationAdapter implements MercadoPagoGateway {

    private static final String PIX_PAYMENT_METHOD = "pix";

    private final MercadoPagoClient client;
    private final int pixExpiresInMinutes;

    public MercadoPagoIntegrationAdapter(
            MercadoPagoClient client,
            @Value("${billing.pix.expires-in-minutes:30}") int pixExpiresInMinutes
    ) {
        this.client = client;
        this.pixExpiresInMinutes = pixExpiresInMinutes;
    }

    @Override
    public MercadoPagoPaymentResult createPixPayment(CreatePixPaymentCommand command) {
        var payer = PaymentPayerRequest.builder()
                .email(command.payerEmail())
                .identification(IdentificationRequest.builder()
                        .type(command.payerIdentificationType())
                        .number(command.payerIdentificationNumber())
                        .build())
                .build();
        var request = PaymentCreateRequest.builder()
                .transactionAmount(command.amount())
                .description(command.description())
                .paymentMethodId(PIX_PAYMENT_METHOD)
                .externalReference(command.externalReference())
                .notificationUrl(command.notificationUrl())
                .dateOfExpiration(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(pixExpiresInMinutes))
                .payer(payer)
                .build();
        return toResult(client.createPixPayment(request));
    }

    @Override
    public MercadoPagoPaymentResult getPayment(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IntegracaoMercadoPagoException("O ID do pagamento é obrigatório.");
        }
        return toResult(client.getPayment(paymentId));
    }

    private MercadoPagoPaymentResult toResult(Payment response) {
        if (response == null || response.getId() == null) {
            throw new IntegracaoMercadoPagoException("Resposta inválida do Mercado Pago.");
        }

        String qrCode = null;
        String qrCodeBase64 = null;
        if (response.getPointOfInteraction() != null
                && response.getPointOfInteraction().getTransactionData() != null) {
            qrCode = response.getPointOfInteraction().getTransactionData().getQrCode();
            qrCodeBase64 = response.getPointOfInteraction().getTransactionData().getQrCodeBase64();
        }
        LocalDateTime dateApproved = response.getDateApproved() == null
                ? null
                : response.getDateApproved().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();

        return new MercadoPagoPaymentResult(
                response.getId().toString(),
                response.getStatus(),
                response.getTransactionAmount(),
                response.getExternalReference(),
                dateApproved,
                null,
                response.getStatusDetail(),
                qrCode,
                qrCodeBase64
        );
    }
}
