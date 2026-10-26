package com.techchallenge.billing.infrastructure.persistence.mappers;

import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.infrastructure.persistence.documents.PagamentoDocument;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PagamentoMongoMapper {

    public PagamentoDocument toDocument(Pagamento pagamento) {
        Objects.requireNonNull(pagamento, "pagamento must not be null");

        var document = new PagamentoDocument();
        document.setId(pagamento.getId());
        document.setOrcamentoId(pagamento.getOrcamentoId());
        document.setOsId(pagamento.getOsId());
        document.setMercadoPagoPaymentId(pagamento.getMercadoPagoPaymentId());
        document.setQrCodePix(pagamento.getQrCodePix());
        document.setQrCodeBase64(pagamento.getQrCodeBase64());
        document.setStatus(pagamento.getStatus());
        document.setValorCobrado(pagamento.getValorCobrado());
        document.setValorPago(pagamento.getValorPago());
        document.setMotivoRejeicao(pagamento.getMotivoRejeicao());
        document.setDataCriacao(pagamento.getDataCriacao());
        document.setDataExpiracao(pagamento.getDataExpiracao());
        document.setDataPagamento(pagamento.getDataPagamento());
        document.setDataAtualizacao(pagamento.getDataAtualizacao());
        document.setTentativasSincronizacao(pagamento.getTentativasSincronizacao());
        document.setUltimaSincronizacaoMercadoPago(pagamento.getUltimaSincronizacaoMercadoPago());
        return document;
    }

    public Pagamento toDomain(PagamentoDocument document) {
        Objects.requireNonNull(document, "document must not be null");

        var pagamento = new Pagamento();
        pagamento.setId(document.getId());
        pagamento.setOrcamentoId(document.getOrcamentoId());
        pagamento.setOsId(document.getOsId());
        pagamento.setMercadoPagoPaymentId(document.getMercadoPagoPaymentId());
        pagamento.setQrCodePix(document.getQrCodePix());
        pagamento.setQrCodeBase64(document.getQrCodeBase64());
        pagamento.setStatus(document.getStatus());
        pagamento.setValorCobrado(document.getValorCobrado());
        pagamento.setValorPago(document.getValorPago());
        pagamento.setMotivoRejeicao(document.getMotivoRejeicao());
        pagamento.setDataCriacao(document.getDataCriacao());
        pagamento.setDataExpiracao(document.getDataExpiracao());
        pagamento.setDataPagamento(document.getDataPagamento());
        pagamento.setDataAtualizacao(document.getDataAtualizacao());
        pagamento.setTentativasSincronizacao(document.getTentativasSincronizacao());
        pagamento.setUltimaSincronizacaoMercadoPago(document.getUltimaSincronizacaoMercadoPago());
        return pagamento;
    }
}
