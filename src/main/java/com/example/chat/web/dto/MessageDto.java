package com.example.chat.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageDto(
        @NotBlank @Size(min = 1, max = 20) String nickname,
        @NotBlank @Size(min = 1, max = 500) String message
) {
}

