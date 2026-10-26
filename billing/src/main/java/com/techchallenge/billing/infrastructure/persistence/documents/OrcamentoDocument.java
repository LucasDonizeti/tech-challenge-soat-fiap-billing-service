package com.techchallenge.billing.infrastructure.persistence.documents;

import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamentoChange;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orcamentos")
@CompoundIndexes({
        @CompoundIndex(name = "status_data_criacao_idx", def = "{'status': 1, 'dataCriacao': -1}")
})
public class OrcamentoDocument {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private UUID osId;

    @Indexed
    private UUID clienteId;
    private UUID veiculoId;
    private BigDecimal valorTotal;
    private StatusOrcamento status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private String descricaoServicos;
    private List<StatusOrcamentoChange> historicoStatusChanges = new ArrayList<>();
}
