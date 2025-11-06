package com.example.chat.it;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RedisIntegrationTest {

    private static final String HOST = System.getenv().getOrDefault("REDIS_HOST", "localhost");
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));

    static StringRedisTemplate template;

    @BeforeAll
    static void init() {
        assumeTrue(isRedisReachable(), () -> "Redis not reachable at %s:%d".formatted(HOST, PORT));

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(HOST, PORT);
        connectionFactory.afterPropertiesSet();
        template = new StringRedisTemplate(connectionFactory);
    }

    @Test
    void ping_works() {
        template.opsForValue().set("k", "v");
        assertThat(template.opsForValue().get("k")).isEqualTo("v");
    }

    private static boolean isRedisReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 200);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

