package com.vieira96.libraryapi.dto.error;

public record LoginRateLimitResponseDTO(
        int status,
        String message,
        long retryAfterSeconds
) {}
