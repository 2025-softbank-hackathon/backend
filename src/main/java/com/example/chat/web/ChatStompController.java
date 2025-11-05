package com.example.chat.web;

import com.example.chat.domain.ChatMessage;
import com.example.chat.repository.MessageRepository;
import com.example.chat.service.RedisPubSubService;
import com.example.chat.web.dto.MessageDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatStompController {

    private final MessageRepository messageRepository;
    private final RedisPubSubService redisPubSubService;
    private final long ttlSeconds;

    public ChatStompController(MessageRepository messageRepository,
                               RedisPubSubService redisPubSubService,
                               @Value("${app.dynamodb.ttl-seconds}") long ttlSeconds) {
        this.messageRepository = messageRepository;
        this.redisPubSubService = redisPubSubService;
        this.ttlSeconds = ttlSeconds;
    }

    @MessageMapping("chatroom.send")
    public void handleMessage(@Payload @Valid MessageDto dto) {
        if (!redisPubSubService.checkLimit(dto.nickname())) {
            throw new IllegalStateException("Too many messages");
        }

        ChatMessage chatMessage = new ChatMessage(dto.nickname(), dto.message(), ttlSeconds);
        messageRepository.save(chatMessage);
        redisPubSubService.publish(chatMessage);
    }
}

