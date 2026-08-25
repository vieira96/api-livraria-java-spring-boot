package com.cursojava.libraryapi.dto.author;

import java.time.LocalDate;

public record CreateAuthorDTO(
        String name,
        LocalDate birthdate,
        String nationality
) {}
