package com.techchallenge.billing.infrastructure.external;

import com.techchallenge.billing.domain.exceptions.IntegracaoMercadoPagoException;
import com.techchallenge.billing.infrastructure.security.ServiceJwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.hamcrest.Matchers.startsWith;

class OrdemServicoClientTest {

    private static final String JWT_SECRET = "mySuperSecretKeyForJWTTokenGenerationThatIsSecureEnough";
    private static final String BASE_URL = "http://localhost:8081";

    @Test
    void sendsServiceBearerTokenWhenFetchingOrder() {
        UUID osId = UUID.fromString("ee94df63-6130-4088-b61b-9ae2ff53b163");
        UUID clienteId = UUID.fromString("2129e8b1-d377-4eb4-8517-56f5d443a9b4");
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "ee94df63-6130-4088-b61b-9ae2ff53b163",
                          "cliente": {
                            "id": "2129e8b1-d377-4eb4-8517-56f5d443a9b4"
                          },
                          "valorTotal": 25.50,
                          "itensServico": []
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertEquals(osId, result.id());
        assertEquals(clienteId, result.clienteId());
        assertEquals("25.50", result.valorTotal().toPlainString());
        server.verify();
    }

    @Test
    void throwsExceptionWhenOsIdIsNull() {
        RestTemplate restTemplate = new RestTemplate();
        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrdemServico(null)
        );
    }

    @Test
    void throwsExceptionWhenResponseIsNull() {
        UUID osId = UUID.randomUUID();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrdemServico(osId)
        );
        server.verify();
    }

    @Test
    void throwsExceptionWhenClienteIsNull() {
        UUID osId = UUID.randomUUID();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "valorTotal": 25.50,
                          "itensServico": []
                        }
                        """, osId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrdemServico(osId)
        );
        server.verify();
    }

    @Test
    void throwsExceptionWhenClienteIdIsNull() {
        UUID osId = UUID.randomUUID();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "cliente": {},
                          "valorTotal": 25.50,
                          "itensServico": []
                        }
                        """, osId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrdemServico(osId)
        );
        server.verify();
    }

    @Test
    void throwsExceptionWhenRestClientFails() {
        UUID osId = UUID.randomUUID();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrdemServico(osId)
        );
        server.verify();
    }

    @Test
    void throwsExceptionWhenConnectionFails() {
        UUID osId = UUID.randomUUID();
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    throw new ResourceAccessException("Connection refused");
                });

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        assertThrows(IntegracaoMercadoPagoException.class, () ->
                client.getOrdemServico(osId)
        );
    }

    @Test
    void returnsOrdemServicoDataWithCorrectFields() {
        UUID osId = UUID.fromString("ee94df63-6130-4088-b61b-9ae2ff53b163");
        UUID clienteId = UUID.fromString("2129e8b1-d377-4eb4-8517-56f5d443a9b4");

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "ee94df63-6130-4088-b61b-9ae2ff53b163",
                          "valorTotal": 150.75,
                          "status": "ABERTA",
                          "cliente": {
                            "id": "2129e8b1-d377-4eb4-8517-56f5d443a9b4",
                            "nome": "João Silva",
                            "cpf": "12345678900",
                            "email": "joao@example.com",
                            "tipo": "PF"
                          },
                          "itensServico": [
                            {
                              "servico": {
                                "nome": "Troca de Óleo"
                              },
                              "preco": 80.00
                            },
                            {
                              "servico": {
                                "nome": "Alinhamento"
                              },
                              "preco": 70.75
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertEquals(osId, result.id());
        assertEquals(clienteId, result.clienteId());
        assertEquals("150.75", result.valorTotal().toPlainString());
        assertEquals("joao@example.com", result.payerEmail());
        assertEquals("CPF", result.payerIdentificationType());
        assertEquals("12345678900", result.payerIdentificationNumber());
        // Should only include first 2 services in description
        assertEquals("Troca de Óleo, Alinhamento", result.descricaoServicos());
        server.verify();
    }

    @Test
    void returnsEmptyDescriptionWhenNoServices() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "cliente": {
                            "id": "%s"
                          },
                          "valorTotal": 100.00,
                          "itensServico": []
                        }
                        """, osId, clienteId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertEquals("Serviços diversos", result.descricaoServicos());
        server.verify();
    }

    @Test
    void mapsPJClienteToCNPJ() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "cliente": {
                            "id": "%s",
                            "tipo": "PJ",
                            "cnpj": "12345678000199",
                            "cpf": "12345678900",
                            "email": "empresa@example.com"
                          },
                          "valorTotal": 500.00,
                          "itensServico": []
                        }
                        """, osId, clienteId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertEquals("CNPJ", result.payerIdentificationType());
        assertEquals("12345678000199", result.payerIdentificationNumber());
        server.verify();
    }

    @Test
    void mapsPFClienteToCPF() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "cliente": {
                            "id": "%s",
                            "tipo": "PF",
                            "cnpj": null,
                            "cpf": "12345678900",
                            "email": "joao@example.com"
                          },
                          "valorTotal": 250.00,
                          "itensServico": []
                        }
                        """, osId, clienteId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertEquals("CPF", result.payerIdentificationType());
        assertEquals("12345678900", result.payerIdentificationNumber());
        server.verify();
    }

    @Test
    void prefersCNPJOverCPFWhenAvailable() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "cliente": {
                            "id": "%s",
                            "tipo": "PJ",
                            "cnpj": "98765432000199",
                            "cpf": "12345678900",
                            "email": "empresa@example.com"
                          },
                          "valorTotal": 750.00,
                          "itensServico": []
                        }
                        """, osId, clienteId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertEquals("98765432000199", result.payerIdentificationNumber());
        server.verify();
    }

    @Test
    void returnsNullEmailWhenClienteHasNoEmail() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "cliente": {
                            "id": "%s"
                          },
                          "valorTotal": 100.00,
                          "itensServico": []
                        }
                        """, osId, clienteId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertNull(result.payerEmail());
        server.verify();
    }

    @Test
    void formatsServiceDescriptionWithMultipleServices() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "cliente": {
                            "id": "%s"
                          },
                          "valorTotal": 300.00,
                          "itensServico": [
                            {
                              "servico": {
                                "nome": "Serviço A"
                              }
                            },
                            {
                              "servico": {
                                "nome": "Serviço B"
                              }
                            }
                          ]
                        }
                        """, osId, clienteId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertEquals("Serviço A, Serviço B", result.descricaoServicos());
        server.verify();
    }

    @Test
    void limitsServiceDescriptionToThreeServices() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(BASE_URL + "/v1/os/" + osId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        String.format("""
                        {
                          "id": "%s",
                          "cliente": {
                            "id": "%s"
                          },
                          "valorTotal": 400.00,
                          "itensServico": [
                            {
                              "servico": {
                                "nome": "Serviço 1"
                              }
                            },
                            {
                              "servico": {
                                "nome": "Serviço 2"
                              }
                            },
                            {
                              "servico": {
                                "nome": "Serviço 3"
                              }
                            },
                            {
                              "servico": {
                                "nome": "Serviço 4"
                              }
                            }
                          ]
                        }
                        """, osId, clienteId),
                        MediaType.APPLICATION_JSON
                ));

        OrdemServicoClient client = new OrdemServicoClient(
                restTemplate,
                new ServiceJwtTokenProvider(JWT_SECRET),
                BASE_URL
        );

        var result = client.getOrdemServico(osId);

        assertEquals("Serviço 1, Serviço 2, Serviço 3", result.descricaoServicos());
        server.verify();
    }
}
