package com.techchallenge.billing.domain.models;

import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    private UUID id;
    private UUID orcamentoId;
    private UUID osId;
    private String mercadoPagoPaymentId;
    private String qrCodePix;
    private String qrCodeBase64;
    private StatusPagamento status;
    private BigDecimal valorCobrado;
    private BigDecimal valorPago;
    private String motivoRejeicao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataPagamento;
    private LocalDateTime dataAtualizacao;
    private int tentativasSincronizacao;
    private LocalDateTime ultimaSincronizacaoMercadoPago;
}
