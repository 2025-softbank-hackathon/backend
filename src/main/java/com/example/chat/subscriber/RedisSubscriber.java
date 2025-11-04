package com.example.chat.subscriber;

import com.example.chat.domain.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSubscriber extends MessageListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(RedisSubscriber.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final String topicDestination;

    public RedisSubscriber(SimpMessagingTemplate messagingTemplate,
                           ObjectMapper objectMapper,
                           @Value("${app.ws.topic}") String topicDestination) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.topicDestination = topicDestination;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getBody(), ChatMessage.class);
            messagingTemplate.convertAndSend(topicDestination, chatMessage);
        } catch (Exception e) {
            log.warn("Failed to handle Redis message", e);
        }
    }
}

