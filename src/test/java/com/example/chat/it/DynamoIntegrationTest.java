package com.example.chat.it;

import com.example.chat.domain.ChatMessage;
import com.example.chat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class DynamoIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer("localstack/localstack:3.6").withServices(LocalStackContainer.Service.DYNAMODB);

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("app.dynamodb.region", localstack::getRegion);
        registry.add("app.dynamodb.access-key", () -> "test");
        registry.add("app.dynamodb.secret-key", () -> "test");
        registry.add("app.dynamodb.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    }

    @Autowired
    DynamoDbClient dynamoDbClient;

    @Autowired
    MessageRepository messageRepository;

    @BeforeEach
    void ensureTableExists() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not available");

        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName("chatapp-dev-messages").build());
        } catch (ResourceNotFoundException ex) {
            dynamoDbClient.createTable(builder -> builder
                    .tableName("chatapp-dev-messages")
                    .attributeDefinitions(a -> a.attributeName("pk").attributeType("S"),
                            a -> a.attributeName("timestamp").attributeType("N"))
                    .keySchema(k -> k.attributeName("pk").keyType("HASH"),
                            k -> k.attributeName("timestamp").keyType("RANGE"))
                    .billingMode(BillingMode.PAY_PER_REQUEST));
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
}

