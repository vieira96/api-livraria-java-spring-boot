package com.cursojava.libraryapi.dto.book;

import com.cursojava.libraryapi.dto.author.AuthorResponseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookResponseDTO(
        UUID id,
        String title,
        String isbn,
        LocalDate publishDate,
        String gender,
        BigDecimal price,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "America/Sao_Paulo")
        Instant createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "America/Sao_Paulo")
        Instant updatedAt,
        AuthorResponseDTO author
) {}
