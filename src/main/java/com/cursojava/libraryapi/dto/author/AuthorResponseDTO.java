package com.cursojava.libraryapi.dto.author;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AuthorResponseDTO(
        UUID id,
        String name,
        LocalDate birthdate,
        String nationality,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "America/Sao_Paulo")
        Instant createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "America/Sao_Paulo")
        Instant updatedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long bookCount
) {}
