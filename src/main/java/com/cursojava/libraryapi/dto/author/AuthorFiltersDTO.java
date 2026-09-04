package com.cursojava.libraryapi.dto.author;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

public record AuthorFiltersDTO(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        String name,
        String search,
        String nationality,
        @Parameter(
                description = "Dados adicionais na resposta. Aceita bookCount para retornar a quantidade de livros do autor.",
                example = "bookCount"
        ) String include,
        AuthorSortBy sortBy,
        Sort.Direction direction
) {
    public int pageOrDefault() {
        return page == null ? 0 : page - 1;
    }

    public int sizeOrDefault() {
        return size == null ? 2 : size;
    }

    public AuthorSortBy sortByOrDefault() {
        return sortBy == null ? AuthorSortBy.CREATED_AT : sortBy;
    }

    public Sort.Direction directionOrDefault() {
        return direction == null ? Sort.Direction.DESC : direction;
    }

    public boolean includesBookCount() {
        return include != null && Arrays.stream(include.split(","))
                .map(String::trim)
                .anyMatch("bookCount"::equals);
    }
}
