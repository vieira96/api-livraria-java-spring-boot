package com.cursojava.libraryapi.dto.book;

import com.cursojava.libraryapi.model.book.BookGender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;

import java.util.UUID;

public record BookFiltersDTO(
        @Min(1) Integer page,
        @Min(1) @Max(100) Integer size,
        String title,
        String isbn,
        BookGender gender,
        String search,
        UUID authorId,
        BookSortBy sortBy,
        Sort.Direction direction
) {
    public int pageOrDefault() {
        return page == null ? 0 : page - 1;
    }

    public int sizeOrDefault() {
        return size == null ? 2 : size;
    }

    public BookSortBy sortByOrDefault() {
        return sortBy == null ? BookSortBy.CREATED_AT : sortBy;
    }

    public Sort.Direction directionOrDefault() {
        return direction == null ? Sort.Direction.DESC : direction;
    }
}
