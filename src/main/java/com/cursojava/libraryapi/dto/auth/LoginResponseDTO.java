package com.cursojava.libraryapi.dto.auth;

public record LoginResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn,
        RegisteredUserResponseDTO user
) {}
