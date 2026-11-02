package com.techchallenge.billing.infrastructure.external;

import com.techchallenge.billing.infrastructure.security.ServiceJwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.startsWith;

class OrdemServicoClientTest {

    @Test
    void sendsServiceBearerTokenWhenFetchingOrder() {
        String jwtSecret = "mySuperSecretKeyForJWTTokenGenerationThatIsSecureEnough";
        String baseUrl = "http://localhost:8081";
        UUID osId = UUID.fromString("ee94df63-6130-4088-b61b-9ae2ff53b163");
        UUID clienteId = UUID.fromString("2129e8b1-d377-4eb4-8517-56f5d443a9b4");
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(baseUrl + "/v1/os/" + osId))
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
                new ServiceJwtTokenProvider(jwtSecret),
                baseUrl
        );

        var result = client.getOrdemServico(osId);

        assertEquals(osId, result.id());
        assertEquals(clienteId, result.clienteId());
        assertEquals("25.50", result.valorTotal().toPlainString());
        server.verify();
    }
}
