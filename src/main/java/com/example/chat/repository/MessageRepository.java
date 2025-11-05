package com.example.chat.repository;

import com.example.chat.domain.ChatMessage;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MessageRepository {

    private final DynamoDbTable<ChatMessage> table;

    public MessageRepository(DynamoDbTable<ChatMessage> table) {
        this.table = table;
    }

    public void save(ChatMessage message) {
        table.putItem(message);
    }

    public List<ChatMessage> getRecent(int limit) {
        PageIterable<ChatMessage> pages = table.query(QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue("CHAT").build()))
                .scanIndexForward(false)
                .limit(limit)
                .build());

        List<ChatMessage> result = new ArrayList<>();
        pages.items().forEach(result::add);
        return result;
    }
}

