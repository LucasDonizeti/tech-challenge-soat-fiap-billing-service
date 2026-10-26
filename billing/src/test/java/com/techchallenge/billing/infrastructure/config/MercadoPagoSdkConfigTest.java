package com.techchallenge.billing.infrastructure.config;

import com.mercadopago.client.payment.PaymentClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - MercadoPagoSdkConfig")
class MercadoPagoSdkConfigTest {

    private MercadoPagoSdkConfig config;

    @BeforeEach
    void setUp() {
        config = new MercadoPagoSdkConfig();
    }

    @Test
    @DisplayName("Deve criar bean PaymentClient com access token")
    void deveCriarBeanPaymentClientComAccessToken() {
        PaymentClient client = config.mercadoPagoPaymentClient("test-access-token");

        assertNotNull(client);
    }

    @Test
    @DisplayName("Deve criar bean PaymentClient com access token vazio")
    void deveCriarBeanPaymentClientComAccessTokenVazio() {
        PaymentClient client = config.mercadoPagoPaymentClient("");

        assertNotNull(client);
    }

    @Test
    @DisplayName("Deve criar bean PaymentClient com access token nulo")
    void deveCriarBeanPaymentClientComAccessTokenNulo() {
        PaymentClient client = config.mercadoPagoPaymentClient(null);

        assertNotNull(client);
    }

    @Test
    @DisplayName("Deve criar bean PaymentClient com access token em branco")
    void deveCriarBeanPaymentClientComAccessTokenEmBranco() {
        PaymentClient client = config.mercadoPagoPaymentClient("   ");

        assertNotNull(client);
    }

    @Test
    @DisplayName("Deve criar bean PaymentClient sem lançar exceção")
    void deveCriarBeanPaymentClientSemLancarExcecao() {
        assertDoesNotThrow(() -> config.mercadoPagoPaymentClient("test-token"));
    }
}
