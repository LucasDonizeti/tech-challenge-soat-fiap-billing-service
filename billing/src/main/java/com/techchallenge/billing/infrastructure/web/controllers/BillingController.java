package com.techchallenge.billing.infrastructure.web.controllers;

import com.techchallenge.billing.application.commands.ConsultarOrcamentoCommand;
import com.techchallenge.billing.application.commands.CriarOrcamentoCommand;
import com.techchallenge.billing.application.commands.ListarPagamentosCommand;
import com.techchallenge.billing.application.commands.ListarOrcamentosCommand;
import com.techchallenge.billing.application.gateway.ports.input.ConsultarOrcamentoInput;
import com.techchallenge.billing.application.gateway.ports.input.CriarOrcamentoInput;
import com.techchallenge.billing.application.gateway.ports.input.ListarOrcamentosInput;
import com.techchallenge.billing.application.gateway.ports.input.ListarPagamentosInput;
import com.techchallenge.billing.application.responses.OrcamentoResponse;
import com.techchallenge.billing.application.responses.PagamentoResponse;
import com.techchallenge.billing.domain.models.valueobjects.StatusOrcamento;
import com.techchallenge.billing.infrastructure.web.dtos.CriarOrcamentoRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billings")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orçamentos")
@SecurityRequirement(name = "bearerAuth")
public class BillingController {

    private final CriarOrcamentoInput criarOrcamentoInput;
    private final ConsultarOrcamentoInput consultarOrcamentoInput;
    private final ListarOrcamentosInput listarOrcamentosInput;
    private final ListarPagamentosInput listarPagamentosInput;

    @PostMapping("/orcamentos")
    public ResponseEntity<OrcamentoResponse> criarOrcamento(@Valid @RequestBody CriarOrcamentoRequest request) {
        var command = new CriarOrcamentoCommand(
                request.ordemServicoId(),
                request.clienteId(),
                request.veiculoId(),
                request.valorTotal(),
                request.descricaoServicos(),
                null,
                request.payerEmail(),
                request.payerIdentificationType(),
                request.payerIdentificationNumber()
        );
        var response = criarOrcamentoInput.execute(command);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/orcamentos")
    public Page<OrcamentoResponse> listarOrcamentos(
            @RequestParam UUID clienteId,
            @RequestParam(required = false) StatusOrcamento status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return listarOrcamentosInput.execute(new ListarOrcamentosCommand(clienteId, status, pageable));
    }

    @GetMapping("/orcamentos/{orcamentoId}")
    public OrcamentoResponse obterOrcamento(@PathVariable UUID orcamentoId) {
        return consultarOrcamentoInput.execute(new ConsultarOrcamentoCommand(orcamentoId));
    }

    @GetMapping("/os/{osId}/pagamentos")
    public List<PagamentoResponse> listarPagamentos(@PathVariable UUID osId) {
        return listarPagamentosInput.execute(new ListarPagamentosCommand(osId));
    }
}
