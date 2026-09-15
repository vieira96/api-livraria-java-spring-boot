package com.cursojava.libraryapi.dto.auth;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {}
