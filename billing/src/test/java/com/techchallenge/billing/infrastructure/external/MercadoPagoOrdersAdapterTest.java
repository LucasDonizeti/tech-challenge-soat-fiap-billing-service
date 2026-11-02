package com.techchallenge.billing.infrastructure.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoOrdersAdapterTest {

    @Test
    void fetchesOrderAndMapsProcessedStatusToApprovedPayment() {
        String orderId = "ORDTST01M6EXRT5E37H8P5VX32YF6E0K";
        String externalReference = "6bc837b8-3c12-45ff-a2e5-2723c5682b7f";
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("https://api.mercadopago.com/v1/orders/" + orderId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "%s",
                          "status": "processed",
                          "external_reference": "%s",
                          "total_amount": "380.00",
                          "transactions": {
                            "payments": [{
                              "status": "processed",
                              "status_detail": "accredited",
                              "amount": "380.00"
                            }]
                          }
                        }
                        """.formatted(orderId, externalReference),
                        MediaType.APPLICATION_JSON
                ));
        var client = new MercadoPagoOrdersClient(
                restTemplate, "access-token", 30, new ObjectMapper());
        var adapter = new MercadoPagoOrdersAdapter(client);

        var result = adapter.getPayment(orderId);

        assertEquals(orderId, result.paymentId());
        assertEquals("approved", result.status());
        assertEquals("380.00", result.amount().toPlainString());
        assertEquals(externalReference, result.externalReference());
        assertEquals("accredited", result.failureCode());
        server.verify();
    }

    @Test
    void mapsCanceledOrderToCanceledPayment() {
        String orderId = "ORD-CANCELED";
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("https://api.mercadopago.com/v1/orders/" + orderId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "%s",
                          "status": "canceled",
                          "external_reference": "os-id",
                          "total_amount": "380.00"
                        }
                        """.formatted(orderId),
                        MediaType.APPLICATION_JSON
                ));
        var adapter = new MercadoPagoOrdersAdapter(new MercadoPagoOrdersClient(
                restTemplate, "access-token", 30, new ObjectMapper()));

        var result = adapter.getPayment(orderId);

        assertEquals("cancelled", result.status());
        server.verify();
    }

    @Test
    void doesNotApproveProcessedOrderWithoutAccreditedPaymentDetail() {
        String orderId = "ORD-PROCESSED-NOT-ACCREDITED";
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("https://api.mercadopago.com/v1/orders/" + orderId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "%s",
                          "status": "processed",
                          "external_reference": "os-id",
                          "transactions": {
                            "payments": [{
                              "status": "processed",
                              "status_detail": "pending"
                            }]
                          }
                        }
                        """.formatted(orderId),
                        MediaType.APPLICATION_JSON
                ));
        var adapter = new MercadoPagoOrdersAdapter(new MercadoPagoOrdersClient(
                restTemplate, "access-token", 30, new ObjectMapper()));

        var result = adapter.getPayment(orderId);

        assertEquals("pending", result.status());
        server.verify();
    }
}
