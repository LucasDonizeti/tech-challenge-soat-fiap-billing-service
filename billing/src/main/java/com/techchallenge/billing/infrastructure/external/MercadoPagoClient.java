package com.techchallenge.billing.infrastructure.external;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Component
public class MercadoPagoClient {

    private final PaymentClient paymentClient;
    private final String accessToken;

    public MercadoPagoClient(
            PaymentClient paymentClient,
            @Value("${mercadopago.access-token:}") String accessToken
    ) {
        this.paymentClient = paymentClient;
        this.accessToken = accessToken;
    }

    public Payment createPixPayment(PaymentCreateRequest request) {
        requireAccessToken();
        if (request == null || request.getExternalReference() == null
                || request.getExternalReference().isBlank()) {
            throw new IntegracaoMercadoPagoException("A referência externa é obrigatória para criar pagamento.");
        }
        String idempotencyKey = UUID.nameUUIDFromBytes(
                request.getExternalReference().getBytes(StandardCharsets.UTF_8)
        ).toString();

        try {
            var options = MPRequestOptions.builder()
                    .customHeaders(Map.of("X-Idempotency-Key", idempotencyKey))
                    .build();
            return paymentClient.create(request, options);
        } catch (MPException | MPApiException exception) {
            throw new IntegracaoMercadoPagoException("Falha ao criar pagamento PIX.", exception);
        }
    }

    public Payment getPayment(String paymentId) {
        requireAccessToken();
        try {
            return paymentClient.get(Long.valueOf(paymentId));
        } catch (NumberFormatException exception) {
            throw new IntegracaoMercadoPagoException("O ID do pagamento retornado pelo webhook é inválido.", exception);
        } catch (MPException | MPApiException exception) {
            throw new IntegracaoMercadoPagoException("Falha ao consultar pagamento.", exception);
        }
    }

    private void requireAccessToken() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IntegracaoMercadoPagoException(
                    "Configure MERCADO_PAGO_ACCESS_TOKEN antes de solicitar pagamentos.");
        }
    }
}
