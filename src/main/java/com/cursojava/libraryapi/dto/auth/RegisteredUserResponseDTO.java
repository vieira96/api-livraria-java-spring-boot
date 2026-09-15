package com.cursojava.libraryapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RegisteredUserResponseDTO(
        UUID id,
        String name,
        String email,
        List<String> roles,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "America/Sao_Paulo")
        Instant createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "America/Sao_Paulo")
        Instant updatedAt
) {}
