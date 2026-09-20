package com.vieira96.libraryapi.mapper.book;

import com.vieira96.libraryapi.dto.book.BookResponseDTO;
import com.vieira96.libraryapi.mapper.author.AuthorMapper;
import com.vieira96.libraryapi.model.book.BookModel;

public class BookMapper {
    public static BookResponseDTO toBookResponseDTO(BookModel bookModel) {
        return new BookResponseDTO(
                bookModel.getId(),
                bookModel.getTitle(),
                bookModel.getIsbn(),
                bookModel.getPublishDate(),
                bookModel.getGender().name(),
                bookModel.getPrice(),
                bookModel.getCreatedAt(),
                bookModel.getUpdatedAt(),
                AuthorMapper.toAuthorResponseDTO(bookModel.getAuthor())
        );
    }
}
