package com.example.chat.service;

import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.assertThat;

class NicknameServiceTest {

    private final NicknameService service = new NicknameService();

    @RepeatedTest(10)
    void generateNickname_format_and_range() {
        String nickname = service.generateNickname();

        assertThat(nickname).startsWith("Guest-");
        String numberPart = nickname.substring("Guest-".length());
        assertThat(numberPart).hasSize(4);

        int value = Integer.parseInt(numberPart);
        assertThat(value).isBetween(1000, 9999);
    }
}

