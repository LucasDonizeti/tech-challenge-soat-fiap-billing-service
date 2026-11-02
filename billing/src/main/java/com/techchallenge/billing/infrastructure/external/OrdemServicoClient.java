package com.techchallenge.billing.infrastructure.external;

import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import com.techchallenge.billing.infrastructure.security.ServiceJwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cliente para consumir dados da OS Service via HTTP.
 * Responsável por obter informações de Ordem de Serviço para criar pagamentos.
 */
@Component
@Slf4j
public class OrdemServicoClient {

    private final RestTemplate restTemplate;
    private final ServiceJwtTokenProvider tokenProvider;
    private final String osServiceBaseUrl;

    public OrdemServicoClient(
            RestTemplate restTemplate,
            ServiceJwtTokenProvider tokenProvider,
            @Value("${os-service.base-url:http://localhost:8081}") String osServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.tokenProvider = tokenProvider;
        this.osServiceBaseUrl = osServiceBaseUrl;
    }

    /**
     * Obtém dados da Ordem de Serviço
     */
    public OrdemServicoData getOrdemServico(UUID osId) {
        if (osId == null) {
            throw new IntegracaoMercadoPagoException("O ID da ordem de serviço é obrigatório.");
        }

        String url = osServiceBaseUrl + "/v1/os/" + osId;
        log.info("Consultando OS Service: url={}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenProvider.createToken());
            HttpEntity<Void> request = new HttpEntity<>(headers);
            OrdemServicoResponseDto response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    OrdemServicoResponseDto.class
            ).getBody();
            
            if (response == null) {
                throw new IntegracaoMercadoPagoException("OS não encontrada: " + osId);
            }
            if (response.getCliente() == null || response.getCliente().getId() == null) {
                throw new IntegracaoMercadoPagoException(
                        "A OS não contém um ID de cliente válido: " + osId);
            }

            log.info("OS consultada com sucesso: osId={}, valorTotal={}", osId, response.getValorTotal());

            // Mapear para objeto interno
            return new OrdemServicoData(
                    response.getId(),
                    response.getCliente().getId(),
                    response.getValorTotal(),
                    formatarDescricaoServicos(response.getItensServico()),
                    response.getCliente() != null ? response.getCliente().getEmail() : null,
                    response.getCliente() != null ? getTipoIdentificacao(response.getCliente().getTipo()) : null,
                    response.getCliente() != null ? getNumeroIdentificacao(response.getCliente()) : null
            );

        } catch (RestClientException exception) {
            log.error("Erro ao consultar OS Service: osId={}, url={}", osId, url, exception);
            throw new IntegracaoMercadoPagoException(
                    "Falha ao consultar dados da OS: " + exception.getMessage(), exception
            );
        }
    }

    /**
     * Formata descrição dos serviços para o Mercado Pago
     */
    private String formatarDescricaoServicos(java.util.List<OrdemServicoResponseDto.ItemServicoDto> itens) {
        if (itens == null || itens.isEmpty()) {
            return "Serviços diversos";
        }

        return itens.stream()
                .map(item -> item.getServico() != null ? item.getServico().getNome() : "Serviço")
                .limit(3)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Serviços diversos");
    }

    /**
     * Mapeia tipo de pessoa (PF/PJ) para tipo de identificação do Mercado Pago
     */
    private String getTipoIdentificacao(String tipoPessoa) {
        if (tipoPessoa == null) {
            return "CPF";
        }
        return "PJ".equalsIgnoreCase(tipoPessoa) ? "CNPJ" : "CPF";
    }

    /**
     * Obtém número de identificação (CPF ou CNPJ)
     */
    private String getNumeroIdentificacao(OrdemServicoResponseDto.ClienteDto cliente) {
        if (cliente == null) {
            return null;
        }

        // Prefere CNPJ se disponível (pessoa jurídica)
        if (cliente.getCnpj() != null && !cliente.getCnpj().isBlank()) {
            return cliente.getCnpj();
        }

        // Fallback para CPF
        return cliente.getCpf();
    }

    /**
     * DTO interno com dados necessários para criar pagamento PIX
     */
    public record OrdemServicoData(
            UUID id,
            UUID clienteId,
            BigDecimal valorTotal,
            String descricaoServicos,
            String payerEmail,
            String payerIdentificationType,
            String payerIdentificationNumber
    ) {
    }

    /**
     * DTOs mapeados da resposta do OS Service (versão simplificada para este contexto)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrdemServicoResponseDto {
        private java.util.UUID id;
        private BigDecimal valorTotal;
        private String status;
        private java.time.LocalDateTime dataCriacao;
        private java.time.LocalDateTime dataInicioExecucao;
        private java.time.LocalDateTime dataFinalizacao;
        private ClienteDto cliente;
        private VeiculoDto veiculo;
        private java.util.List<ItemServicoDto> itensServico;

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ClienteDto {
            private java.util.UUID id;
            private String nome;
            private String cpf;
            private String cnpj;
            private String email;
            private String tipo;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class VeiculoDto {
            private java.util.UUID id;
            private String placa;
            private String modelo;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ItemServicoDto {
            private java.util.UUID id;
            private ServicoDto servico;
            private BigDecimal preco;
            private String status;
        }

        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class ServicoDto {
            private java.util.UUID id;
            private String nome;
            private String descricao;
            private BigDecimal preco;
        }
    }
}
