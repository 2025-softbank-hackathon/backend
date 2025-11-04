package com.example.chat.config;

import com.example.chat.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoConfig {

    @Value("${app.dynamodb.table-name}")
    private String tableName;

    @Value("${app.dynamodb.region}")
    private String region;

//    @Bean
//    public DynamoDbClient dynamoDbClient() {
//        return DynamoDbClient.builder()
//                .region(Region.of(region))
//                .credentialsProvider(DefaultCredentialsProvider.create())
//                .build();
//    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:8000")) // 로컬
                .region(Region.AP_NORTHEAST_2)                         // 임의 지역
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("dummy", "dummy")
                        )
                )
                .build();
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

