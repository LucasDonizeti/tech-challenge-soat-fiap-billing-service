package com.techchallenge.billing.infrastructure.persistence.mappers;

import com.techchallenge.billing.domain.models.Orcamento;
import com.techchallenge.billing.infrastructure.persistence.documents.OrcamentoDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Component
public class OrcamentoMongoMapper {

    public OrcamentoDocument toDocument(Orcamento orcamento) {
        Objects.requireNonNull(orcamento, "orcamento must not be null");

        var document = new OrcamentoDocument();
        document.setId(orcamento.getId());
        document.setOsId(orcamento.getOsId());
        document.setClienteId(orcamento.getClienteId());
        document.setVeiculoId(orcamento.getVeiculoId());
        document.setValorTotal(orcamento.getValorTotal());
        document.setStatus(orcamento.getStatus());
        document.setDataCriacao(orcamento.getDataCriacao());
        document.setDataAtualizacao(orcamento.getDataAtualizacao());
        document.setDescricaoServicos(orcamento.getDescricaoServicos());
        document.setHistoricoStatusChanges(orcamento.getHistoricoStatusChanges() == null
                ? new ArrayList<>()
                : new ArrayList<>(orcamento.getHistoricoStatusChanges()));
        return document;
    }

    public Orcamento toDomain(OrcamentoDocument document) {
        Objects.requireNonNull(document, "document must not be null");

        var orcamento = new Orcamento();
        orcamento.setId(document.getId());
        orcamento.setOsId(document.getOsId());
        orcamento.setClienteId(document.getClienteId());
        orcamento.setVeiculoId(document.getVeiculoId());
        orcamento.setValorTotal(document.getValorTotal());
        orcamento.setStatus(document.getStatus());
        orcamento.setDataCriacao(document.getDataCriacao());
        orcamento.setDataAtualizacao(document.getDataAtualizacao());
        orcamento.setDescricaoServicos(document.getDescricaoServicos());
        orcamento.setHistoricoStatusChanges(document.getHistoricoStatusChanges() == null
                ? new ArrayList<>()
                : new ArrayList<>(document.getHistoricoStatusChanges()));
        return orcamento;
    }
}
