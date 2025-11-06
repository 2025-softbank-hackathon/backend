package com.example.chat.it;

import com.example.chat.domain.ChatMessage;
import com.example.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("test")
class DynamoIntegrationTest {

    private static final String TABLE_NAME = "chatapp-dev-messages";

    @Autowired
    DynamoDbClient dynamoDbClient;

    @Autowired
    MessageRepository messageRepository;

    @BeforeEach
    void ensureTableExists() {
        assumeTrue(isDynamoReachable(), "DynamoDB not reachable with current configuration");

        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(TABLE_NAME).build());
        } catch (ResourceNotFoundException ex) {
            try {
                dynamoDbClient.createTable(CreateTableRequest.builder()
                        .tableName(TABLE_NAME)
                        .attributeDefinitions(a -> a.attributeName("pk").attributeType("S"),
                                a -> a.attributeName("timestamp").attributeType("N"))
                        .keySchema(k -> k.attributeName("pk").keyType("HASH"),
                                k -> k.attributeName("timestamp").keyType("RANGE"))
                        .billingMode(BillingMode.PAY_PER_REQUEST)
                        .build());
            } catch (SdkException e) {
                assumeTrue(false, "Unable to create DynamoDB table: " + e.getMessage());
            }
        }
    }

    @Test
    void save_and_query_recent() {
        ChatMessage message = new ChatMessage("Guest-1234", "hello", 3600);
        messageRepository.save(message);

        List<ChatMessage> messages = messageRepository.getRecent(10);
        assertThat(messages).isNotEmpty();
        ChatMessage first = messages.get(0);
        assertThat(first.getPk()).isEqualTo("CHAT");
        assertThat(first.getTimestamp()).isLessThanOrEqualTo(Instant.now().toEpochMilli());
    }

    private boolean isDynamoReachable() {
        try {
            dynamoDbClient.listTables();
            return true;
        } catch (SdkException e) {
            return false;
        }
    }
}

