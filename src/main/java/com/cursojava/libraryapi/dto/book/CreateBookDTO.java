package com.cursojava.libraryapi.dto.book;

import com.cursojava.libraryapi.model.book.BookGender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBookDTO(
        @NotBlank(message = "O título do livro é obrigatório")
        @Size(
                min = 2,
                max = 100,
                message = "Tamanho do título tem que ser entre {min} e {max}"
        )
        String title,

        @NotBlank(message = "O isbn do livro é obrigatório")
        @Size(
                min = 2,
                max = 20,
                message = "Tamanho do isbn tem que ser entre {min} e {max}"
        )
        String isbn,

        @NotNull(message = "A data de publicação é obrigatória.")
        @PastOrPresent(message = "Data de publicação não pode ser no futuro")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate publishDate,

        @NotNull(message = "O gênero do livro é obrigatório")
        BookGender gender,

        @NotNull(message = "Preço é obrogatório")
        @Min(value = 1, message = "O valor não pode ser menor que 1")
        BigDecimal price,

        @NotNull(message = "O autor do livro é obrigatório")
        UUID authorId
) {}
