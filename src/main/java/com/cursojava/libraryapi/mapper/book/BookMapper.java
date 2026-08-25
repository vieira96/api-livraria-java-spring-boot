package com.cursojava.libraryapi.mapper.book;

import com.cursojava.libraryapi.dto.book.BookResponseDTO;
import com.cursojava.libraryapi.mapper.author.AuthorMapper;
import com.cursojava.libraryapi.model.book.BookModel;

public class BookMapper {
    public static BookResponseDTO toBookResponseDTO(BookModel bookModel) {
        return new BookResponseDTO(
                bookModel.getId(),
                bookModel.getTitle(),
                bookModel.getIsbn(),
                bookModel.getPublishDate().toString(),
                bookModel.getGender().name(),
                bookModel.getPrice(),
                AuthorMapper.toAuthorResponseDTO(bookModel.getAuthor())
        );
    }
}
