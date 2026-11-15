package com.techchallenge.billing.domain.models;

import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamentoChange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Orcamento {

    private UUID id;
    private UUID osId;
    private UUID clienteId;
    private UUID veiculoId;
    private BigDecimal valorTotal;
    private StatusOrcamento status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private String descricaoServicos;
    private List<StatusOrcamentoChange> historicoStatusChanges = new ArrayList<>();
}
