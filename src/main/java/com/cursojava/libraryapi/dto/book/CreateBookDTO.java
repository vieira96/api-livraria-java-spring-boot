package com.cursojava.libraryapi.dto.book;

import com.cursojava.libraryapi.model.book.BookGender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBookDTO(
        String title,
        String isbn,
        LocalDate publishDate,
        BookGender gender,
        BigDecimal price,
        UUID authorId
) {}
