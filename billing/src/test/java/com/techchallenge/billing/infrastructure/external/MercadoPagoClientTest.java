package com.techchallenge.billing.infrastructure.external;

import com.mercadopago.client.MercadoPagoClient;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.payment.Payment;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoClientTest {
    /*

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private Payment payment;

    @Test
    void createsPaymentUsingSdkAndStableIdempotencyKey() throws Exception {
        var client = new MercadoPagoClient(paymentClient, "test-token");
        var request = PaymentCreateRequest.builder()
                .externalReference("os-123")
                .build();
        when(paymentClient.create(any(PaymentCreateRequest.class), any(MPRequestOptions.class)))
                .thenReturn(payment);

        var result = client.createPixPayment(request);

        var requestCaptor = ArgumentCaptor.forClass(PaymentCreateRequest.class);
        var optionsCaptor = ArgumentCaptor.forClass(MPRequestOptions.class);
        verify(paymentClient).create(requestCaptor.capture(), optionsCaptor.capture());
        assertEquals(request, requestCaptor.getValue());
        assertEquals(
                UUID.nameUUIDFromBytes("os-123".getBytes(StandardCharsets.UTF_8)).toString(),
                optionsCaptor.getValue().getCustomHeaders().get("X-Idempotency-Key")
        );
        assertEquals(payment, result);
    }

    @Test
    void failsExplicitlyWhenAccessTokenIsMissing() {
        var client = new MercadoPagoClient(paymentClient, "");
        var request = PaymentCreateRequest.builder()
                .externalReference("os-123")
                .build();

        assertThrows(IntegracaoMercadoPagoException.class, () -> client.createPixPayment(request));
    }

    @Test
    void includesMercadoPagoApiResponseWhenPaymentCreationFails() throws Exception {
        var client = new MercadoPagoClient(paymentClient, "test-token");
        var request = PaymentCreateRequest.builder()
                .externalReference("os-123")
                .build();
        String responseBody = """
                {"message":"invalid payer","cause":[{"code":3003,"description":"payer.email is invalid"}]}
                """;
        var apiException = new MPApiException(
                "Api error. Check response for details",
                new MPResponse(400, Map.of(), responseBody)
        );
        when(paymentClient.create(any(PaymentCreateRequest.class), any(MPRequestOptions.class)))
                .thenThrow(apiException);

        var exception = assertThrows(
                IntegracaoMercadoPagoException.class,
                () -> client.createPixPayment(request)
        );

        assertTrue(exception.getMessage().contains("HTTP status: 400"));
        assertTrue(exception.getMessage().contains(responseBody));
        assertSame(apiException, exception.getCause());
    }

     */
}
