package com.techchallenge.billing.infrastructure.persistence.mappers;

import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.domain.models.valueobjects.StatusPagamento;
import com.techchallenge.billing.infrastructure.persistence.documents.PagamentoDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - PagamentoMongoMapper")
class PagamentoMongoMapperTest {

    private PagamentoMongoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PagamentoMongoMapper();
    }

    @Test
    @DisplayName("Deve converter Pagamento para PagamentoDocument")
    void deveConverterPagamentoParaPagamentoDocument() {
        Pagamento pagamento = createPagamento();

        PagamentoDocument document = mapper.toDocument(pagamento);

        assertNotNull(document);
        assertEquals(pagamento.getId(), document.getId());
        assertEquals(pagamento.getOrcamentoId(), document.getOrcamentoId());
        assertEquals(pagamento.getOsId(), document.getOsId());
        assertEquals(pagamento.getMercadoPagoPaymentId(), document.getMercadoPagoPaymentId());
        assertEquals(pagamento.getQrCodePix(), document.getQrCodePix());
        assertEquals(pagamento.getQrCodeBase64(), document.getQrCodeBase64());
        assertEquals(pagamento.getStatus(), document.getStatus());
        assertEquals(pagamento.getValorCobrado(), document.getValorCobrado());
        assertEquals(pagamento.getValorPago(), document.getValorPago());
        assertEquals(pagamento.getMotivoRejeicao(), document.getMotivoRejeicao());
        assertEquals(pagamento.getDataCriacao(), document.getDataCriacao());
        assertEquals(pagamento.getDataExpiracao(), document.getDataExpiracao());
        assertEquals(pagamento.getDataPagamento(), document.getDataPagamento());
        assertEquals(pagamento.getDataAtualizacao(), document.getDataAtualizacao());
        assertEquals(pagamento.getTentativasSincronizacao(), document.getTentativasSincronizacao());
        assertEquals(pagamento.getUltimaSincronizacaoMercadoPago(), document.getUltimaSincronizacaoMercadoPago());
    }

    @Test
    @DisplayName("Deve converter PagamentoDocument para Pagamento")
    void deveConverterPagamentoDocumentParaPagamento() {
        PagamentoDocument document = createPagamentoDocument();

        Pagamento pagamento = mapper.toDomain(document);

        assertNotNull(pagamento);
        assertEquals(document.getId(), pagamento.getId());
        assertEquals(document.getOrcamentoId(), pagamento.getOrcamentoId());
        assertEquals(document.getOsId(), pagamento.getOsId());
        assertEquals(document.getMercadoPagoPaymentId(), pagamento.getMercadoPagoPaymentId());
        assertEquals(document.getQrCodePix(), pagamento.getQrCodePix());
        assertEquals(document.getQrCodeBase64(), pagamento.getQrCodeBase64());
        assertEquals(document.getStatus(), pagamento.getStatus());
        assertEquals(document.getValorCobrado(), pagamento.getValorCobrado());
        assertEquals(document.getValorPago(), pagamento.getValorPago());
        assertEquals(document.getMotivoRejeicao(), pagamento.getMotivoRejeicao());
        assertEquals(document.getDataCriacao(), pagamento.getDataCriacao());
        assertEquals(document.getDataExpiracao(), pagamento.getDataExpiracao());
        assertEquals(document.getDataPagamento(), pagamento.getDataPagamento());
        assertEquals(document.getDataAtualizacao(), pagamento.getDataAtualizacao());
        assertEquals(document.getTentativasSincronizacao(), pagamento.getTentativasSincronizacao());
        assertEquals(document.getUltimaSincronizacaoMercadoPago(), pagamento.getUltimaSincronizacaoMercadoPago());
    }

    @Test
    @DisplayName("Deve lançar exceção quando Pagamento é nulo ao converter para document")
    void deveLancarExcecaoQuandoPagamentoENuloAoConverterParaDocument() {
        assertThrows(NullPointerException.class, () -> mapper.toDocument(null));
    }

    @Test
    @DisplayName("Deve lançar exceção quando PagamentoDocument é nulo ao converter para domain")
    void deveLancarExcecaoQuandoPagamentoDocumentENuloAoConverterParaDomain() {
        assertThrows(NullPointerException.class, () -> mapper.toDomain(null));
    }

    @Test
    @DisplayName("Deve manter consistência em conversão bidirecional")
    void deveManterConsistenciaEmConversaoBidirecional() {
        Pagamento original = createPagamento();

        PagamentoDocument document = mapper.toDocument(original);
        Pagamento restored = mapper.toDomain(document);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getOrcamentoId(), restored.getOrcamentoId());
        assertEquals(original.getOsId(), restored.getOsId());
        assertEquals(original.getMercadoPagoPaymentId(), restored.getMercadoPagoPaymentId());
        assertEquals(original.getQrCodePix(), restored.getQrCodePix());
        assertEquals(original.getQrCodeBase64(), restored.getQrCodeBase64());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getValorCobrado(), restored.getValorCobrado());
        assertEquals(original.getValorPago(), restored.getValorPago());
        assertEquals(original.getMotivoRejeicao(), restored.getMotivoRejeicao());
        assertEquals(original.getDataCriacao(), restored.getDataCriacao());
        assertEquals(original.getDataExpiracao(), restored.getDataExpiracao());
        assertEquals(original.getDataPagamento(), restored.getDataPagamento());
        assertEquals(original.getDataAtualizacao(), restored.getDataAtualizacao());
        assertEquals(original.getTentativasSincronizacao(), restored.getTentativasSincronizacao());
        assertEquals(original.getUltimaSincronizacaoMercadoPago(), restored.getUltimaSincronizacaoMercadoPago());
    }

    @Test
    @DisplayName("Deve converter Pagamento com campos nulos opcionais")
    void deveConverterPagamentoComCamposNulosOpcionais() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrcamentoId(UUID.randomUUID());
        pagamento.setOsId(UUID.randomUUID());
        pagamento.setMercadoPagoPaymentId("mp-123");
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValorCobrado(new BigDecimal("100.00"));
        pagamento.setDataCriacao(LocalDateTime.now());

        PagamentoDocument document = mapper.toDocument(pagamento);

        assertNotNull(document);
        assertNull(document.getQrCodePix());
        assertNull(document.getQrCodeBase64());
        assertNull(document.getValorPago());
        assertNull(document.getMotivoRejeicao());
        assertNull(document.getDataExpiracao());
        assertNull(document.getDataPagamento());
        assertNull(document.getDataAtualizacao());
        assertEquals(0, document.getTentativasSincronizacao());
        assertNull(document.getUltimaSincronizacaoMercadoPago());
    }

    private Pagamento createPagamento() {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setOrcamentoId(UUID.randomUUID());
        pagamento.setOsId(UUID.randomUUID());
        pagamento.setMercadoPagoPaymentId("mp-payment-123");
        pagamento.setQrCodePix("pix-code");
        pagamento.setQrCodeBase64("base64");
        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setValorCobrado(new BigDecimal("100.00"));
        pagamento.setValorPago(new BigDecimal("100.00"));
        pagamento.setMotivoRejeicao(null);
        pagamento.setDataCriacao(LocalDateTime.now());
        pagamento.setDataExpiracao(LocalDateTime.now().plusMinutes(30));
        pagamento.setDataPagamento(LocalDateTime.now().plusMinutes(5));
        pagamento.setDataAtualizacao(LocalDateTime.now().plusMinutes(5));
        pagamento.setTentativasSincronizacao(2);
        pagamento.setUltimaSincronizacaoMercadoPago(LocalDateTime.now().plusMinutes(5));
        return pagamento;
    }

    private PagamentoDocument createPagamentoDocument() {
        PagamentoDocument document = new PagamentoDocument();
        document.setId(UUID.randomUUID());
        document.setOrcamentoId(UUID.randomUUID());
        document.setOsId(UUID.randomUUID());
        document.setMercadoPagoPaymentId("mp-payment-456");
        document.setQrCodePix("pix-code-2");
        document.setQrCodeBase64("base64-2");
        document.setStatus(StatusPagamento.RECUSADO);
        document.setValorCobrado(new BigDecimal("50.00"));
        document.setValorPago(null);
        document.setMotivoRejeicao("Cartão inválido");
        document.setDataCriacao(LocalDateTime.now());
        document.setDataExpiracao(LocalDateTime.now().plusMinutes(30));
        document.setDataPagamento(null);
        document.setDataAtualizacao(LocalDateTime.now());
        document.setTentativasSincronizacao(1);
        document.setUltimaSincronizacaoMercadoPago(LocalDateTime.now());
        return document;
    }
}
