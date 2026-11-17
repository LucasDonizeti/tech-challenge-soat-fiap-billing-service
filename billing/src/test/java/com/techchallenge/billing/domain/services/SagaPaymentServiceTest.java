package com.techchallenge.billing.domain.services;

import com.techchallenge.billing.domain.repositories.SagaContextRepository;
import com.techchallenge.billing.application.gateway.ports.output.SagaEventPublisher;
import com.techchallenge.billing.domain.events.SagaPaymentExpiredEvent;
import com.techchallenge.billing.domain.events.SagaPaymentInitiatedEvent;
import com.techchallenge.billing.domain.models.SagaContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Testes - SagaPaymentService")
@ExtendWith(MockitoExtension.class)
class SagaPaymentServiceTest {

    @Mock
    private SagaContextRepository sagaContextRepository;

    @Mock
    private SagaEventPublisher sagaEventPublisher;

    private SagaPaymentService service;

    @BeforeEach
    void setUp() {
        service = new SagaPaymentService(sagaContextRepository, sagaEventPublisher);
    }

    @Test
    @DisplayName("Deve iniciar nova saga e publicar evento de iniciação")
    void deveIniciarNovaSaga() {
        // Arrange
        UUID ordemServicoId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        UUID orcamentoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        String mercadoPagoPaymentId = "123456789";
        String qrCodePix = "00020126580014com.mercadolibre...";
        String qrCodeBase64 = "iVBORw0KGgoAAAA...";
        BigDecimal valorCobrado = BigDecimal.valueOf(150.50);
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(30);

        when(sagaContextRepository.findByOrdemServicoIdAndStatusPendente(ordemServicoId))
                .thenReturn(Optional.empty());
        
        when(sagaContextRepository.save(any(SagaContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SagaContext result = service.initiateSaga(
                pagamentoId,
                orcamentoId,
                ordemServicoId,
                clienteId,
                mercadoPagoPaymentId,
                qrCodePix,
                qrCodeBase64,
                valorCobrado,
                dataExpiracao
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.getSagaId());
        assertEquals(ordemServicoId, result.getOrdemServicoId());
        assertEquals("PENDENTE", result.getStatus());
        
        // Verificar que evento foi publicado
        ArgumentCaptor<SagaPaymentInitiatedEvent> eventCaptor = 
                ArgumentCaptor.forClass(SagaPaymentInitiatedEvent.class);
        verify(sagaEventPublisher).publishSagaPaymentInitiated(eventCaptor.capture());
        
        SagaPaymentInitiatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(result.getSagaId(), publishedEvent.getSagaId());
        assertEquals(ordemServicoId, publishedEvent.getOrdemServicoId());
    }

    @Test
    @DisplayName("Não deve criar nova saga se já existe PIX pendente")
    void naoDeveCriarNovaSagaSePendente() {
        // Arrange
        UUID ordemServicoId = UUID.randomUUID();
        UUID existingSagaId = UUID.randomUUID();
        
        SagaContext existingSaga = SagaContext.builder()
                .sagaId(existingSagaId)
                .ordemServicoId(ordemServicoId)
                .status("PENDENTE")
                .build();
        
        when(sagaContextRepository.findByOrdemServicoIdAndStatusPendente(ordemServicoId))
                .thenReturn(Optional.of(existingSaga));

        // Act
        SagaContext result = service.initiateSaga(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ordemServicoId,
                UUID.randomUUID(),
                "123456789",
                "qrcode",
                "base64",
                BigDecimal.valueOf(150.50),
                LocalDateTime.now().plusMinutes(30)
        );

        // Assert
        assertEquals(existingSagaId, result.getSagaId());
        
        // Não deve publicar novo evento
        verify(sagaEventPublisher, never()).publishSagaPaymentInitiated(any());
    }

    @Test
    @DisplayName("Deve confirmar saga com status APROVADO")
    void deveConfirmarSagaAprovada() {
        // Arrange
        UUID pagamentoId = UUID.randomUUID();
        UUID sagaId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        
        SagaContext sagaContext = SagaContext.builder()
                .sagaId(sagaId)
                .pagamentoId(pagamentoId)
                .ordemServicoId(ordemServicoId)
                .status("PENDENTE")
                .dados(java.util.Map.of("clienteId", UUID.randomUUID()))
                .build();
        
        when(sagaContextRepository.findAllByPagamentoId(pagamentoId))
                .thenReturn(java.util.List.of(sagaContext));
        
        when(sagaContextRepository.save(any(SagaContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.confirmSagaPayment(
                pagamentoId,
                "APROVADO",
                BigDecimal.valueOf(150.50),
                LocalDateTime.now(),
                null
        );

        // Assert
        ArgumentCaptor<SagaContext> contextCaptor = ArgumentCaptor.forClass(SagaContext.class);
        verify(sagaContextRepository, atLeastOnce()).save(contextCaptor.capture());
        
        SagaContext savedContext = contextCaptor.getValue();
        assertEquals("APROVADA", savedContext.getStatus());
        
    }

    @Test
    @DisplayName("Deve atualizar sagas duplicadas sem publicar evento de transição concorrente")
    void deveAtualizarSagasDuplicadasSemPublicarEventoDeTransicao() {
        UUID pagamentoId = UUID.randomUUID();
        SagaContext first = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .pagamentoId(pagamentoId)
                .ordemServicoId(UUID.randomUUID())
                .status("PENDENTE")
                .criadoEm(LocalDateTime.now().minusMinutes(1))
                .dados(java.util.Map.of("clienteId", UUID.randomUUID()))
                .build();
        SagaContext duplicate = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .pagamentoId(pagamentoId)
                .ordemServicoId(first.getOrdemServicoId())
                .status("PENDENTE")
                .criadoEm(LocalDateTime.now())
                .dados(java.util.Map.of("clienteId", UUID.randomUUID()))
                .build();
        when(sagaContextRepository.findAllByPagamentoId(pagamentoId))
                .thenReturn(java.util.List.of(first, duplicate));
        when(sagaContextRepository.save(any(SagaContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmSagaPayment(
                pagamentoId, "APROVADO", BigDecimal.TEN, LocalDateTime.now(), null);

        assertEquals("APROVADA", first.getStatus());
        assertEquals("APROVADA", duplicate.getStatus());
        verify(sagaContextRepository, times(2)).save(any(SagaContext.class));

        service.confirmSagaPayment(
                pagamentoId, "APROVADO", BigDecimal.TEN, LocalDateTime.now(), null);

        verify(sagaContextRepository, times(2)).save(any(SagaContext.class));
    }

    @Test
    @DisplayName("Deve processar cancelamento como resultado final da saga")
    void deveConfirmarSagaCancelada() {
        UUID pagamentoId = UUID.randomUUID();
        SagaContext saga = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .pagamentoId(pagamentoId)
                .ordemServicoId(UUID.randomUUID())
                .status("PENDENTE")
                .dados(Collections.emptyMap())
                .build();
        when(sagaContextRepository.findAllByPagamentoId(pagamentoId))
                .thenReturn(java.util.List.of(saga));
        when(sagaContextRepository.save(any(SagaContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmSagaPayment(pagamentoId, "CANCELADO", BigDecimal.ZERO, LocalDateTime.now(), "cancelado");

        assertEquals("CANCELADA", saga.getStatus());
    }

    @Test
    @DisplayName("Deve processar sagas expiradas")
    void deveProcessarSagasExpiradas() {
        // Arrange
        UUID sagaId1 = UUID.randomUUID();
        UUID sagaId2 = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime tempoExpiracao = agora.minusMinutes(5);

        SagaContext saga1 = SagaContext.builder()
                .sagaId(sagaId1)
                .ordemServicoId(UUID.randomUUID())
                .pagamentoId(UUID.randomUUID())
                .mercadoPagoPaymentId("1111")
                .status("INICIADA")
                .dataExpiracao(tempoExpiracao)
                .build();
        
        SagaContext saga2 = SagaContext.builder()
                .sagaId(sagaId2)
                .ordemServicoId(UUID.randomUUID())
                .pagamentoId(UUID.randomUUID())
                .mercadoPagoPaymentId("2222")
                .status("INICIADA")
                .dataExpiracao(tempoExpiracao)
                .build();
        
        when(sagaContextRepository.findExpiredSagas(any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(saga1, saga2));
        
        when(sagaContextRepository.save(any(SagaContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.processExpiredSagas();

        // Assert
        // Deve atualizar status de ambas as sagas para EXPIRADA
        ArgumentCaptor<SagaContext> contextCaptor = ArgumentCaptor.forClass(SagaContext.class);
        verify(sagaContextRepository, atLeastOnce()).save(contextCaptor.capture());
        
        // Deve publicar 2 eventos de expiração
        ArgumentCaptor<SagaPaymentExpiredEvent> eventCaptor = 
                ArgumentCaptor.forClass(SagaPaymentExpiredEvent.class);
        verify(sagaEventPublisher, times(2)).publishSagaPaymentExpired(eventCaptor.capture());
        
        java.util.List<SagaPaymentExpiredEvent> publishedEvents = eventCaptor.getAllValues();
        assertTrue(publishedEvents.stream().anyMatch(e -> e.getSagaId().equals(sagaId1)));
        assertTrue(publishedEvents.stream().anyMatch(e -> e.getSagaId().equals(sagaId2)));
    }

    @Test
    @DisplayName("Deve verificar se existe PIX pendente para ordem")
    void deveVerificarPixPendente() {
        // Arrange
        UUID ordemServicoId = UUID.randomUUID();
        SagaContext saga = SagaContext.builder()
                .sagaId(UUID.randomUUID())
                .status("PENDENTE")
                .build();
        
        when(sagaContextRepository.findByOrdemServicoIdAndStatusPendente(ordemServicoId))
                .thenReturn(Optional.of(saga));

        // Act
        boolean hasPending = service.hasPendingPixForOrdemServico(ordemServicoId);

        // Assert
        assertTrue(hasPending);
        verify(sagaContextRepository).findByOrdemServicoIdAndStatusPendente(ordemServicoId);
    }

    @Test
    @DisplayName("Deve retornar false se não existe PIX pendente")
    void deveRetornarFalseSemPixPendente() {
        // Arrange
        UUID ordemServicoId = UUID.randomUUID();
        
        when(sagaContextRepository.findByOrdemServicoIdAndStatusPendente(ordemServicoId))
                .thenReturn(Optional.empty());

        // Act
        boolean hasPending = service.hasPendingPixForOrdemServico(ordemServicoId);

        // Assert
        assertFalse(hasPending);
    }

    @Test
    @DisplayName("Deve recuperar contexto de saga por ID")
    void deveRecuperarSagaContextPorId() {
        // Arrange
        UUID sagaId = UUID.randomUUID();
        SagaContext saga = SagaContext.builder()
                .sagaId(sagaId)
                .status("APROVADA")
                .build();
        
        when(sagaContextRepository.findBySagaId(sagaId))
                .thenReturn(Optional.of(saga));

        // Act
        Optional<SagaContext> result = service.getSagaContext(sagaId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(sagaId, result.get().getSagaId());
        assertEquals("APROVADA", result.get().getStatus());
    }
}
