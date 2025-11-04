package com.example.chat.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class NicknameService {

    public String generateNickname() {
        int num = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "Guest-" + num;
    }
}

