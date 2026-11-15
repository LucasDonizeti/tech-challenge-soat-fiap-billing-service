package com.techchallenge.billing.application.usecases.assemblers;

import com.techchallenge.billing.application.responses.PagamentoResponse;
import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - PagamentoResponseAssembler")
class PagamentoResponseAssemblerTest {

    private PagamentoResponseAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new PagamentoResponseAssembler();
    }

    @Test
    @DisplayName("Deve converter Pagamento para PagamentoResponse")
    void deveConverterPagamentoParaPagamentoResponse() {
        Pagamento pagamento = createPagamento();

        PagamentoResponse response = assembler.toResponse(pagamento);

        assertNotNull(response);
        assertEquals(pagamento.getId(), response.id());
        assertEquals(pagamento.getOrcamentoId(), response.orcamentoId());
        assertEquals(pagamento.getOsId(), response.ordemServicoId());
        assertEquals(pagamento.getQrCodePix(), response.qrCodePix());
        assertEquals(pagamento.getQrCodeBase64(), response.qrCodeBase64());
        assertEquals(pagamento.getStatus().name(), response.status());
        assertEquals(pagamento.getDataExpiracao(), response.dataExpiracao());
        assertEquals(pagamento.getDataPagamento(), response.dataPagamento());
    }

    @Test
    @DisplayName("Deve converter Pagamento com campos nulos opcionais")
    void deveConverterPagamentoComCamposNulosOpcionais() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrcamentoId(UUID.randomUUID());
        pagamento.setOsId(UUID.randomUUID());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataExpiracao(null);
        pagamento.setDataPagamento(null);

        PagamentoResponse response = assembler.toResponse(pagamento);

        assertNotNull(response);
        assertEquals(pagamento.getId(), response.id());
        assertEquals(pagamento.getOrcamentoId(), response.orcamentoId());
        assertEquals(pagamento.getOsId(), response.ordemServicoId());
        assertNull(response.qrCodePix());
        assertNull(response.qrCodeBase64());
        assertEquals("PENDENTE", response.status());
        assertNull(response.dataExpiracao());
        assertNull(response.dataPagamento());
    }

    @Test
    @DisplayName("Deve converter status para string")
    void deveConverterStatusParaString() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrcamentoId(UUID.randomUUID());
        pagamento.setOsId(UUID.randomUUID());
        pagamento.setStatus(StatusPagamento.APROVADO);

        PagamentoResponse response = assembler.toResponse(pagamento);

        assertEquals("APROVADO", response.status());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Pagamento é nulo")
    void deveLancarExcecaoQuandoPagamentoENulo() {
        assertThrows(NullPointerException.class, () -> assembler.toResponse(null));
    }

    @Test
    @DisplayName("Deve converter Pagamento com todos os campos preenchidos")
    void deveConverterPagamentoComTodosOsCamposPreenchidos() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrcamentoId(UUID.randomUUID());
        pagamento.setOsId(UUID.randomUUID());
        pagamento.setQrCodePix("pix-code-123");
        pagamento.setQrCodeBase64("base64-456");
        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataExpiracao(LocalDateTime.now().plusMinutes(30));
        pagamento.setDataPagamento(LocalDateTime.now());

        PagamentoResponse response = assembler.toResponse(pagamento);

        assertNotNull(response);
        assertEquals("pix-code-123", response.qrCodePix());
        assertEquals("base64-456", response.qrCodeBase64());
        assertEquals("APROVADO", response.status());
        assertNotNull(response.dataExpiracao());
        assertNotNull(response.dataPagamento());
    }

    private Pagamento createPagamento() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrcamentoId(UUID.randomUUID());
        pagamento.setOsId(UUID.randomUUID());
        pagamento.setQrCodePix("pix-code");
        pagamento.setQrCodeBase64("base64");
        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataExpiracao(LocalDateTime.now().plusMinutes(30));
        pagamento.setDataPagamento(LocalDateTime.now());
        return pagamento;
    }
}
