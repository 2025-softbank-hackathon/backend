package com.example.chat.service;

import com.example.chat.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisPubSubService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;
    private final int limitPerMinute;

    public RedisPubSubService(
            RedisTemplate<String, Object> redisTemplate,
            ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
        this.limitPerMinute = 100;
    }

    public boolean checkLimit(String nickname) {
        String key = "limit:" + nickname;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }
        return count != null && count <= limitPerMinute;
    }

    public void publish(ChatMessage message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}

