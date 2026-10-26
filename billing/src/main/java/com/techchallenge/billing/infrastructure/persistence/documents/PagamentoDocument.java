package com.techchallenge.billing.infrastructure.persistence.documents;

import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pagamentos")
@CompoundIndexes({
        @CompoundIndex(name = "status_data_criacao_idx", def = "{'status': 1, 'dataCriacao': -1}")
})
public class PagamentoDocument {

    @Id
    private UUID id;

    private UUID orcamentoId;

    @Indexed
    private UUID osId;

    @Indexed(unique = true, sparse = true)
    private String mercadoPagoPaymentId;

    private String qrCodePix;
    private String qrCodeBase64;
    private StatusPagamento status;
    private BigDecimal valorCobrado;
    private BigDecimal valorPago;
    private String motivoRejeicao;
    private LocalDateTime dataCriacao;
    @Indexed
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataPagamento;
    private LocalDateTime dataAtualizacao;
    private int tentativasSincronizacao;
    private LocalDateTime ultimaSincronizacaoMercadoPago;
}
