package com.techchallenge.billing.infrastructure.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoSdkConfig {

    @Bean
    PaymentClient mercadoPagoPaymentClient(
            @Value("${mercadopago.access-token:}") String accessToken
    ) {
        if (accessToken != null && !accessToken.isBlank()) {
            MercadoPagoConfig.setAccessToken(accessToken);
        }
        return new PaymentClient();
    }
}
