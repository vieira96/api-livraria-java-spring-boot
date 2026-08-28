package com.cursojava.libraryapi.validator.book;

import com.cursojava.libraryapi.exception.book.BookWithISBNAlreadyExists;
import com.cursojava.libraryapi.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookValidator {
    private final BookRepository bookRepository;

    public void verifyIfBookExistsWithISBN(String isbn) {
        if (this.bookRepository.existsByIsbn(isbn)) {
            throw new BookWithISBNAlreadyExists("Já existe um livro com o código isbn: " + isbn);
        }
    }
}
