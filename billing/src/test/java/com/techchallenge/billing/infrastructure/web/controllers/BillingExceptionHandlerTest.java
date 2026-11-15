package com.techchallenge.billing.infrastructure.web.controllers;

import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import com.techchallenge.billing.domain.exceptions.OrcamentoNaoEncontradoException;
import com.techchallenge.billing.domain.exceptions.PagamentoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Testes Unitários - BillingExceptionHandler")
class BillingExceptionHandlerTest {

    private BillingExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BillingExceptionHandler();
    }

    @Test
    @DisplayName("Deve tratar OrcamentoNaoEncontradoException com status 404")
    void deveTratarOrcamentoNaoEncontradoExceptionComStatus404() {
        UUID id = UUID.randomUUID();
        OrcamentoNaoEncontradoException exception = new OrcamentoNaoEncontradoException(id);

        ProblemDetail problemDetail = handler.notFound(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Orçamento não encontrado com ID: " + id, problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve tratar PagamentoNaoEncontradoException com status 404")
    void deveTratarPagamentoNaoEncontradoExceptionComStatus404() {
        UUID id = UUID.randomUUID();
        PagamentoNaoEncontradoException exception = new PagamentoNaoEncontradoException(id);

        ProblemDetail problemDetail = handler.notFound(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Pagamento não encontrado com ID: " + id, problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve tratar IntegracaoMercadoPagoException com status 502")
    void deveTratarIntegracaoMercadoPagoExceptionComStatus502() {
        IntegracaoMercadoPagoException exception = new IntegracaoMercadoPagoException("Erro de integração");

        ProblemDetail problemDetail = handler.integrationFailure(exception);

        assertEquals(HttpStatus.BAD_GATEWAY.value(), problemDetail.getStatus());
        assertTrue(problemDetail.getDetail().contains("Erro de integração"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException com status 400")
    void deveTratarIllegalArgumentExceptionComStatus400() {
        IllegalArgumentException exception = new IllegalArgumentException("Parâmetro inválido");

        ProblemDetail problemDetail = handler.badRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Parâmetro inválido", problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com status 400")
    void deveTratarMethodArgumentNotValidExceptionComStatus400() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getMessage()).thenReturn("Validation failed");

        ProblemDetail problemDetail = handler.badRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Validation failed", problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentTypeMismatchException com status 400")
    void deveTratarMethodArgumentTypeMismatchExceptionComStatus400() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getMessage()).thenReturn("Type mismatch");

        ProblemDetail problemDetail = handler.badRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Type mismatch", problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve ter RestControllerAdvice anotação")
    void deveTerRestControllerAdviceAnotacao() {
        assertTrue(handler.getClass().isAnnotationPresent(org.springframework.web.bind.annotation.RestControllerAdvice.class));
    }
}
