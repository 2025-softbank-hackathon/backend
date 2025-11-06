package com.example.chat.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisPubSubServiceTest {

    @Test
    void checkLimit_increments_and_sets_expiry() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class, RETURNS_DEEP_STUBS);
        when(redisTemplate.opsForValue().increment("limit:Guest-1234")).thenReturn(1L);

        RedisPubSubService service = new RedisPubSubService(redisTemplate, new ChannelTopic("chat"), 10);

        boolean allowed = service.checkLimit("Guest-1234");

        assertThat(allowed).isTrue();

        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        verify(redisTemplate).expire(eq("limit:Guest-1234"), ttlCaptor.capture(), eq(TimeUnit.SECONDS));
        assertThat(ttlCaptor.getValue()).isEqualTo(60L);
    }
}

