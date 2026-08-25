package com.cursojava.libraryapi.dto.author;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuthorResponseDTO(
        UUID id,
        String name,
        LocalDate birthdate,
        String nationality,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
