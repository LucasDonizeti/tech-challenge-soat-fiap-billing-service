package com.techchallenge.billing.application.gateway.ports.output;

import com.techchallenge.billing.application.gateway.ports.output.models.CreatePixPaymentCommand;
import com.techchallenge.billing.application.gateway.ports.output.models.MercadoPagoPaymentResult;

public interface MercadoPagoGateway {

    MercadoPagoPaymentResult createPixPayment(CreatePixPaymentCommand command);

    MercadoPagoPaymentResult getPayment(String paymentId);
}
