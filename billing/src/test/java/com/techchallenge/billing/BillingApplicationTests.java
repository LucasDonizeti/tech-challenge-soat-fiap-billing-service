package com.techchallenge.billing;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Testes da aplicação Billing
 * 
 * NOTA: O teste de contexto é desabilitado pois carrega toda a aplicação
 * com dependências de MongoDB que podem não estar disponíveis em CI/CD.
 * 
 * Use testes específicos por componente em vez deste teste de integração.
 */
@SpringBootTest
@ActiveProfiles("test")

class BillingApplicationTests {



}
