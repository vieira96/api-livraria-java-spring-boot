package com.cursojava.libraryapi.dto.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateAuthorDTO(
        @NotBlank(message = "Nome é obrigatório")
        String name,
        @NotNull(message = "Data de nascimento é obrigatória")
        LocalDate birthdate,
        @NotBlank(message = "Nacionalidade é obrigatória")
        String nationality
) {}
