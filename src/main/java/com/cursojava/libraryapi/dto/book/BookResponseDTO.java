package com.cursojava.libraryapi.dto.book;

import com.cursojava.libraryapi.dto.author.AuthorResponseDTO;
import com.cursojava.libraryapi.model.author.AuthorModel;

import java.math.BigDecimal;
import java.util.UUID;

public record BookResponseDTO(
        UUID id,
        String title,
        String isbn,
        String publishDate,
        String gender,
        BigDecimal price,
        AuthorResponseDTO author
) {}
