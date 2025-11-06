package com.example.chat.config;

import com.example.chat.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

@Configuration
public class DynamoConfig {

    @Value("${app.dynamodb.table-name}")
    private String tableName;

    @Value("${app.dynamodb.region}")
    private String region;

    @Value("${app.dynamodb.endpoint:}")
    private String endpoint;

    @Value("${app.dynamodb.access-key:}")
    private String accessKey;

    @Value("${app.dynamodb.secret-key:}")
    private String secretKey;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        AwsCredentialsProvider credentialsProvider = StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)
                ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                : DefaultCredentialsProvider.create();

        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);

        if (StringUtils.hasText(endpoint)) {
            builder = builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient enhancedClient(DynamoDbClient base) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(base)
                .build();
    }

    @Bean
    public DynamoDbTable<ChatMessage> messageTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(tableName, TableSchema.fromBean(ChatMessage.class));
    }
}

