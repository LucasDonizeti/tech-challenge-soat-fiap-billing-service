package com.techchallenge.billing.application.usecases.assemblers;

import com.techchallenge.billing.application.responses.PagamentoResponse;
import com.techchallenge.billing.domain.models.Pagamento;
import org.springframework.stereotype.Component;

@Component
public class PagamentoResponseAssembler {

    public PagamentoResponse toResponse(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getOrcamentoId(),
                pagamento.getOsId(),
                pagamento.getQrCodePix(),
                pagamento.getQrCodeBase64(),
                pagamento.getStatus().name(),
                pagamento.getDataExpiracao(),
                pagamento.getDataPagamento()
        );
    }
}
