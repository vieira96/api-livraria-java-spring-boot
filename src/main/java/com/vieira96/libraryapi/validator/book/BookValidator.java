package com.vieira96.libraryapi.validator.book;

import com.vieira96.libraryapi.exception.book.BookWithISBNAlreadyExists;
import com.vieira96.libraryapi.exception.global.NotFoundException;
import com.vieira96.libraryapi.model.book.BookModel;
import com.vieira96.libraryapi.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookValidator {
    private final BookRepository bookRepository;

    public void verifyIfBookExistsByISBN(String isbn, UUID currentBookUuid) {
        if (this.bookRepository.existsByIsbnAndIdNot(isbn, currentBookUuid)) {
            throw new BookWithISBNAlreadyExists("Já existe um livro com o código isbn: " + isbn);
        }
    }

    public BookModel verifyIfBookExists(UUID bookId) {
        return bookRepository.findWithAuthorById(bookId).orElseThrow(() ->
                new NotFoundException("Livro com o ID: " + bookId + " Não encontrado"));
    }
}
