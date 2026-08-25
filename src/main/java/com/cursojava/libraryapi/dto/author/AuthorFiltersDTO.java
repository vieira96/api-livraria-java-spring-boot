package com.cursojava.libraryapi.dto.author;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;

public record AuthorFiltersDTO(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        String search,
        String nationality,
        AuthorSortBy sortBy,
        Sort.Direction direction
) {
    public int pageOrDefault() {
        return page == null ? 0 : page;
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
}
