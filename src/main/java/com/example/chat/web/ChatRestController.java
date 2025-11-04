package com.example.chat.web;

import com.example.chat.domain.ChatMessage;
import com.example.chat.repository.MessageRepository;
import com.example.chat.service.NicknameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatRestController {

    private final NicknameService nicknameService;
    private final MessageRepository messageRepository;

    public ChatRestController(NicknameService nicknameService, MessageRepository messageRepository) {
        this.nicknameService = nicknameService;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/join")
    public JoinResponse join() {
        return new JoinResponse(nicknameService.generateNickname());
    }

    @GetMapping("/messages")
    public List<ChatMessage> recent() {
        return messageRepository.getRecent(100);
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    public record JoinResponse(String nickname) {
    }
}

