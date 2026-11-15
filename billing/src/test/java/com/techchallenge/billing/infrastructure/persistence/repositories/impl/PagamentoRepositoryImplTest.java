package com.techchallenge.billing.infrastructure.persistence.repositories.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techchallenge.billing.domain.models.Pagamento;
import com.techchallenge.billing.infrastructure.persistence.DynamoDbPersistence;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagamentoRepositoryImplTest {

    @Mock
    private DynamoDbPersistence dynamo;
    @Mock
    private Pagamento pagamento;
    private PagamentoRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new PagamentoRepositoryImpl(dynamo);
    }

    @Test
    void savesPaymentWithLookupIndexesAndPaymentIdUniqueness() {
        var id = UUID.randomUUID();
        var orcamentoId = UUID.randomUUID();
        var osId = UUID.randomUUID();
        when(pagamento.getId()).thenReturn(id);
        when(pagamento.getOrcamentoId()).thenReturn(orcamentoId);
        when(pagamento.getOsId()).thenReturn(osId);
        when(pagamento.getMercadoPagoPaymentId()).thenReturn("mp-123");
        when(dynamo.save("billing-pagamentos", "id", id.toString(), pagamento,
                Map.of("orcamentoId", orcamentoId.toString(), "osId", osId.toString(),
                        "mercadoPagoPaymentId", "mp-123"),
                Map.of("mercadoPagoPaymentId", "mp-123"))).thenReturn(pagamento);

        assertEquals(pagamento, repository.save(pagamento));
    }

    @Test
    void queriesByOrderServiceId() {
        var osId = UUID.randomUUID();
        when(dynamo.query("billing-pagamentos", "osId-index", "osId", osId.toString(),
                null, null, null, Pagamento.class)).thenReturn(List.of(pagamento));

        assertEquals(List.of(pagamento), repository.findByOsId(osId));
        verify(dynamo).query("billing-pagamentos", "osId-index", "osId", osId.toString(),
                null, null, null, Pagamento.class);
    }
}
