package com.techchallenge.billing.infrastructure.external;

import com.techchallenge.billing.application.gateway.ports.output.MercadoPagoGateway;
import com.techchallenge.billing.application.gateway.ports.output.models.CreatePixPaymentCommand;
import com.techchallenge.billing.application.gateway.ports.output.models.MercadoPagoPaymentResult;
import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter que implementa MercadoPagoGateway usando a Orders API.
 * Responsável por traduzir entre o domínio da aplicação e o SDK do Mercado Pago.
 */
@Component
@Slf4j
public class MercadoPagoOrdersAdapter implements MercadoPagoGateway {

    private final MercadoPagoOrdersClient ordersClient;

    public MercadoPagoOrdersAdapter(MercadoPagoOrdersClient ordersClient) {
        this.ordersClient = ordersClient;
    }

    /**
     * Cria um pagamento PIX usando Orders API REST.
     * Internamente, cria uma Order com um Payment PIX dentro dela.
     */
    @Override
    public MercadoPagoPaymentResult createPixPayment(CreatePixPaymentCommand command) {
        log.info("Criando pagamento PIX via Orders API: externalReference={}", command.externalReference());

        Map<String, Object> orderResponse = ordersClient.createPixOrder(
                command.externalReference(),
                command.description(),
                command.amount(),
                command.payerEmail(),
                command.payerIdentificationType(),
                command.payerIdentificationNumber(),
                command.notificationUrl()
        );

        return mapOrderResponseToPaymentResult(orderResponse, command.externalReference());
    }

    /**
     * Consulta um pagamento pelo ID.
     * Na Orders API, o paymentId ainda é um ID de Payment (Long),
     * mas podemos consultar diretamente pelo Payment.
     */
    @Override
    public MercadoPagoPaymentResult getPayment(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IntegracaoMercadoPagoException("O ID do pagamento é obrigatório.");
        }
        log.info("Consultando payment: paymentId={}", paymentId);

        Map<String, Object> order = ordersClient.getOrder(paymentId);
        return mapOrderToPaymentResult(order);
    }

    /**
     * Mapeia resposta da Order API para MercadoPagoPaymentResult.
     * Extrai informações da order criada via REST API.
     */
    private MercadoPagoPaymentResult mapOrderResponseToPaymentResult(
            Map<String, Object> orderResponse,
            String externalReference
    ) {
        if (orderResponse == null || !orderResponse.containsKey("id")) {
            throw new IntegracaoMercadoPagoException("Resposta inválida do Mercado Pago: Order não contém ID.");
        }

        String orderId = (String) orderResponse.get("id");
        String status = mapOrderStatus(stringValue(orderResponse.get("status")), null, null);
        Object totalAmountObj = orderResponse.get("total_amount");
        BigDecimal totalAmount = totalAmountObj != null
                ? new BigDecimal(totalAmountObj.toString())
                : BigDecimal.ZERO;

        // Extrair QR Code da transação se disponível
        String qrCode = null;
        String qrCodeBase64 = null;
        Map<String, Object> transactions = (Map<String, Object>) orderResponse.get("transactions");
        if (transactions != null && transactions.containsKey("payments")) {
            Object paymentsObj = transactions.get("payments");
            if (paymentsObj instanceof java.util.List) {
                java.util.List<?> payments = (java.util.List<?>) paymentsObj;
                if (!payments.isEmpty() && payments.get(0) instanceof Map) {
                    Map<String, Object> firstPayment = (Map<String, Object>) payments.get(0);
                    Map<String, Object> paymentMethod = (Map<String, Object>) firstPayment.get("payment_method");
                    if (paymentMethod != null) {
                        qrCode = (String) paymentMethod.get("qr_code");
                        qrCodeBase64 = (String) paymentMethod.get("qr_code_base64");
                    }
                }
            }
        }

        // QR Code virá no webhook order.updated se ainda não estiver disponível
        return new MercadoPagoPaymentResult(
                orderId,
                status,
                totalAmount,
                externalReference,
                null,                   // dateApproved (virá do webhook)
                null,                   // failureCode
                null,                   // failureMessage
                qrCode,
                qrCodeBase64
        );
    }

    private MercadoPagoPaymentResult mapOrderToPaymentResult(Map<String, Object> order) {
        String orderId = stringValue(order.get("id"));
        if (orderId == null || orderId.isBlank()) {
            throw new IntegracaoMercadoPagoException("Resposta inválida do Mercado Pago: Order sem ID.");
        }

        Map<?, ?> payment = firstPayment(order);
        String orderStatus = stringValue(order.get("status"));
        String paymentStatus = stringValue(payment.get("status"));
        String statusDetail = stringValue(payment.get("status_detail"));
        if (statusDetail == null) {
            statusDetail = stringValue(order.get("status_detail"));
        }

        Object amountValue = payment.get("amount");
        if (amountValue == null) {
            amountValue = payment.get("paid_amount");
        }
        if (amountValue == null) {
            amountValue = order.get("total_amount");
        }
        BigDecimal amount = amountValue == null ? BigDecimal.ZERO : new BigDecimal(amountValue.toString());

        Map<?, ?> paymentMethod = asMap(payment.get("payment_method"));
        return new MercadoPagoPaymentResult(
                orderId,
                mapOrderStatus(orderStatus, paymentStatus, statusDetail),
                amount,
                stringValue(order.get("external_reference")),
                null,
                statusDetail,
                "rejected".equalsIgnoreCase(paymentStatus) ? statusDetail : null,
                stringValue(paymentMethod.get("qr_code")),
                stringValue(paymentMethod.get("qr_code_base64"))
        );
    }

    private Map<?, ?> firstPayment(Map<String, Object> order) {
        Map<?, ?> transactions = asMap(order.get("transactions"));
        Object paymentsValue = transactions.get("payments");
        if (paymentsValue instanceof List<?> payments
                && !payments.isEmpty()
                && payments.get(0) instanceof Map<?, ?> payment) {
            return payment;
        }
        return Map.of();
    }

    private Map<?, ?> asMap(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String mapOrderStatus(String orderStatus, String paymentStatus, String statusDetail) {
        String orderState = normalize(orderStatus);
        String paymentState = normalize(paymentStatus);
        String paymentDetail = normalize(statusDetail);

        if ("rejected".equals(paymentState) || "failed".equals(paymentState)) {
            return "rejected";
        }
        if ("canceled".equals(orderState) || "cancelled".equals(orderState)
                || "canceled".equals(paymentState) || "cancelled".equals(paymentState)) {
            return "cancelled";
        }
        if ("expired".equals(orderState) || "expired".equals(paymentState)) {
            return "expired";
        }
        if (("processed".equals(orderState) || "approved".equals(orderState))
                && ("processed".equals(paymentState) || "approved".equals(paymentState))
                && "accredited".equals(paymentDetail)) {
            return "approved";
        }
        if ("processed".equals(orderState) || "approved".equals(orderState)
                || "created".equals(orderState) || "pending".equals(orderState)
                || "in_process".equals(orderState) || "authorized".equals(orderState)
                || "action_required".equals(orderState)
                || "pending".equals(paymentState) || "in_process".equals(paymentState)
                || "authorized".equals(paymentState) || "action_required".equals(paymentState)) {
            return "pending";
        }
        throw new IntegracaoMercadoPagoException(
                "Status Mercado Pago não reconhecido para order: " + orderStatus
                        + ", payment: " + paymentStatus + ", detail: " + statusDetail);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
