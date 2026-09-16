package com.cursojava.libraryapi.dto.error;

public record LoginRateLimitResponseDTO(
        int status,
        String message,
        long retryAfterSeconds
) {}
