package com.techchallenge.billing.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

import org.springframework.stereotype.Component;

@Component
public class DynamoDbPersistence {

    private static final String PAYLOAD_ATTRIBUTE = "payload";
    private final DynamoDbClient client;
    private final ObjectMapper objectMapper;

    public DynamoDbPersistence(DynamoDbClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<T> get(String table, String keyName, String key, Class<T> type) {
        var result = client.getItem(GetItemRequest.builder()
                .tableName(table)
                .key(Map.of(keyName, stringAttribute(key)))
                .consistentRead(true)
                .build()).item();
        return result.isEmpty() ? Optional.empty() : Optional.of(readPayload(result, type));
    }

    public Optional<String> findUniqueOwner(String table, String keyName, String attributeName, String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String uniqueKey = "unique#" + attributeName + "#" + value;
        var result = client.getItem(GetItemRequest.builder()
                .tableName(table)
                .key(Map.of(keyName, stringAttribute(uniqueKey)))
                .consistentRead(true)
                .build()).item();
        return Optional.ofNullable(result.get("ownerId")).map(AttributeValue::s);
    }

    public <T> T save(
            String table,
            String keyName,
            String key,
            T value,
            Map<String, String> indexAttributes,
            Map<String, String> uniqueAttributes
    ) {
        var item = new HashMap<String, AttributeValue>();
        item.put(keyName, stringAttribute(key));
        item.put(PAYLOAD_ATTRIBUTE, stringAttribute(writePayload(value)));
        indexAttributes.forEach((name, attributeValue) -> {
            if (attributeValue != null && !attributeValue.isBlank()) {
                item.put(name, stringAttribute(attributeValue));
            }
        });

        if (uniqueAttributes.isEmpty()) {
            client.putItem(PutItemRequest.builder().tableName(table).item(item).build());
            return value;
        }

        var writes = new ArrayList<TransactWriteItem>();
        writes.add(TransactWriteItem.builder()
                .put(Put.builder().tableName(table).item(item).build())
                .build());
        uniqueAttributes.forEach((attributeName, attributeValue) -> {
            if (attributeValue != null && !attributeValue.isBlank()) {
                String sentinelKey = "unique#" + attributeName + "#" + attributeValue;
                var sentinel = Map.of(
                        keyName, stringAttribute(sentinelKey),
                        "ownerId", stringAttribute(key)
                );
                writes.add(TransactWriteItem.builder()
                        .put(Put.builder()
                                .tableName(table)
                                .item(sentinel)
                                .conditionExpression("attribute_not_exists(#key) OR #owner = :owner")
                                .expressionAttributeNames(Map.of("#key", keyName, "#owner", "ownerId"))
                                .expressionAttributeValues(Map.of(":owner", stringAttribute(key)))
                                .build())
                        .build());
            }
        });
        client.transactWriteItems(TransactWriteItemsRequest.builder().transactItems(writes).build());
        return value;
    }

    public <T> List<T> query(
            String table,
            String indexName,
            String partitionKey,
            String partitionValue,
            String sortKey,
            String sortOperator,
            String sortValue,
            Class<T> type
    ) {
        String expression = "#partition = :partition";
        var names = new HashMap<String, String>();
        names.put("#partition", partitionKey);
        var values = new HashMap<String, AttributeValue>();
        values.put(":partition", stringAttribute(partitionValue));
        if (sortKey != null && sortValue != null) {
            expression += " AND #sort " + sortOperator + " :sort";
            names.put("#sort", sortKey);
            values.put(":sort", stringAttribute(sortValue));
        }

        var request = QueryRequest.builder()
                .tableName(table)
                .indexName(indexName)
                .keyConditionExpression(expression)
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build();
        var result = new ArrayList<T>();
        client.queryPaginator(request).items().forEach(item -> result.add(readPayload(item, type)));
        return result;
    }

    public int compareJsonProperty(Object left, Object right, String property) {
        return compareJsonProperty(left, right, property, false);
    }

    public int compareJsonProperty(Object left, Object right, String property, boolean ignoreCase) {
        JsonNode leftValue = objectMapper.valueToTree(left).get(property);
        JsonNode rightValue = objectMapper.valueToTree(right).get(property);
        if (leftValue == null || leftValue.isNull()) {
            return rightValue == null || rightValue.isNull() ? 0 : -1;
        }
        if (rightValue == null || rightValue.isNull()) {
            return 1;
        }
        if (leftValue.isNumber() && rightValue.isNumber()) {
            return leftValue.decimalValue().compareTo(rightValue.decimalValue());
        }
        String leftText = leftValue.asText();
        String rightText = rightValue.asText();
        return ignoreCase ? leftText.compareToIgnoreCase(rightText) : leftText.compareTo(rightText);
    }

    private String writePayload(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize DynamoDB item payload", exception);
        }
    }

    private <T> T readPayload(Map<String, AttributeValue> item, Class<T> type) {
        var payload = item.get(PAYLOAD_ATTRIBUTE);
        if (payload == null || payload.s() == null) {
            throw new IllegalStateException("DynamoDB item is missing its payload attribute");
        }
        try {
            return objectMapper.readValue(payload.s(), type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not deserialize DynamoDB item payload", exception);
        }
    }

    private static AttributeValue stringAttribute(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
