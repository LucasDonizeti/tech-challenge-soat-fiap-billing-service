package com.techchallenge.billing.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.paginators.QueryIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DynamoDbPersistenceTest {

    @Mock
    private DynamoDbClient client;
    @Mock
    private QueryIterable queryIterable;
    private DynamoDbPersistence persistence;

    @BeforeEach
    void setUp() {
        persistence = new DynamoDbPersistence(client, new ObjectMapper());
    }

    @Test
    void savesPayloadAndUniqueReservationAtomically() {
        var entity = new Payload("value");
        persistence.save("billing-pagamentos", "id", "payment-1", entity,
                Map.of("osId", "os-1"),
                Map.of("mercadoPagoPaymentId", "mp-1"));

        var request = ArgumentCaptor.forClass(TransactWriteItemsRequest.class);
        verify(client).transactWriteItems(request.capture());
        var writes = request.getValue().transactItems();
        assertEquals(2, writes.size());
        assertEquals("{\"name\":\"value\"}", writes.get(0).put().item().get("payload").s());
        assertEquals("unique#mercadoPagoPaymentId#mp-1", writes.get(1).put().item().get("id").s());
        assertEquals("payment-1", writes.get(1).put().item().get("ownerId").s());
        assertTrue(writes.get(1).put().conditionExpression().contains("attribute_not_exists"));
    }

    @Test
    void savesPayloadWithoutUniqueReservation() {
        var entity = new Payload("value");
        persistence.save("billing-pagamentos", "id", "payment-1", entity,
                Map.of("osId", "os-1"),
                Map.of());

        verify(client).putItem(any(PutItemRequest.class));
        verify(client, never()).transactWriteItems(any(TransactWriteItemsRequest.class));
    }

    @Test
    void readsAndDeserializesPayload() {
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder()
                        .item(Map.of("payload", AttributeValue.builder()
                                .s("{\"name\":\"stored\"}")
                                .build()))
                        .build());

        Optional<Payload> payload = persistence.get("billing-pagamentos", "id", "payment-1", Payload.class);

        assertEquals(Optional.of(new Payload("stored")), payload);
    }

    @Test
    void returnsEmptyWhenItemNotFound() {
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(Map.of()).build());

        Optional<Payload> payload = persistence.get("billing-pagamentos", "id", "payment-1", Payload.class);

        assertTrue(payload.isEmpty());
    }

    @Test
    void throwsExceptionWhenPayloadAttributeMissing() {
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder()
                        .item(Map.of("id", AttributeValue.builder().s("payment-1").build()))
                        .build());

        assertThrows(IllegalStateException.class, () ->
                persistence.get("billing-pagamentos", "id", "payment-1", Payload.class)
        );
    }

    @Test
    void throwsExceptionWhenDeserializationFails() {
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder()
                        .item(Map.of("payload", AttributeValue.builder()
                                .s("invalid json")
                                .build()))
                        .build());

        assertThrows(IllegalStateException.class, () ->
                persistence.get("billing-pagamentos", "id", "payment-1", Payload.class)
        );
    }

    @Test
    void findUniqueOwnerReturnsOwnerWhenExists() {
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder()
                        .item(Map.of("ownerId", AttributeValue.builder().s("payment-1").build()))
                        .build());

        Optional<String> owner = persistence.findUniqueOwner(
                "billing-pagamentos",
                "id",
                "mercadoPagoPaymentId",
                "mp-123"
        );

        assertTrue(owner.isPresent());
        assertEquals("payment-1", owner.get());
    }

    @Test
    void findUniqueOwnerReturnsEmptyWhenNotFound() {
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(Map.of()).build());

        Optional<String> owner = persistence.findUniqueOwner(
                "billing-pagamentos",
                "id",
                "mercadoPagoPaymentId",
                "mp-123"
        );

        assertTrue(owner.isEmpty());
    }

    @Test
    void findUniqueOwnerReturnsEmptyForBlankValue() {
        Optional<String> owner = persistence.findUniqueOwner(
                "billing-pagamentos",
                "id",
                "mercadoPagoPaymentId",
                ""
        );

        assertTrue(owner.isEmpty());
    }

    @Test
    void compareJsonPropertyWithNumbers() {
        var left = new PayloadWithNumber(10);
        var right = new PayloadWithNumber(20);

        int result = persistence.compareJsonProperty(left, right, "value");

        assertTrue(result < 0);
    }

    @Test
    void compareJsonPropertyWithStrings() {
        var left = new PayloadWithString("apple");
        var right = new PayloadWithString("banana");

        int result = persistence.compareJsonProperty(left, right, "name");

        assertTrue(result < 0);
    }

    @Test
    void compareJsonPropertyWithNullLeft() {
        var left = new PayloadWithString(null);
        var right = new PayloadWithString("value");

        int result = persistence.compareJsonProperty(left, right, "name");

        assertTrue(result < 0);
    }

    @Test
    void compareJsonPropertyWithNullRight() {
        var left = new PayloadWithString("value");
        var right = new PayloadWithString(null);

        int result = persistence.compareJsonProperty(left, right, "name");

        assertTrue(result > 0);
    }

    @Test
    void compareJsonPropertyWithBothNull() {
        var left = new PayloadWithString(null);
        var right = new PayloadWithString(null);

        int result = persistence.compareJsonProperty(left, right, "name");

        assertEquals(0, result);
    }

    @Test
    void throwsExceptionWhenSerializationFails() {
        var entity = new Object() {
            private final Object recursive = this;
        };

        assertThrows(IllegalStateException.class, () ->
                persistence.save("table", "id", "key", entity, Map.of(), Map.of())
        );
    }

    private record Payload(String name) {
    }

    private record PayloadWithNumber(int value) {
    }

    private record PayloadWithString(String name) {
    }
}
