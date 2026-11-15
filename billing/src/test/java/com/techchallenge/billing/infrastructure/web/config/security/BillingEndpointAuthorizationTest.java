package com.techchallenge.billing.infrastructure.web.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.billing.application.gateway.ports.input.ConsultarOrcamentoInput;
import com.techchallenge.billing.application.gateway.ports.input.CriarOrcamentoInput;
import com.techchallenge.billing.application.gateway.ports.input.ListarOrcamentosInput;
import com.techchallenge.billing.application.gateway.ports.input.ListarPagamentosInput;
import com.techchallenge.billing.application.gateway.ports.input.ProcessarWebhookInput;
import com.techchallenge.billing.infrastructure.web.security.MercadoPagoWebhookSignatureValidator;
import com.techchallenge.billing.infrastructure.config.SpringDataWebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@WebMvcTest
@Import({SecurityConfig.class, SpringDataWebConfig.class})
class BillingEndpointAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CriarOrcamentoInput criarOrcamentoInput;

    @MockitoBean
    private ConsultarOrcamentoInput consultarOrcamentoInput;

    @MockitoBean
    private ListarOrcamentosInput listarOrcamentosInput;

    @MockitoBean
    private ListarPagamentosInput listarPagamentosInput;

    @MockitoBean
    private ProcessarWebhookInput processarWebhookInput;

    @MockitoBean
    private MercadoPagoWebhookSignatureValidator signatureValidator;

    @MockitoBean
    private ObjectMapper objectMapper;

    @Test
    void rejectsRequestsWithoutAdminAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/billings/orcamentos")
                        .param("clienteId", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsSwaggerEndpointsWithoutAuthentication() throws Exception {
        for (String path : List.of("/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs")) {
            int responseStatus = mockMvc.perform(get(path))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            assertNotEquals(401, responseStatus, path + " must be publicly accessible");
        }
    }

    @Test
    void rejectsAuthenticatedNonAdminUsers() throws Exception {
        mockMvc.perform(get("/api/v1/billings/orcamentos")
                        .param("clienteId", "00000000-0000-0000-0000-000000000001")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")))))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminRequests() throws Exception {
        when(listarOrcamentosInput.execute(any())).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/v1/billings/orcamentos")
                        .param("clienteId", "00000000-0000-0000-0000-000000000001")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().isOk());
    }

    /*
    @Test
    void allowsWebhookWithoutJwtButRequiresValidMercadoPagoSignature() throws Exception {
        when(signatureValidator.isValid(any(), eq("12345"), eq("valid-signature"), eq("request-123")))
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/billings/webhooks/mercadopago")
                        .param("data.id", "12345")
                        .header("x-signature", "valid-signature")
                        .header("x-request-id", "request-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "payment.updated",
                                  "type": "payment",
                                  "data": {"id": "12345"}
                                }
                                """))
                .andExpect(status().isOk());

        verify(processarWebhookInput).execute(any());
    }

     */
}
