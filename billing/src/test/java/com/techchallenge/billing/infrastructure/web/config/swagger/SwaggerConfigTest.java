package com.techchallenge.billing.infrastructure.web.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - SwaggerConfig")
class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        swaggerConfig = new SwaggerConfig();
        ReflectionTestUtils.setField(swaggerConfig, "applicationName", "billing-service");
        ReflectionTestUtils.setField(swaggerConfig, "applicationVersion", "0.0.1-SNAPSHOT");
    }

    @Test
    @DisplayName("Deve criar bean OpenAPI")
    void deveCriarBeanOpenAPI() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertNotNull(openAPI);
    }

    @Test
    @DisplayName("Deve configurar servidor padrão")
    void deveConfigurarServidorPadrao() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());

        Server server = openAPI.getServers().get(0);
        assertEquals("/", server.getUrl());
        assertEquals("Default Server URL", server.getDescription());
    }

    @Test
    @DisplayName("Deve usar o context path do serviço como URL do servidor")
    void deveUsarContextPathComoUrlDoServidor() {
        ReflectionTestUtils.setField(swaggerConfig, "serverContextPath", "/v1/billing-service/");

        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertEquals("/v1/billing-service", openAPI.getServers().get(0).getUrl());
    }

    @Test
    @DisplayName("Deve configurar informações da API")
    void deveConfigurarInformacoesDaAPI() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        Info info = openAPI.getInfo();
        assertNotNull(info);
        assertEquals("billing-service API", info.getTitle());
        assertEquals("0.0.1-SNAPSHOT", info.getVersion());
        assertEquals("API REST para gestão de orçamentos e pagamentos", info.getDescription());
    }

    @Test
    @DisplayName("Deve configurar contato")
    void deveConfigurarContato() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        Contact contact = openAPI.getInfo().getContact();
        assertNotNull(contact);
        assertEquals("Tech Challenge SOAT FIAP", contact.getName());
        assertEquals("techchallenge@fiap.com.br", contact.getEmail());
    }

    @Test
    @DisplayName("Deve configurar licença")
    void deveConfigurarLicenca() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        License license = openAPI.getInfo().getLicense();
        assertNotNull(license);
        assertEquals("Apache 2.0", license.getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0.html", license.getUrl());
    }

    @Test
    @DisplayName("Deve configurar esquema de segurança JWT")
    void deveConfigurarEsquemaDeSegurancaJWT() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("bearerAuth"));

        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(securityScheme);
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
        assertEquals("Autenticação JWT", securityScheme.getDescription());
        assertEquals(List.of(new SecurityRequirement().addList("bearerAuth")), openAPI.getSecurity());
    }

    @Test
    @DisplayName("Deve configurar tags")
    void deveConfigurarTags() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertNotNull(openAPI.getTags());
        assertEquals(3, openAPI.getTags().size());

        List<String> tagNames = openAPI.getTags().stream().map(Tag::getName).toList();
        assertTrue(tagNames.contains("Orçamentos"));
        assertTrue(tagNames.contains("Pagamentos"));
        assertTrue(tagNames.contains("Webhooks"));
    }

    @Test
    @DisplayName("Deve configurar descrição das tags")
    void deveConfigurarDescricaoDasTags() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        Tag orcamentosTag = openAPI.getTags().stream()
                .filter(tag -> "Orçamentos".equals(tag.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(orcamentosTag);
        assertEquals("Gestão de orçamentos", orcamentosTag.getDescription());

        Tag pagamentosTag = openAPI.getTags().stream()
                .filter(tag -> "Pagamentos".equals(tag.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(pagamentosTag);
        assertEquals("Consulta de pagamentos PIX", pagamentosTag.getDescription());

        Tag webhooksTag = openAPI.getTags().stream()
                .filter(tag -> "Webhooks".equals(tag.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(webhooksTag);
        assertEquals("Notificações do Mercado Pago", webhooksTag.getDescription());
    }


}
