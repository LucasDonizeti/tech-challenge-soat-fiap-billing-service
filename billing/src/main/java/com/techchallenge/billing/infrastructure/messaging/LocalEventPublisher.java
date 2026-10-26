package com.techchallenge.billing.infrastructure.messaging;

import com.techchallenge.billing.domain.events.CobrancaPixGeradaEvent;
import com.techchallenge.billing.domain.events.PagamentoAprovadoEvent;
import com.techchallenge.billing.domain.events.PagamentoRecusadoEvent;
import com.techchallenge.billing.application.gateway.ports.output.BillingEventPublisher;
import lombok.extern.slf4j.Slf4j;

/**
 * Local/Mock Event Publisher Implementation - Fase A
 * 
 * Used for local development without AWS
 * Events are logged to console instead of being published to SNS
 * 
 * Environment: LOCAL / TEST
 */
@Slf4j
public class LocalEventPublisher implements BillingEventPublisher {

    @Override
    public void publishCobrancaPixGerada(CobrancaPixGeradaEvent event) {
        log.info("📤 [LOCAL - MOCK] Publicando CobrancaPixGeradaEvent:");
        log.info("   - Pagamento ID: {}", event.getPagamentoId());
        log.info("   - Ordem Serviço ID: {}", event.getOrdemServicoId());
        log.info("   - Cliente ID: {}", event.getClienteId());
        log.info("   - Valor Cobrado: {}", event.getValorCobrado());
        log.info("   - QR Code PIX: {}...{}", 
            event.getQrCodePix() != null && event.getQrCodePix().length() > 10 
                ? event.getQrCodePix().substring(0, 10) 
                : event.getQrCodePix(),
            event.getQrCodePix() != null && event.getQrCodePix().length() > 10
                ? event.getQrCodePix().substring(event.getQrCodePix().length() - 10)
                : "");
        log.info("   - Data Expiração: {}", event.getDataExpiracao());
        log.info("   - Topic: billing-cobranca-pix-gerada-topic (MOCK - não publicado no SNS)");
        log.info("   ✅ Evento registrado em memória (não persistido)");
        
        // In local mode, event is just logged - no actual SNS publishing
        // For test purposes, you can add to in-memory store if needed
    }

    @Override
    public void publishPagamentoAprovado(PagamentoAprovadoEvent event) {
        log.info("📤 [LOCAL - MOCK] Publicando PagamentoAprovadoEvent:");
        log.info("   - Pagamento ID: {}", event.getPagamentoId());
        log.info("   - Ordem Serviço ID: {}", event.getOrdemServicoId());
        log.info("   - Cliente ID: {}", event.getClienteId());
        log.info("   - Mercado Pago Payment ID: {}", event.getMercadoPagoPaymentId());
        log.info("   - Valor Pago: {}", event.getValorPago());
        log.info("   - Data Pagamento: {}", event.getDataPagamento());
        log.info("   - Topic: billing-pagamento-aprovado-topic (MOCK - não publicado no SNS)");
        log.info("   ✅ Evento registrado em memória (não persistido)");
    }

    @Override
    public void publishPagamentoRecusado(PagamentoRecusadoEvent event) {
        log.info("📤 [LOCAL - MOCK] Publicando PagamentoRecusadoEvent:");
        log.info("   - Pagamento ID: {}", event.getPagamentoId());
        log.info("   - Ordem Serviço ID: {}", event.getOrdemServicoId());
        log.info("   - Cliente ID: {}", event.getClienteId());
        log.info("   - Motivo: {}", event.getMotivo());
        log.info("   - Status Mercado Pago: {}", event.getStatusMercadoPago());
        log.info("   - Data Rejeição: {}", event.getDataRejeicao());
        log.info("   - Topic: billing-pagamento-recusado-topic (MOCK - não publicado no SNS)");
        log.info("   ✅ Evento registrado em memória (não persistido)");
    }
}
