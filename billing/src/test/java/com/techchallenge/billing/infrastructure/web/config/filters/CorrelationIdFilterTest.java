package com.techchallenge.billing.infrastructure.web.config.filters;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void propagatesIncomingCorrelationIdAndClearsMdc() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "request-correlation-id");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertEquals("request-correlation-id", MDC.get(CorrelationIdFilter.MDC_KEY));
        });

        assertEquals("request-correlation-id", response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    @Test
    void generatesCorrelationIdWhenRequestDoesNotProvideOne() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        assertNotNull(UUID.fromString(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)));
    }
}
