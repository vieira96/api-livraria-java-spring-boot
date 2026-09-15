package com.cursojava.libraryapi.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDTO(
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}
