package com.cursojava.libraryapi.dto.author;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateAuthorDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(
                min = 2,
                max = 100,
                message = "Nome deve ter entre {min} e {max} caracteres"
        )
        String name,

        @Past(message = "Data de nascimento deve estar no passado")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "Data de nascimento é obrigatória")
        LocalDate birthdate,

        @NotBlank(message = "Nacionalidade é obrigatória")
        @Size(
                min = 2,
                max = 50,
                message = "Nacionalidade deve ter entre {min} e {max} caracteres"
        )
        String nationality
) {}
